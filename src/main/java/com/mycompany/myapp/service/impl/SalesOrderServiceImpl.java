package com.mycompany.myapp.service.impl;

import com.mycompany.myapp.domain.*;
import com.mycompany.myapp.domain.enumeration.OrderStatus;
import com.mycompany.myapp.domain.enumeration.TransactionType;
import com.mycompany.myapp.repository.*;
import com.mycompany.myapp.security.AuthoritiesConstants;
import com.mycompany.myapp.security.SecurityUtils;
import com.mycompany.myapp.service.SalesOrderService;
import com.mycompany.myapp.service.dto.SalesOrderDTO;
import com.mycompany.myapp.service.dto.SalesOrderLineDTO;
import com.mycompany.myapp.service.event.OrderNotificationEvent;
import com.mycompany.myapp.service.mapper.SalesOrderLineMapper;
import com.mycompany.myapp.service.mapper.SalesOrderMapper;
import com.mycompany.myapp.web.rest.errors.BadRequestAlertException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link SalesOrder}.
 */
@Service
@Transactional
public class SalesOrderServiceImpl implements SalesOrderService {

    private final Logger log = LoggerFactory.getLogger(SalesOrderServiceImpl.class);

    private static final String ENTITY_NAME = "salesOrder";

    private final SalesOrderRepository salesOrderRepository;
    private final SalesOrderMapper salesOrderMapper;
    private final SalesOrderLineRepository salesOrderLineRepository;
    private final InventoryTransactionRepository inventoryTransactionRepository;
    private final InventoryBalanceRepository inventoryBalanceRepository;
    private final SalesOrderLineMapper salesOrderLineMapper;
    private final CustomerRepository customerRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final EmployeeRepository employeeRepository;
    private final ProductRepository productRepository; // ĐÃ THÊM

    public SalesOrderServiceImpl(
        SalesOrderRepository salesOrderRepository,
        SalesOrderMapper salesOrderMapper,
        SalesOrderLineRepository salesOrderLineRepository,
        InventoryTransactionRepository inventoryTransactionRepository,
        InventoryBalanceRepository inventoryBalanceRepository,
        SalesOrderLineMapper salesOrderLineMapper,
        CustomerRepository customerRepository,
        ApplicationEventPublisher eventPublisher,
        EmployeeRepository employeeRepository,
        ProductRepository productRepository // ĐÃ THÊM
    ) {
        this.salesOrderRepository = salesOrderRepository;
        this.salesOrderMapper = salesOrderMapper;
        this.salesOrderLineRepository = salesOrderLineRepository;
        this.inventoryTransactionRepository = inventoryTransactionRepository;
        this.inventoryBalanceRepository = inventoryBalanceRepository;
        this.salesOrderLineMapper = salesOrderLineMapper;
        this.customerRepository = customerRepository;
        this.eventPublisher = eventPublisher;
        this.employeeRepository = employeeRepository;
        this.productRepository = productRepository;
    }

    private Employee validateSalesAccess(Long warehouseId) {
        String currentUserLogin = SecurityUtils.getCurrentUserLogin().orElseThrow(() -> new RuntimeException("Chưa đăng nhập!"));

        Employee employee = employeeRepository
            .findByUserLogin(currentUserLogin)
            .orElseThrow(() ->
                new BadRequestAlertException("Tài khoản của bạn chưa được gắn với hồ sơ nhân viên nào!", ENTITY_NAME, "employee_not_found")
            );

        if (SecurityUtils.hasCurrentUserThisAuthority(AuthoritiesConstants.ADMIN)) {
            return employee;
        }

        if (
            employee.getDepartment() == null ||
            employee.getDepartment().getName() != com.mycompany.myapp.domain.enumeration.DepartmentName.SALES
        ) {
            throw new BadRequestAlertException("Tài khoản của bạn không thuộc phòng Bán hàng!", ENTITY_NAME, "invalid_department");
        }

        if (employee.getScopedWarehouse() == null) {
            throw new BadRequestAlertException(
                "Tài khoản của bạn chưa được phân công về chi nhánh/kho nào!",
                ENTITY_NAME,
                "no_scoped_warehouse"
            );
        }

        if (!employee.getScopedWarehouse().getId().equals(warehouseId)) {
            throw new BadRequestAlertException(
                "Bạn không có quyền bán hàng xuất từ chi nhánh khác!",
                ENTITY_NAME,
                "warehouse_access_denied"
            );
        }

        return employee;
    }

    private void checkOrderOwnership(SalesOrder order) {
        if (
            !SecurityUtils.hasCurrentUserThisAuthority(AuthoritiesConstants.ADMIN) &&
            !SecurityUtils.hasCurrentUserThisAuthority(AuthoritiesConstants.MANAGER)
        ) {
            String currentUserLogin = SecurityUtils.getCurrentUserLogin().orElseThrow();
            if (
                order.getEmployee() == null ||
                order.getEmployee().getUser() == null ||
                !order.getEmployee().getUser().getLogin().equals(currentUserLogin)
            ) {
                throw new BadRequestAlertException(
                    "Bạn không có quyền chỉnh sửa đơn bán hàng của người khác!",
                    ENTITY_NAME,
                    "access_denied"
                );
            }
        }
    }

    @Override
    public SalesOrderDTO save(SalesOrderDTO salesOrderDTO) {
        log.debug("Request to save SalesOrder : {}", salesOrderDTO);

        if (salesOrderDTO.getCustomer() == null || salesOrderDTO.getCustomer().getId() == null) {
            throw new BadRequestAlertException("Đơn bán hàng bắt buộc phải chọn Khách hàng!", ENTITY_NAME, "customer_required");
        }
        if (salesOrderDTO.getWarehouse() == null || salesOrderDTO.getWarehouse().getId() == null) {
            throw new BadRequestAlertException("Đơn bán hàng bắt buộc phải chọn Kho xuất hàng!", ENTITY_NAME, "warehouse_required");
        }

        Employee currentEmployee = validateSalesAccess(salesOrderDTO.getWarehouse().getId());

        if (salesOrderDTO.getSalesOrderLines() == null || salesOrderDTO.getSalesOrderLines().isEmpty()) {
            throw new BadRequestAlertException("Đơn bán hàng phải có ít nhất một mặt hàng!", ENTITY_NAME, "empty_lines");
        }

        salesOrderDTO.setOrderCode("SO-" + Instant.now().toEpochMilli());
        salesOrderDTO.setStatus(OrderStatus.DRAFT);
        salesOrderDTO.setCreatedDate(Instant.now());

        List<Long> productIds = salesOrderDTO
            .getSalesOrderLines()
            .stream()
            .map(line -> {
                if (line.getProduct() == null || line.getProduct().getId() == null) {
                    throw new BadRequestAlertException("Mặt hàng không được để trống sản phẩm!", ENTITY_NAME, "invalid_product");
                }
                return line.getProduct().getId();
            })
            .collect(Collectors.toList());

        Map<Long, Product> productMap = productRepository
            .findAllById(productIds)
            .stream()
            .collect(Collectors.toMap(Product::getId, p -> p));

        BigDecimal calculatedTotal = BigDecimal.ZERO;
        for (SalesOrderLineDTO line : salesOrderDTO.getSalesOrderLines()) {
            Product product = productMap.get(line.getProduct().getId());
            if (product == null) {
                throw new BadRequestAlertException("Không tìm thấy sản phẩm!", ENTITY_NAME, "product_not_found");
            }

            // Ép cứng đơn giá bán từ Database gốc lên DTO
            line.setUnitPrice(product.getSellingPrice());

            if (line.getQuantity() == null || line.getQuantity() <= 0) {
                throw new BadRequestAlertException("Số lượng mặt hàng không hợp lệ!", ENTITY_NAME, "invalid_quantity");
            }

            BigDecimal discountPercent = line.getDiscountPercent() != null ? line.getDiscountPercent() : BigDecimal.ZERO;
            if (discountPercent.compareTo(BigDecimal.ZERO) < 0 || discountPercent.compareTo(new BigDecimal("100")) > 0) {
                throw new BadRequestAlertException("Phần trăm giảm giá phải từ 0 đến 100!", ENTITY_NAME, "invalid_discount");
            }

            // Tiền gốc của dòng = Giá gốc x Số lượng
            BigDecimal lineBaseAmount = line.getUnitPrice().multiply(new BigDecimal(line.getQuantity()));
            // Tiền được giảm = Tiền gốc x % giảm giá / 100
            BigDecimal lineDiscountAmount = lineBaseAmount.multiply(discountPercent).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
            // Tiền thực tế của dòng = Tiền gốc - Tiền được giảm
            BigDecimal lineFinalAmount = lineBaseAmount.subtract(lineDiscountAmount);

            calculatedTotal = calculatedTotal.add(lineFinalAmount);
        }

        if (salesOrderDTO.getTotalAmount() == null || salesOrderDTO.getTotalAmount().compareTo(calculatedTotal) != 0) {
            throw new BadRequestAlertException("Tổng tiền đơn hàng không hợp lệ!", ENTITY_NAME, "total_amount_mismatch");
        }
        salesOrderDTO.setTotalAmount(calculatedTotal);

        SalesOrder salesOrder = salesOrderMapper.toEntity(salesOrderDTO);
        salesOrder.setEmployee(currentEmployee);
        salesOrder = salesOrderRepository.save(salesOrder);

        List<SalesOrderLine> linesToSave = new ArrayList<>();
        for (SalesOrderLineDTO lineDTO : salesOrderDTO.getSalesOrderLines()) {
            SalesOrderLine line = salesOrderLineMapper.toEntity(lineDTO);
            line.setSalesOrder(salesOrder);
            linesToSave.add(line);
        }
        salesOrderLineRepository.saveAll(linesToSave);

        SalesOrderDTO result = salesOrderMapper.toDto(salesOrder);
        String currentLogin = SecurityUtils.getCurrentUserLogin().orElse("System");
        eventPublisher.publishEvent(
            new OrderNotificationEvent("SALES", "CREATED", result.getId(), result.getOrderCode(), currentLogin, currentLogin)
        );

        return result;
    }

    @Override
    public SalesOrderDTO update(SalesOrderDTO salesOrderDTO) {
        log.debug("Request to update SalesOrder : {}", salesOrderDTO);

        if (salesOrderDTO.getCustomer() == null || salesOrderDTO.getCustomer().getId() == null) {
            throw new BadRequestAlertException("Đơn bán hàng bắt buộc phải chọn Khách hàng!", ENTITY_NAME, "customer_required");
        }
        if (salesOrderDTO.getWarehouse() == null || salesOrderDTO.getWarehouse().getId() == null) {
            throw new BadRequestAlertException("Đơn bán hàng bắt buộc phải chọn Kho xuất hàng!", ENTITY_NAME, "warehouse_required");
        }
        if (salesOrderDTO.getSalesOrderLines() == null || salesOrderDTO.getSalesOrderLines().isEmpty()) {
            throw new BadRequestAlertException("Đơn bán hàng phải có ít nhất một mặt hàng!", ENTITY_NAME, "empty_lines");
        }

        SalesOrder oldOrder = salesOrderRepository
            .findById(salesOrderDTO.getId())
            .orElseThrow(() -> new BadRequestAlertException("Không tìm thấy đơn hàng", ENTITY_NAME, "id_not_found"));

        validateSalesAccess(oldOrder.getWarehouse().getId());
        checkOrderOwnership(oldOrder);

        if (oldOrder.getStatus() != OrderStatus.DRAFT) {
            throw new BadRequestAlertException("Chỉ được sửa đơn hàng ở trạng thái NHÁP!", ENTITY_NAME, "cannot_edit");
        }

        if (!salesOrderDTO.getWarehouse().getId().equals(oldOrder.getWarehouse().getId())) {
            throw new BadRequestAlertException("Không được thay đổi kho xuất của đơn đã tạo!", ENTITY_NAME, "warehouse_immutable");
        }

        salesOrderDTO.setOrderCode(oldOrder.getOrderCode());
        salesOrderDTO.setStatus(OrderStatus.DRAFT);
        salesOrderDTO.setCreatedDate(oldOrder.getCreatedDate());

        List<Long> productIds = salesOrderDTO
            .getSalesOrderLines()
            .stream()
            .map(line -> line.getProduct().getId())
            .collect(Collectors.toList());

        Map<Long, Product> productMap = productRepository
            .findAllById(productIds)
            .stream()
            .collect(Collectors.toMap(Product::getId, p -> p));

        BigDecimal calculatedTotal = BigDecimal.ZERO;
        for (SalesOrderLineDTO line : salesOrderDTO.getSalesOrderLines()) {
            Product product = productMap.get(line.getProduct().getId());
            if (product == null) {
                throw new BadRequestAlertException("Sản phẩm không hợp lệ!", ENTITY_NAME, "product_not_found");
            }

            line.setUnitPrice(product.getSellingPrice());

            BigDecimal discountPercent = line.getDiscountPercent() != null ? line.getDiscountPercent() : BigDecimal.ZERO;
            BigDecimal lineBaseAmount = line.getUnitPrice().multiply(new BigDecimal(line.getQuantity()));
            BigDecimal lineDiscountAmount = lineBaseAmount.multiply(discountPercent).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
            BigDecimal lineFinalAmount = lineBaseAmount.subtract(lineDiscountAmount);

            calculatedTotal = calculatedTotal.add(lineFinalAmount);
        }

        if (salesOrderDTO.getTotalAmount() == null || salesOrderDTO.getTotalAmount().compareTo(calculatedTotal) != 0) {
            throw new BadRequestAlertException("Tổng tiền đơn hàng không hợp lệ!", ENTITY_NAME, "total_amount_mismatch");
        }
        salesOrderDTO.setTotalAmount(calculatedTotal);

        SalesOrder salesOrder = salesOrderMapper.toEntity(salesOrderDTO);
        salesOrder.setEmployee(oldOrder.getEmployee());
        salesOrder = salesOrderRepository.save(salesOrder);

        List<SalesOrderLine> oldLines = salesOrderLineRepository.findBySalesOrderId(salesOrder.getId());
        salesOrderLineRepository.deleteAll(oldLines);

        List<SalesOrderLine> newLines = new ArrayList<>();
        for (SalesOrderLineDTO lineDTO : salesOrderDTO.getSalesOrderLines()) {
            SalesOrderLine line = salesOrderLineMapper.toEntity(lineDTO);
            line.setSalesOrder(salesOrder);
            newLines.add(line);
        }
        salesOrderLineRepository.saveAll(newLines);

        return salesOrderMapper.toDto(salesOrder);
    }

    @Override
    public Optional<SalesOrderDTO> partialUpdate(SalesOrderDTO salesOrderDTO) {
        log.debug("Request to partially update SalesOrder : {}", salesOrderDTO);

        return salesOrderRepository
            .findById(salesOrderDTO.getId())
            .map(existingOrder -> {
                validateSalesAccess(existingOrder.getWarehouse().getId());
                checkOrderOwnership(existingOrder);

                if (existingOrder.getStatus() != OrderStatus.DRAFT) {
                    throw new BadRequestAlertException("Chỉ được sửa đơn hàng ở trạng thái NHÁP!", ENTITY_NAME, "cannot_edit");
                }

                BigDecimal calculatedTotal = BigDecimal.ZERO;

                if (salesOrderDTO.getSalesOrderLines() != null) {
                    if (salesOrderDTO.getSalesOrderLines().isEmpty()) {
                        throw new BadRequestAlertException("Đơn bán hàng phải có ít nhất một mặt hàng!", ENTITY_NAME, "empty_lines");
                    }

                    List<Long> productIds = salesOrderDTO
                        .getSalesOrderLines()
                        .stream()
                        .map(line -> line.getProduct().getId())
                        .collect(Collectors.toList());

                    Map<Long, Product> productMap = productRepository
                        .findAllById(productIds)
                        .stream()
                        .collect(Collectors.toMap(Product::getId, p -> p));

                    for (SalesOrderLineDTO line : salesOrderDTO.getSalesOrderLines()) {
                        Product product = productMap.get(line.getProduct().getId());
                        if (product == null) {
                            throw new BadRequestAlertException("Không tìm thấy sản phẩm!", ENTITY_NAME, "product_not_found");
                        }

                        line.setUnitPrice(product.getSellingPrice());

                        BigDecimal discountPercent = line.getDiscountPercent() != null ? line.getDiscountPercent() : BigDecimal.ZERO;
                        BigDecimal lineBaseAmount = line.getUnitPrice().multiply(new BigDecimal(line.getQuantity()));
                        BigDecimal lineDiscountAmount = lineBaseAmount
                            .multiply(discountPercent)
                            .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
                        BigDecimal lineFinalAmount = lineBaseAmount.subtract(lineDiscountAmount);

                        calculatedTotal = calculatedTotal.add(lineFinalAmount);
                    }
                } else {
                    // Nếu không đổi danh sách hàng -> Tính tổng dựa trên dòng cũ lưu trong DB
                    if (existingOrder.getOrderLines() != null) {
                        for (SalesOrderLine line : existingOrder.getOrderLines()) {
                            BigDecimal discountPercent = line.getDiscountPercent() != null ? line.getDiscountPercent() : BigDecimal.ZERO;
                            BigDecimal lineBaseAmount = line.getUnitPrice().multiply(new BigDecimal(line.getQuantity()));
                            BigDecimal lineDiscountAmount = lineBaseAmount
                                .multiply(discountPercent)
                                .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
                            BigDecimal lineFinalAmount = lineBaseAmount.subtract(lineDiscountAmount);

                            calculatedTotal = calculatedTotal.add(lineFinalAmount);
                        }
                    }
                }

                if (salesOrderDTO.getTotalAmount() != null && salesOrderDTO.getTotalAmount().compareTo(calculatedTotal) != 0) {
                    throw new BadRequestAlertException("Tổng tiền đơn hàng không hợp lệ!", ENTITY_NAME, "total_amount_mismatch");
                }
                salesOrderDTO.setTotalAmount(calculatedTotal);

                salesOrderMapper.partialUpdate(existingOrder, salesOrderDTO);
                return existingOrder;
            })
            .map(salesOrderRepository::save)
            .map(savedOrder -> {
                if (salesOrderDTO.getSalesOrderLines() != null) {
                    List<SalesOrderLine> oldLines = salesOrderLineRepository.findBySalesOrderId(savedOrder.getId());
                    salesOrderLineRepository.deleteAll(oldLines);

                    List<SalesOrderLine> newLines = new ArrayList<>();
                    for (SalesOrderLineDTO lineDTO : salesOrderDTO.getSalesOrderLines()) {
                        SalesOrderLine line = salesOrderLineMapper.toEntity(lineDTO);
                        line.setSalesOrder(savedOrder);
                        newLines.add(line);
                    }
                    salesOrderLineRepository.saveAll(newLines);
                }
                return salesOrderMapper.toDto(savedOrder);
            });
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SalesOrderDTO> findAll(Pageable pageable) {
        log.debug("Request to get all SalesOrders with Data Filtering");
        return getFilteredSalesOrders(pageable, false);
    }

    public Page<SalesOrderDTO> findAllWithEagerRelationships(Pageable pageable) {
        log.debug("Request to get all SalesOrders with eager relationships and Data Filtering");
        return getFilteredSalesOrders(pageable, true);
    }

    private Page<SalesOrderDTO> getFilteredSalesOrders(Pageable pageable, boolean eager) {
        boolean isAdmin = SecurityUtils.hasCurrentUserThisAuthority(AuthoritiesConstants.ADMIN);
        boolean isAccountant = SecurityUtils.hasCurrentUserThisAuthority(AuthoritiesConstants.ACCOUNTANT);
        boolean isManager = SecurityUtils.hasCurrentUserThisAuthority(AuthoritiesConstants.MANAGER);

        if (isAdmin || isAccountant) {
            if (eager) return salesOrderRepository.findAllWithEagerRelationships(pageable).map(salesOrderMapper::toDto);
            return salesOrderRepository.findAll(pageable).map(salesOrderMapper::toDto);
        }

        String currentUserLogin = SecurityUtils.getCurrentUserLogin().orElseThrow(() -> new RuntimeException("Chưa đăng nhập!"));

        if (isManager) {
            return salesOrderRepository.findAllByEmployeeScopedWarehouse(currentUserLogin, pageable).map(salesOrderMapper::toDto);
        }

        return salesOrderRepository.findAllByEmployeeUserLogin(currentUserLogin, pageable).map(salesOrderMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<SalesOrderDTO> findOne(Long id) {
        log.debug("Request to get SalesOrder : {}", id);

        boolean isAdmin = SecurityUtils.hasCurrentUserThisAuthority(AuthoritiesConstants.ADMIN);
        boolean isAccountant = SecurityUtils.hasCurrentUserThisAuthority(AuthoritiesConstants.ACCOUNTANT);
        boolean isManager = SecurityUtils.hasCurrentUserThisAuthority(AuthoritiesConstants.MANAGER);

        if (isAdmin || isAccountant) {
            return salesOrderRepository.findOneWithEagerRelationships(id).map(salesOrderMapper::toDto);
        }

        String currentUserLogin = SecurityUtils.getCurrentUserLogin().orElseThrow();

        if (isManager) {
            return salesOrderRepository.findOneByIdAndEmployeeScopedWarehouse(id, currentUserLogin).map(salesOrderMapper::toDto);
        }

        return salesOrderRepository.findOneByIdAndEmployeeUserLogin(id, currentUserLogin).map(salesOrderMapper::toDto);
    }

    @Override
    public void delete(Long id) {
        log.debug("Request to delete SalesOrder : {}", id);

        SalesOrder order = salesOrderRepository
            .findById(id)
            .orElseThrow(() -> new BadRequestAlertException("Không tìm thấy đơn hàng", ENTITY_NAME, "id_not_found"));

        validateSalesAccess(order.getWarehouse().getId());
        checkOrderOwnership(order);

        if (order.getStatus() != OrderStatus.DRAFT) {
            throw new BadRequestAlertException("Đại kỵ! Chỉ được xóa đơn hàng ở trạng thái NHÁP!", ENTITY_NAME, "cannot_delete");
        }

        // 👇 Lưu vết mã đơn và người tạo TRƯỚC KHI XÓA
        String orderCode = order.getOrderCode();
        String creatorLogin = order.getEmployee() != null && order.getEmployee().getUser() != null ? order.getEmployee().getUser().getLogin() : "System";

        List<SalesOrderLine> lines = salesOrderLineRepository.findBySalesOrderId(id);
        salesOrderLineRepository.deleteAll(lines);
        salesOrderRepository.deleteById(id);

        // 👇 Bắn sự kiện DELETED báo cho quản lý
        String currentLogin = SecurityUtils.getCurrentUserLogin().orElse("System");
        eventPublisher.publishEvent(
            new OrderNotificationEvent("SALES", "DELETED", id, orderCode, currentLogin, creatorLogin)
        );
    }

    @Transactional
    @Override
    public SalesOrderDTO approveOrder(Long id) {
        log.debug("Request to approve SalesOrder : {}", id);

        SalesOrder order = salesOrderRepository.findById(id).orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));

        validateSalesAccess(order.getWarehouse().getId());

        if (order.getStatus() == OrderStatus.APPROVED) {
            throw new BadRequestAlertException("Đơn hàng này đã được duyệt trước đó!", ENTITY_NAME, "already_approved");
        }

        // KIỂM TRA TỒN KHO THỰC TẾ TRƯỚC KHI DUYỆT XUẤT ĐƠN BÁN
        List<SalesOrderLine> lines = salesOrderLineRepository.findBySalesOrderId(order.getId());
        if (lines.isEmpty()) {
            throw new BadRequestAlertException("Đơn hàng trống rỗng, không thể duyệt!", ENTITY_NAME, "empty_lines");
        }

        List<Long> productIds = lines.stream().map(line -> line.getProduct().getId()).collect(Collectors.toList());

        Map<Long, InventoryBalance> balanceMap = inventoryBalanceRepository
            .findByWarehouseIdAndProductIdIn(order.getWarehouse().getId(), productIds)
            .stream()
            .collect(Collectors.toMap(b -> b.getProduct().getId(), b -> b));

        List<InventoryTransaction> transactionsToSave = new ArrayList<>();
        Instant now = Instant.now();

        for (SalesOrderLine line : lines) {
            InventoryBalance balance = balanceMap.get(line.getProduct().getId());

            if (balance == null || balance.getQuantity() < line.getQuantity()) {
                throw new BadRequestAlertException(
                    "Kho không đủ tồn kho cho sản phẩm: " + line.getProduct().getName(),
                    ENTITY_NAME,
                    "insufficient_stock"
                );
            }

            // Trừ số lượng tồn kho trực tiếp trên RAM
            balance.setQuantity(balance.getQuantity() - line.getQuantity());

            //Cảnh báo chạm đáy tồn kho (Ngưỡng = 10)
            if (balance.getQuantity() <= 10) {
                String currentLogin = SecurityUtils.getCurrentUserLogin().orElse("System");

                // Mượn OrderNotificationEvent để bắn cảnh báo
                eventPublisher.publishEvent(
                    new OrderNotificationEvent(
                        "INVENTORY", // Type: Đánh dấu đây là event của Tồn kho
                        "LOW_STOCK", // Action: Chạm đáy
                        line.getProduct().getId(), // Truyền ID sản phẩm
                        line.getProduct().getName() + " (Chỉ còn " + balance.getQuantity() + " sản phẩm)", // Tên SP + Số lượng
                        currentLogin,
                        String.valueOf(order.getWarehouse().getId()) // Mượn field này để nhét ID Kho vào truyền đi
                    )
                );
            }

            // Ghi nhận lịch sử: THẺ KHO XUẤT HÀNG (ISSUE)
            InventoryTransaction transaction = new InventoryTransaction();
            transaction.setType(TransactionType.ISSUE);
            transaction.setQuantity(line.getQuantity());
            transaction.setUnitCost(line.getUnitPrice());
            transaction.setReferenceId(order.getId());
            transaction.setCreatedDate(now);
            transaction.setProduct(line.getProduct());
            transaction.setWarehouse(order.getWarehouse());
            transactionsToSave.add(transaction);
        }

        try {
            inventoryBalanceRepository.saveAll(balanceMap.values());
            inventoryTransactionRepository.saveAll(transactionsToSave);

            order.setStatus(OrderStatus.APPROVED);
            order = salesOrderRepository.save(order);

            inventoryBalanceRepository.flush();
            inventoryTransactionRepository.flush();
        } catch (org.springframework.orm.ObjectOptimisticLockingFailureException e) {
            throw new BadRequestAlertException(
                "Xung đột dữ liệu tồn kho do có giao dịch đồng thời. Vui lòng thử lại!",
                ENTITY_NAME,
                "optimistic_locking_conflict"
            );
        }

        String currentLogin = SecurityUtils.getCurrentUserLogin().orElse("System");
        String creatorLogin = order.getEmployee().getUser().getLogin();
        eventPublisher.publishEvent(
            new OrderNotificationEvent("SALES", "APPROVED", order.getId(), order.getOrderCode(), currentLogin, creatorLogin)
        );

        return salesOrderMapper.toDto(order);
    }

    @Transactional
    @Override
    public SalesOrderDTO completeOrder(Long id) {
        log.debug("Kế toán xác nhận nhận đủ tiền, chốt sổ đơn bán hàng: {}", id);
        SalesOrder order = salesOrderRepository.findById(id).orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));

        if (order.getStatus() == OrderStatus.COMPLETED) {
            throw new BadRequestAlertException("Đơn hàng này đã được chốt hoàn thành!", ENTITY_NAME, "already_completed");
        }
        if (order.getStatus() != OrderStatus.PROCESSING) {
            throw new BadRequestAlertException(
                "Chỉ được chốt đơn khi Shipper đã giao tới nơi (PROCESSING)!",
                ENTITY_NAME,
                "invalid_status"
            );
        }

        Customer customer = order.getCustomer();
        if (customer == null) {
            throw new BadRequestAlertException("Lỗi dữ liệu hệ thống: Đơn hàng không có Khách hàng!", ENTITY_NAME, "customer_missing");
        }

        BigDecimal currentDebt = customer.getCurrentDebt() != null ? customer.getCurrentDebt() : BigDecimal.ZERO;
        BigDecimal orderTotal = order.getTotalAmount() != null ? order.getTotalAmount() : BigDecimal.ZERO;

        // Tăng công nợ (phải thu) của khách hàng
        customer.setCurrentDebt(currentDebt.add(orderTotal));
        order.setStatus(OrderStatus.COMPLETED);

        try {
            customerRepository.save(customer);
            order = salesOrderRepository.save(order);
            customerRepository.flush();
        } catch (org.springframework.orm.ObjectOptimisticLockingFailureException e) {
            throw new BadRequestAlertException(
                "Dữ liệu công nợ khách hàng đang bị biến động ở luồng khác. Vui lòng thử lại!",
                ENTITY_NAME,
                "optimistic_locking_debt_conflict"
            );
        }

        String currentLogin = SecurityUtils.getCurrentUserLogin().orElse("System");
        String creatorLogin = (order.getEmployee() != null && order.getEmployee().getUser() != null)
            ? order.getEmployee().getUser().getLogin()
            : currentLogin;
        eventPublisher.publishEvent(
            new OrderNotificationEvent("SALES", "COMPLETED", order.getId(), order.getOrderCode(), currentLogin, creatorLogin)
        );

        return salesOrderMapper.toDto(order);
    }

    @Transactional
    @Override
    public SalesOrderDTO cancelOrder(Long id) {
        log.debug("Request to cancel SalesOrder : {}", id);
        SalesOrder order = salesOrderRepository.findById(id).orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));

        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new BadRequestAlertException("Đơn hàng này đã bị hủy trước đó!", ENTITY_NAME, "already_cancelled");
        }

        if (order.getStatus() == OrderStatus.COMPLETED) {
            throw new BadRequestAlertException(
                "Không thể hủy đơn bán hàng đã HOÀN THÀNH. Vui lòng sử dụng quy trình Khách Hàng Trả Hàng (Return Process)!",
                ENTITY_NAME,
                "cannot_cancel_completed_order"
            );
        }

        boolean isAdmin = SecurityUtils.hasCurrentUserThisAuthority(AuthoritiesConstants.ADMIN);
        boolean isManager = SecurityUtils.hasCurrentUserThisAuthority(AuthoritiesConstants.MANAGER);

        if (!isAdmin && !isManager) {
            throw new BadRequestAlertException("Chỉ Quản lý hoặc Admin mới được phép hủy đơn bán hàng!", ENTITY_NAME, "permission_denied");
        }

        if (isManager) {
            validateSalesAccess(order.getWarehouse().getId());
        }

        // HOÀN LẠI TỒN KHO NẾU ĐƠN ĐÃ APPROVED/PROCESSING (Vì đã lỡ trừ kho lúc duyệt)
        if (order.getStatus() == OrderStatus.APPROVED || order.getStatus() == OrderStatus.PROCESSING) {
            List<SalesOrderLine> lines = salesOrderLineRepository.findBySalesOrderId(order.getId());

            if (!lines.isEmpty()) {
                List<Long> productIds = lines.stream().map(line -> line.getProduct().getId()).collect(Collectors.toList());
                Map<Long, InventoryBalance> balanceMap = inventoryBalanceRepository
                    .findByWarehouseIdAndProductIdIn(order.getWarehouse().getId(), productIds)
                    .stream()
                    .collect(Collectors.toMap(b -> b.getProduct().getId(), b -> b));

                List<InventoryTransaction> receiptTransactions = new ArrayList<>();
                Instant now = Instant.now();

                for (SalesOrderLine line : lines) {
                    InventoryBalance balance = balanceMap.get(line.getProduct().getId());
                    if (balance == null) {
                        balance = new InventoryBalance();
                        balance.setProduct(line.getProduct());
                        balance.setWarehouse(order.getWarehouse());
                        balance.setQuantity(0);
                    }

                    // Cộng trả lại kho (Vì hủy đơn = nhận lại hàng về kho)
                    balance.setQuantity(balance.getQuantity() + line.getQuantity());

                    InventoryTransaction receiptTx = new InventoryTransaction();
                    receiptTx.setType(TransactionType.RECEIPT);
                    receiptTx.setQuantity(line.getQuantity());
                    receiptTx.setUnitCost(line.getUnitPrice());
                    receiptTx.setReferenceId(order.getId());
                    receiptTx.setCreatedDate(now);
                    receiptTx.setProduct(line.getProduct());
                    receiptTx.setWarehouse(order.getWarehouse());
                    receiptTransactions.add(receiptTx);
                }

                try {
                    inventoryBalanceRepository.saveAll(balanceMap.values());
                    inventoryTransactionRepository.saveAll(receiptTransactions);
                    inventoryBalanceRepository.flush();
                    inventoryTransactionRepository.flush();
                } catch (org.springframework.orm.ObjectOptimisticLockingFailureException e) {
                    throw new BadRequestAlertException(
                        "Xung đột dữ liệu tồn kho khi hoàn trả hàng hủy!",
                        ENTITY_NAME,
                        "optimistic_locking_conflict"
                    );
                }
            }
        }

        order.setStatus(OrderStatus.CANCELLED);
        order = salesOrderRepository.save(order);

        String currentLogin = SecurityUtils.getCurrentUserLogin().orElse("System");
        String creatorLogin = (order.getEmployee() != null && order.getEmployee().getUser() != null)
            ? order.getEmployee().getUser().getLogin()
            : currentLogin;
        eventPublisher.publishEvent(
            new OrderNotificationEvent("SALES", "CANCELLED", order.getId(), order.getOrderCode(), currentLogin, creatorLogin)
        );

        return salesOrderMapper.toDto(order);
    }

    @Transactional
    @Override
    public SalesOrderDTO startDelivery(Long id) {
        log.debug("Shipper bắt đầu giao đơn bán lẻ ra xe: {}", id);
        SalesOrder order = salesOrderRepository.findById(id).orElseThrow();

        if (order.getStatus() != OrderStatus.APPROVED) {
            throw new BadRequestAlertException(
                "Chỉ đi giao hàng khi đơn đã được duyệt xuất kho (APPROVED)!",
                ENTITY_NAME,
                "invalid_status"
            );
        }

        order.setStatus(OrderStatus.PROCESSING); // Hàng đang trên đường đi giao
        order = salesOrderRepository.save(order);

        String currentLogin = SecurityUtils.getCurrentUserLogin().orElse("System");
        eventPublisher.publishEvent(
            new OrderNotificationEvent(
                "SALES",
                "PROCESSING",
                order.getId(),
                order.getOrderCode(), // VÁ LỖI: Trả về đúng mã đơn
                currentLogin,         // VÁ LỖI: Lấy user hiện tại thay vì "System"
                order.getEmployee().getUser().getLogin()
            )
        );
        return salesOrderMapper.toDto(order);
    }

    @Transactional
    @Override
    public SalesOrderDTO confirmDelivery(Long id) {
        log.debug("Shipper báo cáo giao thành công đơn: {}", id);
        SalesOrder order = salesOrderRepository
            .findById(id)
            .orElseThrow(() -> new BadRequestAlertException("Không tìm thấy đơn", ENTITY_NAME, "id_not_found"));

        if (order.getStatus() != OrderStatus.PROCESSING) {
            throw new BadRequestAlertException("Đơn hàng chưa ở trạng thái Đang giao!", ENTITY_NAME, "invalid_status");
        }

        String currentLogin = SecurityUtils.getCurrentUserLogin().orElse("System");
        eventPublisher.publishEvent(
            new OrderNotificationEvent(
                "SALES",
                "DELIVERED_WAITING_PAYMENT",
                order.getId(),
                order.getOrderCode(), // VÁ LỖI
                currentLogin,         // VÁ LỖI
                order.getEmployee().getUser().getLogin() // VÁ LỖI: Truyền lại đúng originalCreator thay vì "ACCOUNTANT_GROUP"
            )
        );

        return salesOrderMapper.toDto(order);
    }
}
