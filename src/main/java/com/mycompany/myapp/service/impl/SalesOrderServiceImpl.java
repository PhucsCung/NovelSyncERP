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
import java.util.Optional;
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

    public SalesOrderServiceImpl(
        SalesOrderRepository salesOrderRepository,
        SalesOrderMapper salesOrderMapper,
        SalesOrderLineRepository salesOrderLineRepository,
        InventoryTransactionRepository inventoryTransactionRepository,
        InventoryBalanceRepository inventoryBalanceRepository,
        SalesOrderLineMapper salesOrderLineMapper,
        CustomerRepository customerRepository,
        ApplicationEventPublisher eventPublisher
    ) {
        this.salesOrderRepository = salesOrderRepository;
        this.salesOrderMapper = salesOrderMapper;
        this.salesOrderLineRepository = salesOrderLineRepository;
        this.inventoryTransactionRepository = inventoryTransactionRepository;
        this.inventoryBalanceRepository = inventoryBalanceRepository;
        this.salesOrderLineMapper = salesOrderLineMapper;
        this.customerRepository = customerRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public SalesOrderDTO save(SalesOrderDTO salesOrderDTO) {
        log.debug("Request to save SalesOrder : {}", salesOrderDTO);
        // 1. Chặn đơn rỗng
        if (salesOrderDTO.getSalesOrderLines() == null || salesOrderDTO.getSalesOrderLines().isEmpty()) {
            throw new BadRequestAlertException("Đơn bán hàng phải có mặt hàng!", "salesOrder", "empty_lines");
        }

        // 2. Ép về DRAFT
        salesOrderDTO.setStatus(OrderStatus.DRAFT);

        // 3. Tự tính lại Tổng tiền
        BigDecimal calculatedTotal = BigDecimal.ZERO;
        for (SalesOrderLineDTO line : salesOrderDTO.getSalesOrderLines()) {
            BigDecimal lineTotal = line.getUnitPrice().multiply(new BigDecimal(line.getQuantity()));
            calculatedTotal = calculatedTotal.add(lineTotal);
        }
        salesOrderDTO.setTotalAmount(calculatedTotal);

        // 4. Lưu Cha
        SalesOrder salesOrder = salesOrderMapper.toEntity(salesOrderDTO);
        salesOrder = salesOrderRepository.save(salesOrder);

        // 5. Lưu Con (Batch)
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

    // Tách ra 1 hàm dùng chung cho code đỡ dài và sạch sẽ:
    private Page<SalesOrderDTO> getFilteredSalesOrders(Pageable pageable, boolean eager) {
        boolean isAdmin = SecurityUtils.hasCurrentUserThisAuthority(AuthoritiesConstants.ADMIN);
        boolean isAccountant = SecurityUtils.hasCurrentUserThisAuthority(AuthoritiesConstants.ACCOUNTANT);
        boolean isManager = SecurityUtils.hasCurrentUserThisAuthority(AuthoritiesConstants.MANAGER);

        // TẦNG 1: Admin hoặc Kế toán -> Xem tất
        if (isAdmin || isAccountant) {
            if (eager) return salesOrderRepository.findAllWithEagerRelationships(pageable).map(salesOrderMapper::toDto);
            return salesOrderRepository.findAll(pageable).map(salesOrderMapper::toDto);
        }

        String currentUserLogin = SecurityUtils.getCurrentUserLogin().orElseThrow(() -> new RuntimeException("Chưa đăng nhập!"));

        // TẦNG 2: Trưởng phòng -> Xem của cả phòng
        if (isManager) {
            return salesOrderRepository.findAllByEmployeeDepartment(currentUserLogin, pageable).map(salesOrderMapper::toDto);
        }

        // TẦNG 3: Sales (nhân viên quèn) -> Chỉ xem của mình
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
            return salesOrderRepository.findOneByEmployeeDepartment(id, currentUserLogin).map(salesOrderMapper::toDto);
        }

        // Với Sales: Dùng filter để chặn
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

        for (SalesOrderLine line : lines) {
            // 1. TÌM VÀ KIỂM TRA TỒN KHO CỤC BỘ (Validation)
            InventoryBalance balance = inventoryBalanceRepository
                .findOneByProductIdAndWarehouseId(line.getProduct().getId(), order.getWarehouse().getId())
                .orElseThrow(() ->
                    new BadRequestAlertException(
                        "Sản phẩm " + line.getProduct().getName() + " không có trong kho " + order.getWarehouse().getName(),
                        "salesOrder",
                        "out_of_stock"
                    )
                );

            // Nếu số lượng trong kho ít hơn số lượng muốn bán -> Bắn lỗi, Rollback toàn bộ!
            if (balance.getQuantity() < line.getQuantity()) {
                throw new BadRequestAlertException(
                    "Không đủ hàng! Sản phẩm " + line.getProduct().getName() + " chỉ còn " + balance.getQuantity() + " cái.",
                    "salesOrder",
                    "insufficient_stock"
                );
            }

            // 2. Trừ tồn kho
            balance.setQuantity(balance.getQuantity() - line.getQuantity());
            inventoryBalanceRepository.save(balance);

            // 3. Ghi nhận Lịch sử giao dịch
            InventoryTransaction transaction = new InventoryTransaction();
            transaction.setType(TransactionType.ISSUE); // Loại hình: XUẤT KHO
            transaction.setQuantity(line.getQuantity()); // Có thể lưu số âm (-10) hoặc dương tùy quy ước của bác
            transaction.setUnitCost(line.getUnitPrice()); // (Thực tế ERP sẽ tính theo FIFO/Bình quân ở đây)
            transaction.setReferenceId(order.getId());
            transaction.setCreatedDate(Instant.now());
            transaction.setProduct(line.getProduct());
            transaction.setWarehouse(order.getWarehouse());

            inventoryTransactionRepository.save(transaction);
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

        // 1. NẾU ĐƠN ĐÃ COMPLETED -> PHẢI XÓA NỢ CHO KHÁCH HÀNG
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

        // 2. NẾU ĐƠN ĐÃ APPROVED HOẶC COMPLETED -> PHẢI HOÀN TRẢ LẠI TỒN KHO
        if (order.getStatus() == OrderStatus.APPROVED || order.getStatus() == OrderStatus.COMPLETED) {
            List<SalesOrderLine> lines = salesOrderLineRepository.findBySalesOrderId(order.getId());

            for (SalesOrderLine line : lines) {
                // Tìm lại dòng tồn kho
                InventoryBalance balance = inventoryBalanceRepository
                    .findOneByProductIdAndWarehouseId(line.getProduct().getId(), order.getWarehouse().getId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy dòng tồn kho để hoàn trả"));

                // Cộng lại số lượng hàng khách trả về
                balance.setQuantity(balance.getQuantity() + line.getQuantity());
                inventoryBalanceRepository.save(balance);

                // Ghi lại lịch sử (Loại RECEIPT - Nhập lại hàng do hủy đơn)
                InventoryTransaction returnTx = new InventoryTransaction();
                returnTx.setType(TransactionType.RECEIPT); // Nhập kho
                returnTx.setQuantity(line.getQuantity());
                returnTx.setReferenceId(order.getId());
                returnTx.setCreatedDate(Instant.now());
                returnTx.setProduct(line.getProduct());
                returnTx.setWarehouse(order.getWarehouse());
                // Ghi chú thêm để thủ kho biết đây là hàng hoàn trả
                // returnTx.setNote("Hoàn trả hàng do hủy đơn");
                inventoryTransactionRepository.save(returnTx);
            }
        }

        // 3. CUỐI CÙNG: ĐỔI TRẠNG THÁI THÀNH CANCELLED
        order.setStatus(OrderStatus.CANCELLED);
        order = salesOrderRepository.save(order);

        String currentLogin = SecurityUtils.getCurrentUserLogin().orElse("System");
        String creatorLogin = order.getEmployee().getUser().getLogin();
        eventPublisher.publishEvent(
            new OrderNotificationEvent("SALES", "CANCELLED", order.getId(), order.getOrderCode(), currentLogin, creatorLogin)
        );

        return salesOrderMapper.toDto(order);
    }
}
