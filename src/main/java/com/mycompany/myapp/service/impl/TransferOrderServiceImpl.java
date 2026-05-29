package com.mycompany.myapp.service.impl;

import com.mycompany.myapp.domain.*;
import com.mycompany.myapp.domain.enumeration.OrderStatus;
import com.mycompany.myapp.domain.enumeration.TransactionType;
import com.mycompany.myapp.repository.*;
import com.mycompany.myapp.security.AuthoritiesConstants;
import com.mycompany.myapp.security.SecurityUtils;
import com.mycompany.myapp.service.TransferOrderService;
import com.mycompany.myapp.service.dto.TransferOrderDTO;
import com.mycompany.myapp.service.dto.TransferOrderLineDTO;
import com.mycompany.myapp.service.event.OrderNotificationEvent;
import com.mycompany.myapp.service.mapper.TransferOrderLineMapper;
import com.mycompany.myapp.service.mapper.TransferOrderMapper;
import com.mycompany.myapp.web.rest.errors.BadRequestAlertException;
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
 * Service Implementation for managing {@link TransferOrder}.
 */
@Service
@Transactional
public class TransferOrderServiceImpl implements TransferOrderService {

    private final Logger log = LoggerFactory.getLogger(TransferOrderServiceImpl.class);

    private final TransferOrderRepository transferOrderRepository;

    private final TransferOrderMapper transferOrderMapper;
    private final TransferOrderLineRepository transferOrderLineRepository;
    private final InventoryTransactionRepository inventoryTransactionRepository;
    private final InventoryBalanceRepository inventoryBalanceRepository;
    private final TransferOrderLineMapper transferOrderLineMapper;
    private final EmployeeRepository employeeRepository;
    private final ApplicationEventPublisher eventPublisher;

    public TransferOrderServiceImpl(
        TransferOrderRepository transferOrderRepository,
        TransferOrderMapper transferOrderMapper,
        TransferOrderLineRepository transferOrderLineRepository,
        InventoryTransactionRepository inventoryTransactionRepository,
        InventoryBalanceRepository inventoryBalanceRepository,
        TransferOrderLineMapper transferOrderLineMapper,
        EmployeeRepository employeeRepository,
        ApplicationEventPublisher eventPublisher
    ) {
        this.transferOrderRepository = transferOrderRepository;
        this.transferOrderMapper = transferOrderMapper;
        this.transferOrderLineRepository = transferOrderLineRepository;
        this.inventoryTransactionRepository = inventoryTransactionRepository;
        this.inventoryBalanceRepository = inventoryBalanceRepository;
        this.transferOrderLineMapper = transferOrderLineMapper;
        this.employeeRepository = employeeRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public TransferOrderDTO save(TransferOrderDTO transferOrderDTO) {
        log.debug("Request to save TransferOrder : {}", transferOrderDTO);
        if (!SecurityUtils.hasCurrentUserThisAuthority(AuthoritiesConstants.ADMIN)) {
            String currentUserLogin = SecurityUtils
                .getCurrentUserLogin()
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thông tin đăng nhập!"));

            Employee employee = employeeRepository
                .findByUserLogin(currentUserLogin)
                .orElseThrow(() -> new BadRequestAlertException("Nhân viên không tồn tại", "transferOrder", "employee_not_found"));

            // Nếu kho của nhân viên bị NULL, hoặc kho đó KHÁC với kho xuất (fromWarehouse) -> Bắn lỗi ngay!
            if (
                employee.getScopedWarehouse() == null ||
                !employee.getScopedWarehouse().getId().equals(transferOrderDTO.getFromWarehouse().getId())
            ) {
                throw new BadRequestAlertException(
                    "Bạn không có quyền xuất hàng từ kho mà bạn không quản lý!",
                    "transferOrder",
                    "warehouse_access_denied"
                );
            }
        }
        // 1. BẢO MẬT: Bắt buộc đơn mới tạo phải là DRAFT (Nháp)
        transferOrderDTO.setStatus(OrderStatus.DRAFT);

        // 2. Chặn lỗi ngớ ngẩn ngay từ lúc tạo nháp: Kho xuất và Kho nhập giống nhau
        if (
            transferOrderDTO.getFromWarehouse() != null &&
            transferOrderDTO.getToWarehouse() != null &&
            transferOrderDTO.getFromWarehouse().getId().equals(transferOrderDTO.getToWarehouse().getId())
        ) {
            throw new BadRequestAlertException("Kho xuất và Kho nhập không được trùng nhau!", "transferOrder", "same_warehouse");
        }

        // 3. Lưu Phiếu Cha (TransferOrder) để lấy ID
        TransferOrder transferOrder = transferOrderMapper.toEntity(transferOrderDTO);
        transferOrder = transferOrderRepository.save(transferOrder);

        // 4. Lưu danh sách Mặt hàng Con (TransferOrderLine) trong 1 lần bắn (Batch Insert)
        if (transferOrderDTO.getTransferOrderLines() != null && !transferOrderDTO.getTransferOrderLines().isEmpty()) {
            List<TransferOrderLine> linesToSave = new ArrayList<>();
            for (TransferOrderLineDTO lineDTO : transferOrderDTO.getTransferOrderLines()) {
                TransferOrderLine line = transferOrderLineMapper.toEntity(lineDTO);
                line.setTransferOrder(transferOrder); // Trỏ khóa ngoại về phiếu cha vừa tạo
                linesToSave.add(line);
            }
            transferOrderLineRepository.saveAll(linesToSave);
        }

        TransferOrderDTO result = transferOrderMapper.toDto(transferOrder);

        // --- CODE MỚI THÊM VÀO ---
        String currentLogin = SecurityUtils.getCurrentUserLogin().orElse("System");

        eventPublisher.publishEvent(
            new OrderNotificationEvent("TRANSFER", "CREATED", result.getId(), result.getTransferCode(), currentLogin, currentLogin)
        );

        return result;
    }

    @Override
    public TransferOrderDTO update(TransferOrderDTO transferOrderDTO) {
        log.debug("Request to update TransferOrder : {}", transferOrderDTO);
        if (!SecurityUtils.hasCurrentUserThisAuthority(AuthoritiesConstants.ADMIN)) {
            String currentUserLogin = SecurityUtils
                .getCurrentUserLogin()
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thông tin đăng nhập!"));

            Employee employee = employeeRepository
                .findByUserLogin(currentUserLogin)
                .orElseThrow(() -> new BadRequestAlertException("Nhân viên không tồn tại", "transferOrder", "employee_not_found"));

            // Nếu kho của nhân viên bị NULL, hoặc kho đó KHÁC với kho xuất (fromWarehouse) -> Bắn lỗi ngay!
            if (
                employee.getScopedWarehouse() == null ||
                !employee.getScopedWarehouse().getId().equals(transferOrderDTO.getFromWarehouse().getId())
            ) {
                throw new BadRequestAlertException(
                    "Bạn không có quyền xuất hàng từ kho mà bạn không quản lý!",
                    "transferOrder",
                    "warehouse_access_denied"
                );
            }
        }
        // 1. Kiểm tra bảo mật
        TransferOrder oldOrder = transferOrderRepository
            .findById(transferOrderDTO.getId())
            .orElseThrow(() -> new RuntimeException("Không tìm thấy phiếu điều chuyển"));

        if (oldOrder.getStatus() == OrderStatus.APPROVED) {
            throw new BadRequestAlertException("Phiếu đã duyệt thì không được sửa!", "transferOrder", "already_approved");
        }
        if (oldOrder.getStatus() != transferOrderDTO.getStatus()) {
            throw new BadRequestAlertException("Không được đổi trạng thái ở đây!", "transferOrder", "status_change_forbidden");
        }

        // 2. Lưu Phiếu Cha
        TransferOrder transferOrder = transferOrderMapper.toEntity(transferOrderDTO);
        transferOrder = transferOrderRepository.save(transferOrder);

        // 3. Cập nhật Phiếu Con (Xóa sạch các dòng cũ và Insert lại các dòng mới Front-end gửi lên)
        if (transferOrderDTO.getTransferOrderLines() != null) {
            // Lấy và xóa dòng cũ
            List<TransferOrderLine> oldLines = transferOrderLineRepository.findByTransferOrderId(transferOrder.getId());
            transferOrderLineRepository.deleteAll(oldLines);

            // Lưu dòng mới
            List<TransferOrderLine> newLines = new ArrayList<>();
            for (TransferOrderLineDTO lineDTO : transferOrderDTO.getTransferOrderLines()) {
                TransferOrderLine line = transferOrderLineMapper.toEntity(lineDTO);
                line.setTransferOrder(transferOrder);
                newLines.add(line);
            }
            transferOrderLineRepository.saveAll(newLines);
        }

        return transferOrderMapper.toDto(transferOrder);
    }

    @Override
    public Optional<TransferOrderDTO> partialUpdate(TransferOrderDTO transferOrderDTO) {
        log.debug("Request to partially update TransferOrder : {}", transferOrderDTO);

        return transferOrderRepository
            .findById(transferOrderDTO.getId())
            .map(existingOrder -> {
                // Bảo mật
                if (!SecurityUtils.hasCurrentUserThisAuthority(AuthoritiesConstants.ADMIN)) {
                    String currentUserLogin = SecurityUtils
                        .getCurrentUserLogin()
                        .orElseThrow(() -> new RuntimeException("Không tìm thấy thông tin đăng nhập!"));

                    Employee employee = employeeRepository
                        .findByUserLogin(currentUserLogin)
                        .orElseThrow(() -> new BadRequestAlertException("Nhân viên không tồn tại", "transferOrder", "employee_not_found"));

                    // 1. Chặn sửa phiếu của kho khác
                    if (
                        employee.getScopedWarehouse() == null ||
                        !employee.getScopedWarehouse().getId().equals(existingOrder.getFromWarehouse().getId())
                    ) {
                        throw new BadRequestAlertException(
                            "Bạn không có quyền sửa phiếu của kho khác!",
                            "transferOrder",
                            "warehouse_access_denied"
                        );
                    }

                    // 2. Chặn việc lén lút đổi kho xuất qua PATCH
                    if (
                        transferOrderDTO.getFromWarehouse() != null &&
                        !employee.getScopedWarehouse().getId().equals(transferOrderDTO.getFromWarehouse().getId())
                    ) {
                        throw new BadRequestAlertException(
                            "Không được phép đổi kho xuất sang kho mà bạn không quản lý!",
                            "transferOrder",
                            "warehouse_access_denied"
                        );
                    }
                }
                if (existingOrder.getStatus() == OrderStatus.APPROVED) {
                    throw new BadRequestAlertException("Phiếu đã duyệt thì không được sửa!", "transferOrder", "already_approved");
                }
                if (transferOrderDTO.getStatus() != null && existingOrder.getStatus() != transferOrderDTO.getStatus()) {
                    throw new BadRequestAlertException("Không được đổi trạng thái ở đây!", "transferOrder", "status_change_forbidden");
                }

                // Map các trường mới vào existingOrder
                transferOrderMapper.partialUpdate(existingOrder, transferOrderDTO);
                return existingOrder;
            })
            .map(transferOrderRepository::save)
            .map(savedOrder -> {
                // Nếu Front-end có gửi kèm danh sách mặt hàng để sửa một phần
                if (transferOrderDTO.getTransferOrderLines() != null) {
                    List<TransferOrderLine> oldLines = transferOrderLineRepository.findByTransferOrderId(savedOrder.getId());
                    transferOrderLineRepository.deleteAll(oldLines);

                    List<TransferOrderLine> newLines = new ArrayList<>();
                    for (TransferOrderLineDTO lineDTO : transferOrderDTO.getTransferOrderLines()) {
                        TransferOrderLine line = transferOrderLineMapper.toEntity(lineDTO);
                        line.setTransferOrder(savedOrder);
                        newLines.add(line);
                    }
                    transferOrderLineRepository.saveAll(newLines);
                }
                return transferOrderMapper.toDto(savedOrder);
            });
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TransferOrderDTO> findAll(Pageable pageable) {
        log.debug("Request to get all TransferOrders with Data Filtering");
        return getFilteredTransferOrders(pageable, false);
    }

    public Page<TransferOrderDTO> findAllWithEagerRelationships(Pageable pageable) {
        log.debug("Request to get all TransferOrders with eager relationships and Data Filtering");
        return getFilteredTransferOrders(pageable, true);
    }

    // Hàm dùng chung cho luồng Điều chuyển
    private Page<TransferOrderDTO> getFilteredTransferOrders(Pageable pageable, boolean eager) {
        boolean isAdmin = SecurityUtils.hasCurrentUserThisAuthority(AuthoritiesConstants.ADMIN);
        boolean isManager = SecurityUtils.hasCurrentUserThisAuthority(AuthoritiesConstants.MANAGER);

        // TẦNG 1: Admin hoặc Manager -> Xem toàn bộ các phiếu điều chuyển của tất cả các kho
        if (isAdmin || isManager) {
            if (eager) return transferOrderRepository.findAllWithEagerRelationships(pageable).map(transferOrderMapper::toDto);
            return transferOrderRepository.findAll(pageable).map(transferOrderMapper::toDto);
        }

        // TẦNG 2: Warehouse (Thủ kho chi nhánh) -> Chỉ xem phiếu liên quan đến kho của mình
        String currentUserLogin = SecurityUtils.getCurrentUserLogin().orElseThrow(() -> new RuntimeException("Chưa đăng nhập!"));
        return transferOrderRepository.findAllByEmployeeScopedWarehouse(currentUserLogin, pageable).map(transferOrderMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<TransferOrderDTO> findOne(Long id) {
        log.debug("Request to get TransferOrder : {}", id);

        boolean isAdmin = SecurityUtils.hasCurrentUserThisAuthority(AuthoritiesConstants.ADMIN);
        boolean isManager = SecurityUtils.hasCurrentUserThisAuthority(AuthoritiesConstants.MANAGER);

        // TẦNG 1
        if (isAdmin || isManager) {
            return transferOrderRepository.findOneWithEagerRelationships(id).map(transferOrderMapper::toDto);
        }

        // TẦNG 2
        String currentUserLogin = SecurityUtils.getCurrentUserLogin().orElseThrow(() -> new RuntimeException("Chưa đăng nhập!"));
        return transferOrderRepository.findOneByIdAndUserLogin(id, currentUserLogin).map(transferOrderMapper::toDto);
    }

    @Override
    public void delete(Long id) {
        log.debug("Request to delete TransferOrder : {}", id);

        // 1. Kéo dữ liệu lên kiểm tra
        TransferOrder order = transferOrderRepository
            .findById(id)
            .orElseThrow(() -> new BadRequestAlertException("Không tìm thấy phiếu điều chuyển", "transferOrder", "id_not_found"));

        // 2. Chặn xóa phiếu đã duyệt
        if (order.getStatus() != OrderStatus.DRAFT) {
            throw new BadRequestAlertException(
                "Đại kỵ! Chỉ được xóa vĩnh viễn phiếu điều chuyển ở trạng thái NHÁP (DRAFT).",
                "transferOrder",
                "cannot_delete_processed_order"
            );
        }

        // 3. Phân quyền: Cấm Thủ kho xóa phiếu của kho khác
        if (
            !SecurityUtils.hasCurrentUserThisAuthority(AuthoritiesConstants.ADMIN) &&
            !SecurityUtils.hasCurrentUserThisAuthority(AuthoritiesConstants.MANAGER)
        ) {
            String currentUserLogin = SecurityUtils.getCurrentUserLogin().orElseThrow();
            Employee employee = employeeRepository
                .findByUserLogin(currentUserLogin)
                .orElseThrow(() -> new BadRequestAlertException("Nhân viên không tồn tại", "transferOrder", "employee_not_found"));

            // Check xem kho xuất của phiếu có khớp với kho mà nhân viên đang quản lý không
            if (
                employee.getScopedWarehouse() == null ||
                order.getFromWarehouse() == null ||
                !employee.getScopedWarehouse().getId().equals(order.getFromWarehouse().getId())
            ) {
                throw new BadRequestAlertException(
                    "Bạn không có quyền xóa phiếu nháp của kho khác!",
                    "transferOrder",
                    "warehouse_access_denied"
                );
            }
        }

        // 4. Cho phép xóa
        transferOrderRepository.deleteById(id);
    }

    //ham xu li kho
    private void processTransferInventory(TransferOrder order) {
        log.debug("Bắt đầu xử lý điều chuyển cho phiếu: {}", order.getId());

        List<TransferOrderLine> lines = transferOrderLineRepository.findByTransferOrderId(order.getId());

        for (TransferOrderLine line : lines) {
            // 1. TRỪ KHO XUẤT (fromWarehouse)
            InventoryBalance sourceBalance = inventoryBalanceRepository
                .findOneByProductIdAndWarehouseId(line.getProduct().getId(), order.getFromWarehouse().getId())
                .orElseThrow(() -> new BadRequestAlertException("Sản phẩm không có trong kho xuất!", "transferOrder", "out_of_stock"));

            if (sourceBalance.getQuantity() < line.getQuantity()) {
                throw new BadRequestAlertException("Kho xuất không đủ hàng!", "transferOrder", "insufficient_stock");
            }

            sourceBalance.setQuantity(sourceBalance.getQuantity() - line.getQuantity());
            inventoryBalanceRepository.save(sourceBalance);

            InventoryTransaction issueTx = new InventoryTransaction();
            issueTx.setType(TransactionType.ISSUE);
            issueTx.setQuantity(line.getQuantity());
            issueTx.setReferenceId(order.getId());
            issueTx.setCreatedDate(Instant.now());
            issueTx.setProduct(line.getProduct());
            issueTx.setWarehouse(order.getFromWarehouse()); // Kho xuất
            inventoryTransactionRepository.save(issueTx);

            // 2. CỘNG KHO NHẬP (toWarehouse)
            Optional<InventoryBalance> destBalanceOpt = inventoryBalanceRepository.findOneByProductIdAndWarehouseId(
                line.getProduct().getId(),
                order.getToWarehouse().getId()
            );

            if (destBalanceOpt.isPresent()) {
                InventoryBalance destBalance = destBalanceOpt.get();
                destBalance.setQuantity(destBalance.getQuantity() + line.getQuantity());
                inventoryBalanceRepository.save(destBalance);
            } else {
                InventoryBalance newDestBalance = new InventoryBalance();
                newDestBalance.setProduct(line.getProduct());
                newDestBalance.setWarehouse(order.getToWarehouse()); // Kho nhập
                newDestBalance.setQuantity(line.getQuantity());
                inventoryBalanceRepository.save(newDestBalance);
            }

            InventoryTransaction receiptTx = new InventoryTransaction();
            receiptTx.setType(TransactionType.RECEIPT);
            receiptTx.setQuantity(line.getQuantity());
            receiptTx.setReferenceId(order.getId());
            receiptTx.setCreatedDate(Instant.now());
            receiptTx.setProduct(line.getProduct());
            receiptTx.setWarehouse(order.getToWarehouse()); // Kho nhập
            inventoryTransactionRepository.save(receiptTx);
        }
    }

    // Hàm Duyệt phiếu điều chuyển
    @Override
    public TransferOrderDTO approveOrder(Long id) {
        TransferOrder order = transferOrderRepository
            .findById(id)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy phiếu điều chuyển"));

        if (order.getStatus() == OrderStatus.APPROVED) {
            throw new BadRequestAlertException("Phiếu đã được duyệt!", "transferOrder", "already_approved");
        }

        if (order.getFromWarehouse().getId().equals(order.getToWarehouse().getId())) {
            throw new BadRequestAlertException("Kho xuất và Kho nhập không được trùng nhau!", "transferOrder", "same_warehouse");
        }

        order.setStatus(OrderStatus.APPROVED);
        order = transferOrderRepository.save(order);

        processTransferInventory(order);

        String currentLogin = SecurityUtils.getCurrentUserLogin().orElse("System");

        // Vì TransferOrder chưa lưu Employee tạo đơn, ta đành truyền currentLogin vào
        // để code không bị lỗi. Đồng nghĩa với việc người bấm duyệt sẽ tự nhận thông báo của chính mình.
        String creatorLogin = currentLogin;

        eventPublisher.publishEvent(
            new OrderNotificationEvent("TRANSFER", "APPROVED", order.getId(), order.getTransferCode(), currentLogin, creatorLogin)
        );

        return transferOrderMapper.toDto(order);
    }

    /**
     * Hàm Hủy Phiếu Điều Chuyển: Hoàn trả lại kho Nguồn và Thu hồi từ kho Đích
     */
    @Override
    public TransferOrderDTO cancelOrder(Long id) {
        log.debug("Request to cancel TransferOrder : {}", id);

        TransferOrder order = transferOrderRepository
            .findById(id)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy phiếu điều chuyển"));

        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new BadRequestAlertException("Phiếu này đã bị hủy từ trước!", "transferOrder", "already_cancelled");
        }

        // NẾU ĐÃ DUYỆT -> PHẢI ĐẢO NGƯỢC LẠI KHO
        if (order.getStatus() == OrderStatus.APPROVED || order.getStatus() == OrderStatus.COMPLETED) {
            List<TransferOrderLine> lines = transferOrderLineRepository.findByTransferOrderId(order.getId());

            for (TransferOrderLine line : lines) {
                // ==========================================
                // A. THU HỒI TỪ KHO ĐÍCH (TRỪ HÀNG)
                // ==========================================
                InventoryBalance destBalance = inventoryBalanceRepository
                    .findOneByProductIdAndWarehouseId(line.getProduct().getId(), order.getToWarehouse().getId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy tồn kho đích để thu hồi"));

                // Chặn lỗi: Kho đích đã lỡ bán mất hàng điều chuyển đến
                if (destBalance.getQuantity() < line.getQuantity()) {
                    throw new BadRequestAlertException(
                        "Kho đích không còn đủ hàng để thu hồi do đã xuất bán!",
                        "transferOrder",
                        "insufficient_dest_stock"
                    );
                }
                destBalance.setQuantity(destBalance.getQuantity() - line.getQuantity());
                inventoryBalanceRepository.save(destBalance);

                InventoryTransaction destTx = new InventoryTransaction();
                destTx.setType(TransactionType.ISSUE); // Xuất trả lại
                destTx.setQuantity(line.getQuantity());
                destTx.setReferenceId(order.getId());
                destTx.setCreatedDate(Instant.now());
                destTx.setProduct(line.getProduct());
                destTx.setWarehouse(order.getToWarehouse());
                inventoryTransactionRepository.save(destTx);

                // ==========================================
                // B. HOÀN TRẢ LẠI KHO NGUỒN (CỘNG HÀNG)
                // ==========================================
                InventoryBalance sourceBalance = inventoryBalanceRepository
                    .findOneByProductIdAndWarehouseId(line.getProduct().getId(), order.getFromWarehouse().getId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy tồn kho nguồn để hoàn trả"));

                sourceBalance.setQuantity(sourceBalance.getQuantity() + line.getQuantity());
                inventoryBalanceRepository.save(sourceBalance);

                InventoryTransaction sourceTx = new InventoryTransaction();
                sourceTx.setType(TransactionType.RECEIPT); // Nhập nhận lại
                sourceTx.setQuantity(line.getQuantity());
                sourceTx.setReferenceId(order.getId());
                sourceTx.setCreatedDate(Instant.now());
                sourceTx.setProduct(line.getProduct());
                sourceTx.setWarehouse(order.getFromWarehouse());
                inventoryTransactionRepository.save(sourceTx);
            }
        }

        // ĐỔI TRẠNG THÁI THÀNH CANCELLED
        order.setStatus(OrderStatus.CANCELLED);
        order = transferOrderRepository.save(order);

        String currentLogin = SecurityUtils.getCurrentUserLogin().orElse("System");
        String creatorLogin = currentLogin;

        eventPublisher.publishEvent(
            new OrderNotificationEvent("TRANSFER", "CANCELLED", order.getId(), order.getTransferCode(), currentLogin, creatorLogin)
        );

        return transferOrderMapper.toDto(order);
    }
}
