package com.mycompany.myapp.service.impl;

import static org.hibernate.id.IdentifierGenerator.ENTITY_NAME;

import com.mycompany.myapp.domain.*;
import com.mycompany.myapp.domain.enumeration.OrderStatus;
import com.mycompany.myapp.domain.enumeration.TransactionType;
import com.mycompany.myapp.repository.*;
import com.mycompany.myapp.security.AuthoritiesConstants;
import com.mycompany.myapp.security.SecurityUtils;
import com.mycompany.myapp.service.PurchaseOrderService;
import com.mycompany.myapp.service.dto.PurchaseOrderDTO;
import com.mycompany.myapp.service.dto.PurchaseOrderLineDTO;
import com.mycompany.myapp.service.event.OrderNotificationEvent;
import com.mycompany.myapp.service.mapper.PurchaseOrderLineMapper;
import com.mycompany.myapp.service.mapper.PurchaseOrderMapper;
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
 * Service Implementation for managing {@link PurchaseOrder}.
 */
@Service
@Transactional
public class PurchaseOrderServiceImpl implements PurchaseOrderService {

    private final Logger log = LoggerFactory.getLogger(PurchaseOrderServiceImpl.class);

    private final PurchaseOrderRepository purchaseOrderRepository;

    private final PurchaseOrderMapper purchaseOrderMapper;

    private final PurchaseOrderLineRepository purchaseOrderLineRepository;

    private final InventoryTransactionRepository inventoryTransactionRepository;

    private final InventoryBalanceRepository inventoryBalanceRepository;

    private final PurchaseOrderLineMapper purchaseOrderLineMapper;

    private final SupplierRepository supplierRepository;

    private final ApplicationEventPublisher eventPublisher;

    public PurchaseOrderServiceImpl(
        PurchaseOrderRepository purchaseOrderRepository,
        PurchaseOrderMapper purchaseOrderMapper,
        PurchaseOrderLineRepository purchaseOrderLineRepository,
        InventoryTransactionRepository inventoryTransactionRepository,
        InventoryBalanceRepository inventoryBalanceRepository,
        PurchaseOrderLineMapper purchaseOrderLineMapper,
        SupplierRepository supplierRepository,
        ApplicationEventPublisher eventPublisher
    ) {
        this.purchaseOrderRepository = purchaseOrderRepository;
        this.purchaseOrderMapper = purchaseOrderMapper;
        this.purchaseOrderLineRepository = purchaseOrderLineRepository;
        this.inventoryTransactionRepository = inventoryTransactionRepository;
        this.inventoryBalanceRepository = inventoryBalanceRepository;
        this.purchaseOrderLineMapper = purchaseOrderLineMapper;
        this.supplierRepository = supplierRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public PurchaseOrderDTO save(PurchaseOrderDTO purchaseOrderDTO) {
        log.debug("Request to save PurchaseOrder : {}", purchaseOrderDTO);
        if (purchaseOrderDTO.getPurchaseOrderLines() == null || purchaseOrderDTO.getPurchaseOrderLines().isEmpty()) {
            throw new BadRequestAlertException("Đơn mua hàng phải có ít nhất 1 mặt hàng!", ENTITY_NAME, "empty_order_lines");
        }
        // 1. CHẶN BẢO MẬT: Ép về DRAFT
        purchaseOrderDTO.setStatus(OrderStatus.DRAFT);

        BigDecimal calculatedTotal = BigDecimal.ZERO;
        if (purchaseOrderDTO.getPurchaseOrderLines() != null) {
            for (PurchaseOrderLineDTO line : purchaseOrderDTO.getPurchaseOrderLines()) {
                // Tiền 1 dòng = Số lượng * Đơn giá
                BigDecimal lineTotal = line.getUnitPrice().multiply(new BigDecimal(line.getQuantity()));
                calculatedTotal = calculatedTotal.add(lineTotal);
            }
        }
        // Ghi đè lại tổng tiền bằng số hệ thống tự tính
        purchaseOrderDTO.setTotalAmount(calculatedTotal);

        // 2. Lưu Phiếu nhập (Thẻ Cha) để DB cấp cho nó 1 cái ID
        PurchaseOrder purchaseOrder = purchaseOrderMapper.toEntity(purchaseOrderDTO);
        purchaseOrder = purchaseOrderRepository.save(purchaseOrder);

        // 3. Duyệt qua mảng chi tiết gửi kèm (nếu có) và lưu thẻ Con
        List<PurchaseOrderLineDTO> lineDTOs = purchaseOrderDTO.getPurchaseOrderLines();
        if (lineDTOs != null && !lineDTOs.isEmpty()) {
            // Tạo một danh sách rỗng để chứa các Entity con
            List<PurchaseOrderLine> linesToSave = new ArrayList<>();

            for (PurchaseOrderLineDTO lineDTO : lineDTOs) {
                // Chuyển DTO thành Entity
                PurchaseOrderLine line = purchaseOrderLineMapper.toEntity(lineDTO);
                // Trỏ Khóa ngoại
                line.setPurchaseOrder(purchaseOrder);

                // Ném vào danh sách chờ (CHƯA LƯU NGAY)
                linesToSave.add(line);
            }

            // LƯU TẤT CẢ XUỐNG DB CÙNG MỘT LÚC (Tránh N+1)
            purchaseOrderLineRepository.saveAll(linesToSave);
        }
        PurchaseOrderDTO result = purchaseOrderMapper.toDto(purchaseOrder);

        // --- CODE MỚI THÊM VÀO ---
        String currentLogin = SecurityUtils.getCurrentUserLogin().orElse("System");
        eventPublisher.publishEvent(
            new OrderNotificationEvent("PURCHASE", "CREATED", result.getId(), result.getPoCode(), currentLogin, currentLogin)
        );

        // 4. Trả về kết quả
        return result;
    }

    @Override
    public PurchaseOrderDTO update(PurchaseOrderDTO purchaseOrderDTO) {
        log.debug("Request to update PurchaseOrder : {}", purchaseOrderDTO);
        PurchaseOrder oldOrder = purchaseOrderRepository
            .findById(purchaseOrderDTO.getId())
            .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));

        // CHẶN BẢO MẬT: Không cho phép đổi trạng thái qua API update thông thường
        if (oldOrder.getStatus() != purchaseOrderDTO.getStatus()) {
            throw new BadRequestAlertException(
                "Không được phép thay đổi trạng thái đơn hàng ở đây!",
                ENTITY_NAME,
                "status_change_forbidden"
            );
        }
        PurchaseOrder purchaseOrder = purchaseOrderMapper.toEntity(purchaseOrderDTO);
        purchaseOrder = purchaseOrderRepository.save(purchaseOrder);
        return purchaseOrderMapper.toDto(purchaseOrder);
    }

    @Override
    public Optional<PurchaseOrderDTO> partialUpdate(PurchaseOrderDTO purchaseOrderDTO) {
        log.debug("Request to partially update PurchaseOrder : {}", purchaseOrderDTO);

        return purchaseOrderRepository
            .findById(purchaseOrderDTO.getId())
            .map(existingPurchaseOrder -> {
                // CHẶN BẢO MẬT: Không cho phép đổi trạng thái lén lút qua PATCH
                if (purchaseOrderDTO.getStatus() != null && existingPurchaseOrder.getStatus() != purchaseOrderDTO.getStatus()) {
                    throw new BadRequestAlertException(
                        "Không được phép thay đổi trạng thái đơn hàng ở đây!",
                        ENTITY_NAME,
                        "status_change_forbidden"
                    );
                }

                purchaseOrderMapper.partialUpdate(existingPurchaseOrder, purchaseOrderDTO);

                return existingPurchaseOrder;
            })
            .map(purchaseOrderRepository::save)
            .map(purchaseOrderMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PurchaseOrderDTO> findAll(Pageable pageable) {
        log.debug("Request to get all PurchaseOrders with Data Filtering");
        return getFilteredPurchaseOrders(pageable, false);
    }

    public Page<PurchaseOrderDTO> findAllWithEagerRelationships(Pageable pageable) {
        log.debug("Request to get all PurchaseOrders with eager relationships and Data Filtering");
        return getFilteredPurchaseOrders(pageable, true);
    }

    // Hàm dùng chung cho code sạch sẽ
    private Page<PurchaseOrderDTO> getFilteredPurchaseOrders(Pageable pageable, boolean eager) {
        boolean isAdmin = SecurityUtils.hasCurrentUserThisAuthority(AuthoritiesConstants.ADMIN);
        boolean isAccountant = SecurityUtils.hasCurrentUserThisAuthority(AuthoritiesConstants.ACCOUNTANT);
        boolean isManager = SecurityUtils.hasCurrentUserThisAuthority(AuthoritiesConstants.MANAGER);

        // TẦNG 1: Admin hoặc Kế toán -> Xem tất
        if (isAdmin || isAccountant) {
            if (eager) return purchaseOrderRepository.findAllWithEagerRelationships(pageable).map(purchaseOrderMapper::toDto);
            return purchaseOrderRepository.findAll(pageable).map(purchaseOrderMapper::toDto);
        }

        String currentUserLogin = SecurityUtils.getCurrentUserLogin().orElseThrow(() -> new RuntimeException("Chưa đăng nhập!"));

        // TẦNG 2: Trưởng phòng -> Xem của cả phòng
        if (isManager) {
            return purchaseOrderRepository.findAllByEmployeeDepartment(currentUserLogin, pageable).map(purchaseOrderMapper::toDto);
        }

        // TẦNG 3: Purchaser (nhân viên mua hàng) -> Chỉ xem của mình
        return purchaseOrderRepository.findAllByEmployeeUserLogin(currentUserLogin, pageable).map(purchaseOrderMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<PurchaseOrderDTO> findOne(Long id) {
        log.debug("Request to get PurchaseOrder : {}", id);

        boolean isAdmin = SecurityUtils.hasCurrentUserThisAuthority(AuthoritiesConstants.ADMIN);
        boolean isAccountant = SecurityUtils.hasCurrentUserThisAuthority(AuthoritiesConstants.ACCOUNTANT);
        boolean isManager = SecurityUtils.hasCurrentUserThisAuthority(AuthoritiesConstants.MANAGER);

        // TẦNG 1
        if (isAdmin || isAccountant) {
            return purchaseOrderRepository.findOneWithEagerRelationships(id).map(purchaseOrderMapper::toDto);
        }

        String currentUserLogin = SecurityUtils.getCurrentUserLogin().orElseThrow();

        // TẦNG 2
        if (isManager) {
            return purchaseOrderRepository.findOneByEmployeeDepartment(id, currentUserLogin).map(purchaseOrderMapper::toDto);
        }

        // TẦNG 3: Với Purchaser -> Filter ngay trên memory
        Optional<PurchaseOrder> orderOpt = purchaseOrderRepository.findOneWithEagerRelationships(id);
        return orderOpt
            .filter(order ->
                order.getEmployee() != null &&
                order.getEmployee().getUser() != null &&
                currentUserLogin.equals(order.getEmployee().getUser().getLogin())
            )
            .map(purchaseOrderMapper::toDto);
    }

    @Override
    public void delete(Long id) {
        log.debug("Request to delete PurchaseOrder : {}", id);

        // 1. Kéo dữ liệu lên để kiểm tra
        PurchaseOrder order = purchaseOrderRepository
            .findById(id)
            .orElseThrow(() -> new BadRequestAlertException("Không tìm thấy đơn mua hàng", "purchaseOrder", "id_not_found"));

        // 2. Chặn xóa chứng từ đã xử lý
        if (order.getStatus() != OrderStatus.DRAFT) {
            throw new BadRequestAlertException(
                "Đại kỵ! Chỉ được xóa vĩnh viễn đơn hàng ở trạng thái NHÁP (DRAFT). Hãy dùng tính năng HỦY (CANCEL) để lưu vết!",
                "purchaseOrder",
                "cannot_delete_processed_order"
            );
        }

        // 3. Phân quyền: Cấm Purchaser xóa đơn nháp của người khác
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
                throw new BadRequestAlertException("Bạn không có quyền xóa đơn nháp của người khác!", "purchaseOrder", "access_denied");
            }
        }

        // 4. Cho phép xóa
        purchaseOrderRepository.deleteById(id);
    }

    /**
     * Hàm chuyên dụng để Duyệt đơn và Nhập kho
     */
    @Override
    public PurchaseOrderDTO approveOrder(Long id) {
        log.debug("Request to approve PurchaseOrder : {}", id);

        PurchaseOrder order = purchaseOrderRepository.findById(id).orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));

        if (order.getStatus() == OrderStatus.APPROVED) {
            throw new BadRequestAlertException("Đơn hàng này đã được duyệt rồi!", ENTITY_NAME, "already_approved");
        }

        // 1. Đổi trạng thái sang Đã duyệt
        order.setStatus(OrderStatus.APPROVED);
        order = purchaseOrderRepository.save(order);

        // 2. Gọi hàm Bơm hàng vào kho (cái hàm processInboundInventory bác giữ nguyên nhé)
        processInboundInventory(order);

        String currentLogin = SecurityUtils.getCurrentUserLogin().orElse("System");
        String creatorLogin = (order.getEmployee() != null && order.getEmployee().getUser() != null)
            ? order.getEmployee().getUser().getLogin()
            : currentLogin;

        eventPublisher.publishEvent(
            new OrderNotificationEvent("PURCHASE", "APPROVED", order.getId(), order.getPoCode(), currentLogin, creatorLogin)
        );

        return purchaseOrderMapper.toDto(order);
    }

    /**
     * Hàm tự động sinh Lịch sử và Cộng Tồn kho khi Đơn mua hàng được duyệt
     */
    private void processInboundInventory(PurchaseOrder order) {
        log.debug("Bắt đầu xử lý nhập kho cho Đơn mua hàng: {}", order.getPoCode());

        // 1. Lấy danh sách các mặt hàng trong đơn này
        List<PurchaseOrderLine> lines = purchaseOrderLineRepository.findByPurchaseOrderId(order.getId());

        for (PurchaseOrderLine line : lines) {
            // 2. Ghi nhận Lịch sử giao dịch (Transaction)
            InventoryTransaction transaction = new InventoryTransaction();
            transaction.setType(TransactionType.RECEIPT); // Loại hình: NHẬP KHO
            transaction.setQuantity(line.getQuantity());
            transaction.setUnitCost(line.getUnitPrice());
            transaction.setReferenceId(order.getId());
            transaction.setCreatedDate(Instant.now());
            transaction.setProduct(line.getProduct());
            transaction.setWarehouse(order.getWarehouse());

            inventoryTransactionRepository.save(transaction);

            // 3. Cập nhật cục Tồn kho (Balance)
            // Tìm xem sản phẩm này trong kho này đã có record nào chưa?
            Optional<InventoryBalance> existingBalance = inventoryBalanceRepository.findOneByProductIdAndWarehouseId(
                line.getProduct().getId(),
                order.getWarehouse().getId()
            );

            if (existingBalance.isPresent()) {
                // Đã có hàng -> Cộng dồn số lượng
                InventoryBalance balance = existingBalance.get();
                balance.setQuantity(balance.getQuantity() + line.getQuantity());
                inventoryBalanceRepository.save(balance);
            } else {
                // Chưa từng có mặt hàng này ở kho này -> Tạo dòng mới
                InventoryBalance newBalance = new InventoryBalance();
                newBalance.setProduct(line.getProduct());
                newBalance.setWarehouse(order.getWarehouse());
                newBalance.setQuantity(line.getQuantity());
                inventoryBalanceRepository.save(newBalance);
            }
        }
        log.debug("Hoàn tất nhập kho thành công!");
    }

    /**
     * Hàm Chốt đơn và Ghi nhận Công nợ Phải Trả
     */
    @Override
    public PurchaseOrderDTO completeOrder(Long id) {
        log.debug("Request to complete PurchaseOrder : {}", id);

        PurchaseOrder order = purchaseOrderRepository.findById(id).orElseThrow(() -> new RuntimeException("Không tìm thấy đơn mua hàng"));

        if (order.getStatus() == OrderStatus.COMPLETED) {
            throw new BadRequestAlertException("Đơn hàng này đã được hoàn thành trước đó!", "purchaseOrder", "already_completed");
        }
        if (order.getStatus() != OrderStatus.APPROVED) {
            throw new BadRequestAlertException("Chỉ có thể hoàn thành đơn hàng đã duyệt (APPROVED)!", "purchaseOrder", "invalid_status");
        }

        order.setStatus(OrderStatus.COMPLETED);
        order = purchaseOrderRepository.save(order);

        // 3. Ghi nợ cho Nhà cung cấp
        Supplier supplier = order.getSupplier();
        if (supplier != null) {
            BigDecimal currentDebt = supplier.getCurrentDebt() != null ? supplier.getCurrentDebt() : BigDecimal.ZERO;
            BigDecimal orderTotal = order.getTotalAmount() != null ? order.getTotalAmount() : BigDecimal.ZERO;

            // Nợ phải trả mới = Nợ cũ + Tiền đơn hàng
            supplier.setCurrentDebt(currentDebt.add(orderTotal));
            supplierRepository.save(supplier);
        }
        String currentLogin = SecurityUtils.getCurrentUserLogin().orElse("System");
        String creatorLogin = (order.getEmployee() != null && order.getEmployee().getUser() != null)
            ? order.getEmployee().getUser().getLogin()
            : currentLogin;

        eventPublisher.publishEvent(
            new OrderNotificationEvent("PURCHASE", "COMPLETED", order.getId(), order.getPoCode(), currentLogin, creatorLogin)
        );

        return purchaseOrderMapper.toDto(order);
    }

    /**
     * Hàm Hủy Đơn Mua Hàng và dọn dẹp dữ liệu (Xuất trả hàng, Giảm nợ)
     */
    @Override
    public PurchaseOrderDTO cancelOrder(Long id) {
        log.debug("Request to cancel PurchaseOrder : {}", id);

        PurchaseOrder order = purchaseOrderRepository.findById(id).orElseThrow(() -> new RuntimeException("Không tìm thấy đơn mua hàng"));

        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new BadRequestAlertException("Đơn hàng này đã bị hủy từ trước!", "purchaseOrder", "already_cancelled");
        }

        // 1. NẾU ĐƠN ĐÃ COMPLETED -> XÓA NỢ CHO NHÀ CUNG CẤP
        if (order.getStatus() == OrderStatus.COMPLETED) {
            Supplier supplier = order.getSupplier();
            if (supplier != null) {
                BigDecimal currentDebt = supplier.getCurrentDebt() != null ? supplier.getCurrentDebt() : BigDecimal.ZERO;
                BigDecimal orderTotal = order.getTotalAmount() != null ? order.getTotalAmount() : BigDecimal.ZERO;

                // Trừ đi khoản nợ mình đã lỡ cộng vào
                supplier.setCurrentDebt(currentDebt.subtract(orderTotal));
                supplierRepository.save(supplier);
            }
        }

        // 2. NẾU ĐƠN ĐÃ APPROVED HOẶC COMPLETED -> XUẤT TRẢ LẠI TỒN KHO
        if (order.getStatus() == OrderStatus.APPROVED || order.getStatus() == OrderStatus.COMPLETED) {
            List<PurchaseOrderLine> lines = purchaseOrderLineRepository.findByPurchaseOrderId(order.getId());

            for (PurchaseOrderLine line : lines) {
                InventoryBalance balance = inventoryBalanceRepository
                    .findOneByProductIdAndWarehouseId(line.getProduct().getId(), order.getWarehouse().getId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy tồn kho để xuất trả"));

                // Trừ đi số lượng hàng mình trả lại cho Supplier
                if (balance.getQuantity() < line.getQuantity()) {
                    throw new BadRequestAlertException(
                        "Tồn kho hiện tại không đủ để xuất trả!",
                        "purchaseOrder",
                        "insufficient_stock_to_return"
                    );
                }
                balance.setQuantity(balance.getQuantity() - line.getQuantity());
                inventoryBalanceRepository.save(balance);

                // Ghi lại lịch sử (Loại ISSUE - Xuất kho trả hàng)
                InventoryTransaction issueTx = new InventoryTransaction();
                issueTx.setType(TransactionType.ISSUE);
                issueTx.setQuantity(line.getQuantity());
                issueTx.setReferenceId(order.getId());
                issueTx.setCreatedDate(Instant.now());
                issueTx.setProduct(line.getProduct());
                issueTx.setWarehouse(order.getWarehouse());
                inventoryTransactionRepository.save(issueTx);
            }
        }

        // 3. ĐỔI TRẠNG THÁI
        order.setStatus(OrderStatus.CANCELLED);
        order = purchaseOrderRepository.save(order);

        String currentLogin = SecurityUtils.getCurrentUserLogin().orElse("System");
        String creatorLogin = (order.getEmployee() != null && order.getEmployee().getUser() != null)
            ? order.getEmployee().getUser().getLogin()
            : currentLogin;

        eventPublisher.publishEvent(
            new OrderNotificationEvent("PURCHASE", "CANCELLED", order.getId(), order.getPoCode(), currentLogin, creatorLogin)
        );

        return purchaseOrderMapper.toDto(order);
    }
}
