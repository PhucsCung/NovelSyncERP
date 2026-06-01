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

    private final SalesOrderRepository salesOrderRepository;

    private final SalesOrderMapper salesOrderMapper;
    private final SalesOrderLineRepository salesOrderLineRepository;
    private final InventoryTransactionRepository inventoryTransactionRepository;
    private final InventoryBalanceRepository inventoryBalanceRepository;
    private final SalesOrderLineMapper salesOrderLineMapper;
    private final CustomerRepository customerRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final EmployeeRepository employeeRepository;

    public SalesOrderServiceImpl(
        SalesOrderRepository salesOrderRepository,
        SalesOrderMapper salesOrderMapper,
        SalesOrderLineRepository salesOrderLineRepository,
        InventoryTransactionRepository inventoryTransactionRepository,
        InventoryBalanceRepository inventoryBalanceRepository,
        SalesOrderLineMapper salesOrderLineMapper,
        CustomerRepository customerRepository,
        ApplicationEventPublisher eventPublisher,
        EmployeeRepository employeeRepository
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
    }

    @Override
    public SalesOrderDTO save(SalesOrderDTO salesOrderDTO) {
        log.debug("Request to save SalesOrder : {}", salesOrderDTO);

        if (salesOrderDTO.getCustomer() == null || salesOrderDTO.getCustomer().getId() == null) {
            throw new BadRequestAlertException("Đơn bán hàng bắt buộc phải chọn Khách hàng!", "salesOrder", "customer_required");
        }

        if (salesOrderDTO.getWarehouse() == null || salesOrderDTO.getWarehouse().getId() == null) {
            throw new BadRequestAlertException("Đơn bán hàng bắt buộc phải chọn Kho xuất hàng!", "salesOrder", "warehouse_required");
        }
        //(Lấy ra hồ sơ nhân viên tạo đơn)
        Employee currentEmployee = validateSalesAccess(salesOrderDTO.getWarehouse().getId());

        //KIỂM TRA TÍNH TOÀN VẸN CỦA ĐƠN HÀNG ---
        if (salesOrderDTO.getSalesOrderLines() == null || salesOrderDTO.getSalesOrderLines().isEmpty()) {
            throw new BadRequestAlertException("Đơn bán hàng phải có ít nhất một mặt hàng!", "salesOrder", "empty_lines");
        }

        // TỰ ĐỘNG SINH MÃ ĐƠN HÀNG Ở BACKEND (Format: SO- + mili-giây hiện tại)
        // Ví dụ: SO-1717255100000 -> Không bao giờ lo trùng lặp!
        String generatedOrderCode = "SO-" + Instant.now().toEpochMilli();
        salesOrderDTO.setOrderCode(generatedOrderCode);

        // Ép trạng thái về DRAFT (Nháp)
        salesOrderDTO.setStatus(OrderStatus.DRAFT);

        // Tính toán và đối chiếu tổng tiền
        BigDecimal calculatedTotal = BigDecimal.ZERO;
        for (SalesOrderLineDTO line : salesOrderDTO.getSalesOrderLines()) {
            if (line.getUnitPrice() == null || line.getQuantity() == null) {
                throw new BadRequestAlertException(
                    "Mặt hàng không được để trống đơn giá hoặc số lượng!",
                    "salesOrder",
                    "invalid_line_data"
                );
            }
            BigDecimal lineTotal = line.getUnitPrice().multiply(new BigDecimal(line.getQuantity()));
            calculatedTotal = calculatedTotal.add(lineTotal);
        }

        if (salesOrderDTO.getTotalAmount() == null || salesOrderDTO.getTotalAmount().compareTo(calculatedTotal) != 0) {
            throw new BadRequestAlertException(
                "Tổng tiền đơn hàng không hợp lệ! Front-end gửi: " + salesOrderDTO.getTotalAmount() + ", Server tính: " + calculatedTotal,
                "salesOrder",
                "total_amount_mismatch"
            );
        }

        // Lưu Phiếu Cha
        SalesOrder salesOrder = salesOrderMapper.toEntity(salesOrderDTO);
        salesOrder.setEmployee(currentEmployee); // Gắn chặt chủ nhân vào đơn hàng

        salesOrder = salesOrderRepository.save(salesOrder);

        // Lưu Phiếu Con (Batch Insert)
        List<SalesOrderLine> linesToSave = new ArrayList<>();
        for (SalesOrderLineDTO lineDTO : salesOrderDTO.getSalesOrderLines()) {
            SalesOrderLine line = salesOrderLineMapper.toEntity(lineDTO);
            line.setSalesOrder(salesOrder); // Trỏ khóa ngoại về ID phiếu cha vừa sinh
            linesToSave.add(line);
        }
        salesOrderLineRepository.saveAll(linesToSave);

        // ---BẮN THÔNG BÁO ---
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
            throw new BadRequestAlertException("Đơn bán hàng bắt buộc phải chọn Khách hàng!", "salesOrder", "customer_required");
        }
        if (salesOrderDTO.getWarehouse() == null || salesOrderDTO.getWarehouse().getId() == null) {
            throw new BadRequestAlertException("Đơn bán hàng bắt buộc phải chọn Kho xuất hàng!", "salesOrder", "warehouse_required");
        }
        if (salesOrderDTO.getSalesOrderLines() == null || salesOrderDTO.getSalesOrderLines().isEmpty()) {
            throw new BadRequestAlertException("Đơn bán hàng phải có ít nhất một mặt hàng!", "salesOrder", "empty_lines");
        }

        SalesOrder oldOrder = salesOrderRepository
            .findById(salesOrderDTO.getId())
            .orElseThrow(() -> new BadRequestAlertException("Không tìm thấy đơn hàng", "salesOrder", "id_not_found"));

        //Check quyền trên kho gốc và quyền sở hữu đơn
        validateSalesAccess(oldOrder.getWarehouse().getId());
        checkOrderOwnership(oldOrder);

        if (oldOrder.getStatus() != OrderStatus.DRAFT) {
            throw new BadRequestAlertException(
                "Chỉ được phép chỉnh sửa đơn hàng ở trạng thái NHÁP (DRAFT)!",
                "salesOrder",
                "cannot_edit_processed_order"
            );
        }

        // Chặn đổi Kho xuất
        if (!salesOrderDTO.getWarehouse().getId().equals(oldOrder.getWarehouse().getId())) {
            throw new BadRequestAlertException(
                "Không được phép thay đổi kho xuất của đơn bán hàng đã tạo!",
                "salesOrder",
                "warehouse_immutable"
            );
        }

        salesOrderDTO.setOrderCode(oldOrder.getOrderCode()); // Giữ nguyên mã đơn cũ
        salesOrderDTO.setStatus(OrderStatus.DRAFT); // giữ là Nháp

        // 5. ĐỐI CHIẾU DÒNG TIỀN (Tính lại tổng tiền từ danh sách mặt hàng mới)
        BigDecimal calculatedTotal = BigDecimal.ZERO;
        for (SalesOrderLineDTO line : salesOrderDTO.getSalesOrderLines()) {
            if (line.getUnitPrice() == null || line.getQuantity() == null) {
                throw new BadRequestAlertException(
                    "Mặt hàng không được để trống đơn giá hoặc số lượng!",
                    "salesOrder",
                    "invalid_line_data"
                );
            }
            BigDecimal lineTotal = line.getUnitPrice().multiply(new BigDecimal(line.getQuantity()));
            calculatedTotal = calculatedTotal.add(lineTotal);
        }

        if (salesOrderDTO.getTotalAmount() == null || salesOrderDTO.getTotalAmount().compareTo(calculatedTotal) != 0) {
            throw new BadRequestAlertException(
                "Tổng tiền đơn hàng không hợp lệ! Front-end gửi: " + salesOrderDTO.getTotalAmount() + ", Server tính: " + calculatedTotal,
                "salesOrder",
                "total_amount_mismatch"
            );
        }
        // Lưu Order
        SalesOrder salesOrder = salesOrderMapper.toEntity(salesOrderDTO);
        salesOrder.setEmployee(oldOrder.getEmployee()); // Bắt buộc giữ lại chủ nhân cũ của đơn hàng

        salesOrder = salesOrderRepository.save(salesOrder);

        // Kéo danh sách dòng cũ lên và xóa sạch
        List<SalesOrderLine> oldLines = salesOrderLineRepository.findBySalesOrderId(salesOrder.getId());
        salesOrderLineRepository.deleteAll(oldLines);

        // Lưu danh sách dòng mới
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
                //(Check quyền trên kho gốc và quyền sở hữu đơn)
                validateSalesAccess(existingOrder.getWarehouse().getId());
                checkOrderOwnership(existingOrder);

                if (existingOrder.getStatus() != OrderStatus.DRAFT) {
                    throw new BadRequestAlertException(
                        "Chỉ được phép chỉnh sửa đơn hàng ở trạng thái NHÁP (DRAFT)!",
                        "salesOrder",
                        "cannot_edit_processed_order"
                    );
                }

                //CHẶN THAY ĐỔI CÁC TRƯỜNG CẤM
                if (
                    salesOrderDTO.getWarehouse() != null &&
                    !salesOrderDTO.getWarehouse().getId().equals(existingOrder.getWarehouse().getId())
                ) {
                    throw new BadRequestAlertException(
                        "Không được phép thay đổi kho xuất của đơn bán hàng đã tạo!",
                        "salesOrder",
                        "warehouse_immutable"
                    );
                }
                if (salesOrderDTO.getOrderCode() != null && !salesOrderDTO.getOrderCode().equals(existingOrder.getOrderCode())) {
                    throw new BadRequestAlertException("Không được phép thay đổi mã đơn hàng!", "salesOrder", "order_code_immutable");
                }
                if (salesOrderDTO.getStatus() != null && salesOrderDTO.getStatus() != OrderStatus.DRAFT) {
                    throw new BadRequestAlertException(
                        "Không được đổi trạng thái đơn hàng qua API này!",
                        "salesOrder",
                        "status_change_forbidden"
                    );
                }

                //KIỂM TRA VÀ TÍNH TOÁN LẠI (NẾU CÓ GỬI KÈM MẶT HÀNG MỚI)
                if (salesOrderDTO.getSalesOrderLines() != null) {
                    if (salesOrderDTO.getSalesOrderLines().isEmpty()) {
                        throw new BadRequestAlertException("Đơn bán hàng phải có ít nhất một mặt hàng!", "salesOrder", "empty_lines");
                    }

                    BigDecimal calculatedTotal = BigDecimal.ZERO;
                    for (SalesOrderLineDTO line : salesOrderDTO.getSalesOrderLines()) {
                        if (line.getUnitPrice() == null || line.getQuantity() == null) {
                            throw new BadRequestAlertException(
                                "Mặt hàng không được để trống đơn giá hoặc số lượng!",
                                "salesOrder",
                                "invalid_line_data"
                            );
                        }
                        BigDecimal lineTotal = line.getUnitPrice().multiply(new BigDecimal(line.getQuantity()));
                        calculatedTotal = calculatedTotal.add(lineTotal);
                    }

                    if (salesOrderDTO.getTotalAmount() != null && salesOrderDTO.getTotalAmount().compareTo(calculatedTotal) != 0) {
                        throw new BadRequestAlertException(
                            "Tổng tiền đơn hàng không hợp lệ! Server tính ra: " + calculatedTotal,
                            "salesOrder",
                            "total_amount_mismatch"
                        );
                    }
                    salesOrderDTO.setTotalAmount(calculatedTotal);
                }

                salesOrderMapper.partialUpdate(existingOrder, salesOrderDTO);

                return existingOrder;
            })
            .map(salesOrderRepository::save)
            .map(savedOrder -> {
                // XÓA CŨ - ĐẬP MỚI PHIẾU CON (Chỉ chạy nếu có gửi danh sách mặt hàng)
                if (salesOrderDTO.getSalesOrderLines() != null) {
                    // Kéo danh sách dòng cũ lên và xóa sạch
                    List<SalesOrderLine> oldLines = salesOrderLineRepository.findBySalesOrderId(savedOrder.getId());
                    salesOrderLineRepository.deleteAll(oldLines);

                    // Lưu danh sách dòng mới
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

        //Admin hoặc Kế toán -> Xem tất cả đơn của mọi chi nhánh
        if (isAdmin || isAccountant) {
            if (eager) return salesOrderRepository.findAllWithEagerRelationships(pageable).map(salesOrderMapper::toDto);
            return salesOrderRepository.findAll(pageable).map(salesOrderMapper::toDto);
        }

        String currentUserLogin = SecurityUtils.getCurrentUserLogin().orElseThrow(() -> new RuntimeException("Chưa đăng nhập!"));

        //Quản lý chi nhánh -> CHUYỂN SANG: Chỉ xem đơn thuộc Kho mình phụ trách
        if (isManager) {
            return salesOrderRepository.findAllByEmployeeScopedWarehouse(currentUserLogin, pageable).map(salesOrderMapper::toDto);
        }

        //Nhân viên Sales -> Chỉ xem đơn do chính mình tạo ra
        return salesOrderRepository.findAllByEmployeeUserLogin(currentUserLogin, pageable).map(salesOrderMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<SalesOrderDTO> findOne(Long id) {
        log.debug("Request to get SalesOrder : {}", id);

        boolean isAdmin = SecurityUtils.hasCurrentUserThisAuthority(AuthoritiesConstants.ADMIN);
        boolean isAccountant = SecurityUtils.hasCurrentUserThisAuthority(AuthoritiesConstants.ACCOUNTANT);
        boolean isManager = SecurityUtils.hasCurrentUserThisAuthority(AuthoritiesConstants.MANAGER);

        //Admin, Kế toán
        if (isAdmin || isAccountant) {
            return salesOrderRepository.findOneWithEagerRelationships(id).map(salesOrderMapper::toDto);
        }

        String currentUserLogin = SecurityUtils.getCurrentUserLogin().orElseThrow();

        //Quản lý chi nhánh
        if (isManager) {
            return salesOrderRepository.findOneByIdAndEmployeeScopedWarehouse(id, currentUserLogin).map(salesOrderMapper::toDto);
        }

        //Nhân viên Sales
        return salesOrderRepository.findOneByIdAndEmployeeUserLogin(id, currentUserLogin).map(salesOrderMapper::toDto);
    }

    @Override
    public void delete(Long id) {
        log.debug("Request to delete SalesOrder : {}", id);

        SalesOrder order = salesOrderRepository
            .findById(id)
            .orElseThrow(() -> new BadRequestAlertException("Không tìm thấy đơn hàng", "salesOrder", "id_not_found"));

        //(Check quyền trên kho và quyền sở hữu đơn)
        validateSalesAccess(order.getWarehouse().getId());
        checkOrderOwnership(order);

        //(Chỉ xóa Nháp)
        if (order.getStatus() != OrderStatus.DRAFT) {
            throw new BadRequestAlertException(
                "Đại kỵ! Chỉ được phép xóa vĩnh viễn đơn hàng ở trạng thái NHÁP (DRAFT). Các đơn đã xử lý vui lòng dùng tính năng Hủy (CANCEL) để lưu vết!",
                "salesOrder",
                "cannot_delete_processed_order"
            );
        }

        // Kéo danh sách mặt hàng con lên và xóa trước để chống lỗi khóa ngoại (Foreign Key Constraint)
        List<SalesOrderLine> lines = salesOrderLineRepository.findBySalesOrderId(id);
        salesOrderLineRepository.deleteAll(lines);

        salesOrderRepository.deleteById(id);
    }

    @Transactional
    @Override
    public SalesOrderDTO approveOrder(Long id) {
        log.debug("Request to approve SalesOrder : {}", id);
        SalesOrder order = salesOrderRepository.findById(id).orElseThrow(() -> new RuntimeException("Không tìm thấy đơn"));

        validateSalesAccess(order.getWarehouse().getId());

        if (order.getStatus() == OrderStatus.APPROVED) {
            throw new BadRequestAlertException("Đơn đã duyệt!", "salesOrder", "already_approved");
        }

        order.setStatus(OrderStatus.APPROVED);
        order = salesOrderRepository.save(order);

        // GỌI HÀM XUẤT KHO VÀ KIỂM TRA TỒN
        processOutboundInventory(order);

        String currentLogin = SecurityUtils.getCurrentUserLogin().orElse("System");
        // Chủ nhân đơn hàng (originalCreator) là người nằm trong order.getEmployee()
        String creatorLogin = order.getEmployee().getUser().getLogin();

        // Bắn sự kiện: Duyệt đơn (APPROVED)
        eventPublisher.publishEvent(
            new OrderNotificationEvent("SALES", "APPROVED", order.getId(), order.getOrderCode(), currentLogin, creatorLogin)
        );

        return salesOrderMapper.toDto(order);
    }

    /**
     * Hàm tự động Kiểm tra, Trừ Tồn kho và Ghi Lịch sử khi Đơn bán hàng được duyệt
     */
    private void processOutboundInventory(SalesOrder order) {
        log.debug("Bắt đầu xử lý xuất kho cho Đơn bán hàng: {}", order.getOrderCode());

        List<SalesOrderLine> lines = salesOrderLineRepository.findBySalesOrderId(order.getId());
        if (lines.isEmpty()) return;

        //Gom tất cả ID sản phẩm có trong phiếu
        List<Long> productIds = lines.stream().map(line -> line.getProduct().getId()).collect(Collectors.toList());

        //Kéo toàn bộ tồn kho của Kho đang xuất lên RAM (Map O(1))
        Map<Long, InventoryBalance> balanceMap = inventoryBalanceRepository
            .findByWarehouseIdAndProductIdIn(order.getWarehouse().getId(), productIds)
            .stream()
            .collect(Collectors.toMap(b -> b.getProduct().getId(), b -> b));

        List<InventoryTransaction> transactionsToSave = new ArrayList<>();
        Instant now = Instant.now();

        //Vòng lặp xử lý logic thuần túy trên RAM (Không chọc DB)
        for (SalesOrderLine line : lines) {
            Long productId = line.getProduct().getId();
            InventoryBalance balance = balanceMap.get(productId);

            // Kiểm tra Validation
            if (balance == null) {
                throw new BadRequestAlertException(
                    "Sản phẩm " + line.getProduct().getName() + " không có trong kho " + order.getWarehouse().getName(),
                    "salesOrder",
                    "out_of_stock"
                );
            }

            if (balance.getQuantity() < line.getQuantity()) {
                throw new BadRequestAlertException(
                    "Không đủ hàng! Sản phẩm " + line.getProduct().getName() + " chỉ còn " + balance.getQuantity() + " cái.",
                    "salesOrder",
                    "insufficient_stock"
                );
            }

            // Trừ tồn kho trên RAM
            balance.setQuantity(balance.getQuantity() - line.getQuantity());

            // Ghi nhận Lịch sử giao dịch
            InventoryTransaction transaction = new InventoryTransaction();
            transaction.setType(TransactionType.ISSUE); // Loại hình: XUẤT KHO
            transaction.setQuantity(line.getQuantity());
            transaction.setUnitCost(line.getUnitPrice());
            transaction.setReferenceId(order.getId());
            transaction.setCreatedDate(now);
            transaction.setProduct(line.getProduct());
            transaction.setWarehouse(order.getWarehouse());

            transactionsToSave.add(transaction);
        }

        //Batch Update xuống Database & Xử lý Optimistic Locking
        try {
            inventoryBalanceRepository.saveAll(balanceMap.values());
            inventoryTransactionRepository.saveAll(transactionsToSave);
            inventoryBalanceRepository.flush();
            inventoryTransactionRepository.flush();
        } catch (org.springframework.orm.ObjectOptimisticLockingFailureException e) {
            log.error("Lỗi xung đột dữ liệu (Optimistic Locking) khi xuất kho đơn bán hàng: {}", e.getMessage());
            throw new BadRequestAlertException(
                "Dữ liệu tồn kho của một số mặt hàng đã bị biến động. Vui lòng tải lại trang và thử duyệt lại!",
                "salesOrder",
                "optimistic_locking_inventory_conflict"
            );
        }
    }

    /**
     * Hàm Chốt đơn và Ghi nhận Công nợ Phải Thu
     */
    @Transactional
    @Override
    public SalesOrderDTO completeOrder(Long id) {
        log.debug("Request to complete SalesOrder : {}", id);
        SalesOrder order = salesOrderRepository
            .findById(id)
            .orElseThrow(() -> new BadRequestAlertException("Không tìm thấy đơn bán hàng", "salesOrder", "id_not_found"));

        if (order.getStatus() == OrderStatus.COMPLETED) {
            throw new BadRequestAlertException("Đơn hàng này đã được hoàn thành và chốt sổ trước đó!", "salesOrder", "already_completed");
        }
        if (order.getStatus() != OrderStatus.APPROVED) {
            throw new BadRequestAlertException(
                "Chỉ có thể chốt công nợ cho đơn hàng đã xuất kho (APPROVED)!",
                "salesOrder",
                "invalid_status"
            );
        }

        //XỬ LÝ CÔNG NỢ KHÁCH HÀNG
        Customer customer = order.getCustomer();
        if (customer == null) {
            throw new BadRequestAlertException(
                "Lỗi dữ liệu: Đơn hàng không có thông tin Khách hàng để ghi nợ!",
                "salesOrder",
                "customer_missing"
            );
        }

        BigDecimal currentDebt = customer.getCurrentDebt() != null ? customer.getCurrentDebt() : BigDecimal.ZERO;
        BigDecimal orderTotal = order.getTotalAmount() != null ? order.getTotalAmount() : BigDecimal.ZERO;
        // Cộng dồn tiền đơn hàng vào cục nợ hiện tại của khách
        customer.setCurrentDebt(currentDebt.add(orderTotal));

        order.setStatus(OrderStatus.COMPLETED);

        try {
            customerRepository.save(customer);
            order = salesOrderRepository.save(order);
            customerRepository.flush();
        } catch (org.springframework.orm.ObjectOptimisticLockingFailureException e) {
            log.error("Xung đột dữ liệu khi cập nhật công nợ khách hàng: {}", e.getMessage());
            throw new BadRequestAlertException(
                "Công nợ của khách hàng này vừa bị biến động bởi một giao dịch khác. Vui lòng thử lại!",
                "salesOrder",
                "optimistic_locking_customer_debt"
            );
        }

        // 5. BẮN THÔNG BÁO HỆ THỐNG
        String currentLogin = SecurityUtils.getCurrentUserLogin().orElse("System");
        String creatorLogin = order.getEmployee().getUser().getLogin();

        eventPublisher.publishEvent(
            new OrderNotificationEvent("SALES", "COMPLETED", order.getId(), order.getOrderCode(), currentLogin, creatorLogin)
        );

        return salesOrderMapper.toDto(order);
    }

    /**
     * Hàm Hủy Đơn Bán Hàng và dọn dẹp dữ liệu (Hoàn kho, Xóa nợ)
     */
    @Transactional
    @Override
    public SalesOrderDTO cancelOrder(Long id) {
        log.debug("Request to cancel SalesOrder : {}", id);

        SalesOrder order = salesOrderRepository
            .findById(id)
            .orElseThrow(() -> new BadRequestAlertException("Không tìm thấy đơn bán hàng", "salesOrder", "id_not_found"));

        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new BadRequestAlertException("Đơn hàng này đã bị hủy từ trước!", "salesOrder", "already_cancelled");
        }

        // --- BẮT ĐẦU: PHÂN QUYỀN ĐỘNG THEO TRẠNG THÁI ---
        boolean isAdmin = SecurityUtils.hasCurrentUserThisAuthority(AuthoritiesConstants.ADMIN);
        boolean isAccountant = SecurityUtils.hasCurrentUserThisAuthority(AuthoritiesConstants.ACCOUNTANT);
        boolean isManager = SecurityUtils.hasCurrentUserThisAuthority(AuthoritiesConstants.MANAGER);

        if (order.getStatus() == OrderStatus.COMPLETED) {
            // Đã chốt nợ -> Lãnh địa của Kế toán
            if (!isAdmin && !isAccountant) {
                throw new BadRequestAlertException(
                    "Đơn hàng đã được Kế toán chốt sổ công nợ. Quản lý chi nhánh không được phép tự ý hủy, vui lòng liên hệ phòng Kế toán!",
                    "salesOrder",
                    "accountant_required_for_cancel"
                );
            }
        } else {
            // Chưa chốt nợ (DRAFT hoặc APPROVED) -> Lãnh địa của Quản lý chi nhánh
            if (!isAdmin && !isManager) {
                throw new BadRequestAlertException(
                    "Chỉ Quản lý chi nhánh hoặc Admin mới có quyền hủy đơn hàng ở giai đoạn này!",
                    "salesOrder",
                    "manager_required_for_cancel"
                );
            }
            // Nếu là Manager, bắt buộc phải check xem có đúng chi nhánh của mình không
            if (isManager) {
                validateSalesAccess(order.getWarehouse().getId());
            }
        }
        //ĐẢO NGƯỢC CÔNG NỢ (Nếu đã COMPLETED)
        if (order.getStatus() == OrderStatus.COMPLETED) {
            Customer customer = order.getCustomer();
            if (customer != null) {
                BigDecimal currentDebt = customer.getCurrentDebt() != null ? customer.getCurrentDebt() : BigDecimal.ZERO;
                BigDecimal orderTotal = order.getTotalAmount() != null ? order.getTotalAmount() : BigDecimal.ZERO;

                // Trừ đi cục nợ đã lỡ cộng trước đó
                customer.setCurrentDebt(currentDebt.subtract(orderTotal));

                try {
                    customerRepository.save(customer);
                    customerRepository.flush();
                } catch (org.springframework.orm.ObjectOptimisticLockingFailureException e) {
                    log.error("Xung đột dữ liệu khi hoàn nợ do hủy đơn: {}", e.getMessage());
                    throw new BadRequestAlertException(
                        "Không thể hoàn nợ do dữ liệu Khách hàng đang bị biến động. Vui lòng thử lại!",
                        "salesOrder",
                        "optimistic_locking_customer_debt"
                    );
                }
            }
        }

        //ĐẢO NGƯỢC TỒN KHO (Nếu đã APPROVED hoặc COMPLETED)
        if (order.getStatus() == OrderStatus.APPROVED || order.getStatus() == OrderStatus.COMPLETED) {
            List<SalesOrderLine> lines = salesOrderLineRepository.findBySalesOrderId(order.getId());

            if (!lines.isEmpty()) {
                List<Long> productIds = lines.stream().map(line -> line.getProduct().getId()).collect(Collectors.toList());
                Map<Long, InventoryBalance> balanceMap = inventoryBalanceRepository
                    .findByWarehouseIdAndProductIdIn(order.getWarehouse().getId(), productIds)
                    .stream()
                    .collect(Collectors.toMap(b -> b.getProduct().getId(), b -> b));

                List<InventoryTransaction> returnTransactions = new ArrayList<>();
                Instant now = Instant.now();

                for (SalesOrderLine line : lines) {
                    InventoryBalance balance = balanceMap.get(line.getProduct().getId());
                    if (balance == null) {
                        throw new BadRequestAlertException(
                            "Không tìm thấy dòng tồn kho để hoàn trả cho sản phẩm ID: " + line.getProduct().getId(),
                            "salesOrder",
                            "inventory_not_found"
                        );
                    }

                    // Cộng lại số lượng hàng
                    balance.setQuantity(balance.getQuantity() + line.getQuantity());

                    // Ghi lại lịch sử (Loại RECEIPT - Nhập lại hàng do hủy đơn)
                    InventoryTransaction returnTx = new InventoryTransaction();
                    returnTx.setType(TransactionType.RECEIPT);
                    returnTx.setQuantity(line.getQuantity());
                    returnTx.setUnitCost(line.getUnitPrice()); // Hoàn lại theo đúng giá trị lúc xuất
                    returnTx.setReferenceId(order.getId());
                    returnTx.setCreatedDate(now);
                    returnTx.setProduct(line.getProduct());
                    returnTx.setWarehouse(order.getWarehouse());

                    returnTransactions.add(returnTx);
                }

                try {
                    inventoryBalanceRepository.saveAll(balanceMap.values());
                    inventoryTransactionRepository.saveAll(returnTransactions);

                    inventoryBalanceRepository.flush();
                    inventoryTransactionRepository.flush();
                } catch (org.springframework.orm.ObjectOptimisticLockingFailureException e) {
                    log.error("Lỗi xung đột dữ liệu khi hoàn kho do hủy đơn: {}", e.getMessage());
                    throw new BadRequestAlertException(
                        "Không thể hoàn kho do dữ liệu tồn kho đang bị khóa. Vui lòng thử lại!",
                        "salesOrder",
                        "optimistic_locking_inventory_conflict"
                    );
                }
            }
        }

        //CHUYỂN TRẠNG THÁI VÀ LƯU PHIẾU
        order.setStatus(OrderStatus.CANCELLED);
        order = salesOrderRepository.save(order);

        //BẮN SỰ KIỆN THÔNG BÁO
        String currentLogin = SecurityUtils.getCurrentUserLogin().orElse("System");
        String creatorLogin = order.getEmployee().getUser().getLogin();

        eventPublisher.publishEvent(
            new OrderNotificationEvent("SALES", "CANCELLED", order.getId(), order.getOrderCode(), currentLogin, creatorLogin)
        );

        return salesOrderMapper.toDto(order);
    }

    /**
     * Gác cổng Phòng ban và Chi nhánh
     * Lưu ý: Đầu vào warehouseId phải luôn khác null (Đã check ở hàm gọi nó).
     * Trả về Employee để lưu vết người tạo đơn.
     */
    private Employee validateSalesAccess(Long warehouseId) {
        String currentUserLogin = SecurityUtils.getCurrentUserLogin().orElseThrow(() -> new RuntimeException("Chưa đăng nhập!"));

        Employee employee = employeeRepository
            .findByUserLogin(currentUserLogin)
            .orElseThrow(() ->
                new BadRequestAlertException("Tài khoản của bạn chưa được gắn với hồ sơ nhân viên nào!", "salesOrder", "employee_not_found")
            );

        //ADMIN -> Thông chốt toàn bộ, nhưng vẫn trả về hồ sơ Employee để lưu vết
        if (SecurityUtils.hasCurrentUserThisAuthority(AuthoritiesConstants.ADMIN)) {
            return employee;
        }

        //CHECK PHÒNG BAN: Bắt buộc phải là nhân viên SALES
        if (
            employee.getDepartment() == null ||
            employee.getDepartment().getName() != com.mycompany.myapp.domain.enumeration.DepartmentName.SALES
        ) {
            throw new BadRequestAlertException("Tài khoản của bạn không thuộc phòng Kinh doanh!", "salesOrder", "invalid_department");
        }

        //CHECK CHI NHÁNH/KHO:
        if (employee.getScopedWarehouse() == null) {
            throw new BadRequestAlertException(
                "Tài khoản của bạn chưa được phân công về chi nhánh/kho nào!",
                "salesOrder",
                "no_scoped_warehouse"
            );
        }

        if (!employee.getScopedWarehouse().getId().equals(warehouseId)) {
            throw new BadRequestAlertException(
                "Bạn không có quyền thao tác trên đơn hàng của chi nhánh khác!",
                "salesOrder",
                "warehouse_access_denied"
            );
        }

        return employee;
    }

    /**
     * Chặn Sales sửa/xóa đơn của người khác (Manager và Admin được phép)
     */
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
                    "Bạn không có quyền thao tác trên đơn bán hàng của người khác!",
                    "salesOrder",
                    "access_denied"
                );
            }
        }
    }
}
