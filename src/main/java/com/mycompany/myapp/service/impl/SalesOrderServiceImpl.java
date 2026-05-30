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

        //Gọi hàm gác cổng để kiểm tra phòng ban, kho xuất và lấy hồ sơ nhân viên lập đơn
        Employee currentEmployee = validateSalesAccess(salesOrderDTO.getWarehouse().getId());
        if (currentEmployee == null) {
            throw new BadRequestAlertException(
                "Hệ thống không thể xác định hồ sơ nhân viên hợp lệ để lập đơn!",
                "salesOrder",
                "employee_required"
            );
        }

        // LỚP BẢO VỆ 4: Chặn đơn hàng trống (Không có danh sách sản phẩm con)
        if (salesOrderDTO.getSalesOrderLines() == null || salesOrderDTO.getSalesOrderLines().isEmpty()) {
            throw new BadRequestAlertException("Đơn bán hàng phải có ít nhất một mặt hàng!", "salesOrder", "empty_lines");
        }

        // 5. Ép trạng thái đơn về DRAFT (Nháp) khi vừa tạo mới
        salesOrderDTO.setStatus(OrderStatus.DRAFT);

        // 6. Tính toán lại Tổng tiền thực tế dựa trên danh sách mặt hàng con gửi lên
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

        // 7. KIỂM TRA ĐỐI CHIẾU TỔNG TIỀN (Fail-Fast chống gian lận giá từ Front-end)
        if (salesOrderDTO.getTotalAmount() == null || salesOrderDTO.getTotalAmount().compareTo(calculatedTotal) != 0) {
            throw new BadRequestAlertException(
                "Tổng tiền đơn hàng không hợp lệ! Front-end gửi: " + salesOrderDTO.getTotalAmount() + ", Server tính: " + calculatedTotal,
                "salesOrder",
                "total_amount_mismatch"
            );
        }

        // 8. Ánh xạ dữ liệu sang Entity cha và gắn chặt thông tin Nhân viên lập đơn vào
        SalesOrder salesOrder = salesOrderMapper.toEntity(salesOrderDTO);
        salesOrder.setEmployee(currentEmployee); // Chân kiềng Employee được khóa tại đây

        // Lưu phiếu cha để Database sinh ID tự động
        salesOrder = salesOrderRepository.save(salesOrder);

        // 9. Duyệt danh sách mặt hàng con và lưu hàng loạt (Batch Insert)
        List<SalesOrderLine> linesToSave = new ArrayList<>();
        for (SalesOrderLineDTO lineDTO : salesOrderDTO.getSalesOrderLines()) {
            SalesOrderLine line = salesOrderLineMapper.toEntity(lineDTO);
            line.setSalesOrder(salesOrder); // Trỏ khóa ngoại chính xác về ID phiếu cha vừa sinh
            linesToSave.add(line);
        }
        salesOrderLineRepository.saveAll(linesToSave);

        SalesOrderDTO result = salesOrderMapper.toDto(salesOrder);

        // 10. Bắn sự kiện thông báo hệ thống
        String currentLogin = SecurityUtils.getCurrentUserLogin().orElse("System");
        eventPublisher.publishEvent(
            new OrderNotificationEvent("SALES", "CREATED", result.getId(), result.getOrderCode(), currentLogin, currentLogin)
        );

        return result;
    }

    @Override
    public SalesOrderDTO update(SalesOrderDTO salesOrderDTO) {
        log.debug("Request to update SalesOrder : {}", salesOrderDTO);
        SalesOrder oldOrder = salesOrderRepository.findById(salesOrderDTO.getId()).orElseThrow();
        if (oldOrder.getStatus() != salesOrderDTO.getStatus()) {
            throw new BadRequestAlertException("Không đổi trạng thái ở đây!", "salesOrder", "forbidden");
        }
        SalesOrder salesOrder = salesOrderMapper.toEntity(salesOrderDTO);
        salesOrder = salesOrderRepository.save(salesOrder);
        return salesOrderMapper.toDto(salesOrder);
    }

    @Override
    public Optional<SalesOrderDTO> partialUpdate(SalesOrderDTO salesOrderDTO) {
        log.debug("Request to partially update SalesOrder : {}", salesOrderDTO);

        return salesOrderRepository
            .findById(salesOrderDTO.getId())
            .map(old -> {
                if (salesOrderDTO.getStatus() != null && old.getStatus() != salesOrderDTO.getStatus()) {
                    throw new BadRequestAlertException("Không đổi trạng thái ở đây!", "salesOrder", "forbidden");
                }
                salesOrderMapper.partialUpdate(old, salesOrderDTO);
                return old;
            })
            .map(salesOrderRepository::save)
            .map(salesOrderMapper::toDto);
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

        if (isAdmin || isAccountant) {
            return salesOrderRepository.findOneWithEagerRelationships(id).map(salesOrderMapper::toDto);
        }

        String currentUserLogin = SecurityUtils.getCurrentUserLogin().orElseThrow();

        //Quản lý chi nhánh -> CHUYỂN SANG: Check xem đơn có thuộc kho mình quản lý không
        if (isManager) {
            return salesOrderRepository.findOneByIdAndEmployeeScopedWarehouse(id, currentUserLogin).map(salesOrderMapper::toDto);
        }

        //Nhân viên Sales -> Chỉ được xem đơn của chính mình
        Optional<SalesOrder> orderOpt = salesOrderRepository.findOneWithEagerRelationships(id);
        return orderOpt
            .filter(order ->
                order.getEmployee() != null &&
                order.getEmployee().getUser() != null &&
                currentUserLogin.equals(order.getEmployee().getUser().getLogin())
            )
            .map(salesOrderMapper::toDto);
    }

    @Override
    public void delete(Long id) {
        log.debug("Request to delete SalesOrder : {}", id);

        // 1. Kéo dữ liệu lên để kiểm tra trước khi trảm
        SalesOrder order = salesOrderRepository
            .findById(id)
            .orElseThrow(() -> new BadRequestAlertException("Không tìm thấy đơn hàng", "salesOrder", "id_not_found"));

        // 2. Chặn đứng hành vi xóa chứng từ đã duyệt
        if (order.getStatus() != OrderStatus.DRAFT) {
            throw new BadRequestAlertException(
                "Đại kỵ! Chỉ được xóa vĩnh viễn đơn hàng ở trạng thái NHÁP (DRAFT). Các đơn đã xử lý vui lòng dùng tính năng HỦY (CANCEL) để lưu vết hệ thống!",
                "salesOrder",
                "cannot_delete_processed_order"
            );
        }

        // 3. Phân quyền: Cấm Sales xóa đơn của người khác (Dù là DRAFT)
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
                throw new BadRequestAlertException("Bạn không có quyền xóa đơn nháp của người khác!", "salesOrder", "access_denied");
            }
        }

        // 4. Nếu thỏa mãn hết điều kiện an toàn, mới cho phép xóa cứng (Dọn rác)
        salesOrderRepository.deleteById(id);
    }

    @Override
    // Hàm Duyệt đơn (Đừng quên khai báo trong Interface SalesOrderService nhé)
    public SalesOrderDTO approveOrder(Long id) {
        SalesOrder order = salesOrderRepository.findById(id).orElseThrow(() -> new RuntimeException("Không tìm thấy đơn"));

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
    @Override
    public SalesOrderDTO completeOrder(Long id) {
        log.debug("Request to complete SalesOrder : {}", id);

        SalesOrder order = salesOrderRepository.findById(id).orElseThrow(() -> new RuntimeException("Không tìm thấy đơn bán hàng"));

        // 1. Kiểm tra trạng thái (Chỉ đơn Đã duyệt mới được Hoàn thành)
        if (order.getStatus() == OrderStatus.COMPLETED) {
            throw new BadRequestAlertException("Đơn hàng này đã được hoàn thành trước đó!", "salesOrder", "already_completed");
        }
        if (order.getStatus() != OrderStatus.APPROVED) {
            throw new BadRequestAlertException("Chỉ có thể hoàn thành đơn hàng đã duyệt (APPROVED)!", "salesOrder", "invalid_status");
        }

        // 2. Chuyển trạng thái
        order.setStatus(OrderStatus.COMPLETED);
        order = salesOrderRepository.save(order);

        // 3. Ghi nợ cho Khách hàng
        Customer customer = order.getCustomer();
        if (customer != null) {
            // Xử lý chống NullPointerException
            BigDecimal currentDebt = customer.getCurrentDebt() != null ? customer.getCurrentDebt() : BigDecimal.ZERO;
            BigDecimal orderTotal = order.getTotalAmount() != null ? order.getTotalAmount() : BigDecimal.ZERO;

            // Nợ mới = Nợ cũ + Tiền đơn hàng
            customer.setCurrentDebt(currentDebt.add(orderTotal));
            customerRepository.save(customer);
        }
        String currentLogin = SecurityUtils.getCurrentUserLogin().orElse("System");
        String creatorLogin = order.getEmployee().getUser().getLogin();

        // Cho hàm completeOrder:
        eventPublisher.publishEvent(
            new OrderNotificationEvent("SALES", "COMPLETED", order.getId(), order.getOrderCode(), currentLogin, creatorLogin)
        );

        return salesOrderMapper.toDto(order);
    }

    /**
     * Hàm Hủy Đơn Bán Hàng và dọn dẹp dữ liệu (Hoàn kho, Xóa nợ)
     */
    @Override
    public SalesOrderDTO cancelOrder(Long id) {
        log.debug("Request to cancel SalesOrder : {}", id);
        SalesOrder order = salesOrderRepository.findById(id).orElseThrow(() -> new RuntimeException("Không tìm thấy đơn bán hàng"));
        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new BadRequestAlertException("Đơn hàng này đã bị hủy từ trước!", "salesOrder", "already_cancelled");
        }
        //NẾU ĐƠN ĐÃ COMPLETED -> PHẢI XÓA NỢ CHO KHÁCH HÀNG
        if (order.getStatus() == OrderStatus.COMPLETED) {
            Customer customer = order.getCustomer();
            if (customer != null) {
                BigDecimal currentDebt = customer.getCurrentDebt() != null ? customer.getCurrentDebt() : BigDecimal.ZERO;
                BigDecimal orderTotal = order.getTotalAmount() != null ? order.getTotalAmount() : BigDecimal.ZERO;
                // Trừ đi cục nợ đã lỡ cộng trước đó
                customer.setCurrentDebt(currentDebt.subtract(orderTotal));
                customerRepository.save(customer);
            }
        }

        //NẾU ĐƠN ĐÃ APPROVED HOẶC COMPLETED -> PHẢI HOÀN TRẢ LẠI TỒN KHO
        if (order.getStatus() == OrderStatus.APPROVED || order.getStatus() == OrderStatus.COMPLETED) {
            List<SalesOrderLine> lines = salesOrderLineRepository.findBySalesOrderId(order.getId());

            if (!lines.isEmpty()) {
                // Gom ID sản phẩm
                List<Long> productIds = lines.stream().map(line -> line.getProduct().getId()).collect(Collectors.toList());
                // Kéo tồn kho lên RAM
                Map<Long, InventoryBalance> balanceMap = inventoryBalanceRepository
                    .findByWarehouseIdAndProductIdIn(order.getWarehouse().getId(), productIds)
                    .stream()
                    .collect(Collectors.toMap(b -> b.getProduct().getId(), b -> b));

                List<InventoryTransaction> returnTransactions = new ArrayList<>();
                Instant now = Instant.now();

                for (SalesOrderLine line : lines) {
                    Long productId = line.getProduct().getId();
                    InventoryBalance balance = balanceMap.get(productId);

                    if (balance == null) {
                        throw new RuntimeException("Không tìm thấy dòng tồn kho để hoàn trả cho sản phẩm ID: " + productId);
                    }

                    // Cộng lại số lượng hàng khách trả về
                    balance.setQuantity(balance.getQuantity() + line.getQuantity());

                    // Ghi lại lịch sử (Loại RECEIPT - Nhập lại hàng do hủy đơn)
                    InventoryTransaction returnTx = new InventoryTransaction();
                    returnTx.setType(TransactionType.RECEIPT);
                    returnTx.setQuantity(line.getQuantity());
                    returnTx.setReferenceId(order.getId());
                    returnTx.setCreatedDate(now);
                    returnTx.setProduct(line.getProduct());
                    returnTx.setWarehouse(order.getWarehouse());

                    returnTransactions.add(returnTx);
                }

                // Batch Update DB
                try {
                    inventoryBalanceRepository.saveAll(balanceMap.values());
                    inventoryTransactionRepository.saveAll(returnTransactions);
                    inventoryBalanceRepository.flush();
                    inventoryTransactionRepository.flush();
                } catch (org.springframework.orm.ObjectOptimisticLockingFailureException e) {
                    log.error("Lỗi xung đột dữ liệu khi hoàn kho do hủy đơn: {}", e.getMessage());
                    throw new BadRequestAlertException(
                        "Không thể hoàn kho do dữ liệu tồn kho đang bị khóa bởi giao dịch khác. Vui lòng thử lại!",
                        "salesOrder",
                        "optimistic_locking_inventory_conflict"
                    );
                }
            }
        }

        //ĐỔI TRẠNG THÁI THÀNH CANCELLED
        order.setStatus(OrderStatus.CANCELLED);
        order = salesOrderRepository.save(order);

        String currentLogin = SecurityUtils.getCurrentUserLogin().orElse("System");
        String creatorLogin = order.getEmployee().getUser().getLogin();
        eventPublisher.publishEvent(
            new OrderNotificationEvent("SALES", "CANCELLED", order.getId(), order.getOrderCode(), currentLogin, creatorLogin)
        );

        return salesOrderMapper.toDto(order);
    }

    /**
     * Gác cổng Phòng ban và Chi nhánh
     * BẮT BUỘC trả về Employee (kể cả Admin) để lưu vết người tạo đơn.
     */
    private Employee validateSalesAccess(Long warehouseId) {
        String currentUserLogin = SecurityUtils.getCurrentUserLogin().orElseThrow(() -> new RuntimeException("Chưa đăng nhập!"));

        Employee employee = employeeRepository
            .findByUserLogin(currentUserLogin)
            .orElseThrow(() ->
                new BadRequestAlertException("Tài khoản của bạn chưa được gắn với hồ sơ nhân viên nào!", "salesOrder", "employee_not_found")
            );

        // Nếu là ADMIN -> Trả về luôn Employee để gán vào đơn, BỎ QUA check phòng ban và kho
        if (SecurityUtils.hasCurrentUserThisAuthority(AuthoritiesConstants.ADMIN)) {
            return employee;
        }

        //CHECK PHÒNG BAN: Phải là người của Phòng Kinh doanh
        if (
            employee.getDepartment() == null ||
            employee.getDepartment().getName() != com.mycompany.myapp.domain.enumeration.DepartmentName.SALES
        ) {
            throw new BadRequestAlertException(
                "Tài khoản của bạn không thuộc phòng Kinh doanh. Bạn không có quyền thực hiện thao tác này!",
                "salesOrder",
                "invalid_department_approval"
            );
        }

        //Đơn hàng phải có kho, và Nhân viên cũng phải có kho
        if (warehouseId == null) {
            throw new BadRequestAlertException("Đơn bán hàng bắt buộc phải chọn kho xuất!", "salesOrder", "warehouse_required");
        }

        if (employee.getScopedWarehouse() == null) {
            throw new BadRequestAlertException(
                "Tài khoản của bạn chưa được phân công về chi nhánh/kho nào. Vui lòng liên hệ quản lý!",
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
