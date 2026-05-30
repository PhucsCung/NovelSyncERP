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
        validateWarehouseManagerAccess(transferOrderDTO.getFromWarehouse() != null ? transferOrderDTO.getFromWarehouse().getId() : null);
        //Bắt buộc đơn mới tạo phải là DRAFT (Nháp)
        transferOrderDTO.setStatus(OrderStatus.DRAFT);
        //chặn Kho xuất và Kho nhập giống nhau
        if (
            transferOrderDTO.getFromWarehouse() != null &&
            transferOrderDTO.getToWarehouse() != null &&
            transferOrderDTO.getFromWarehouse().getId().equals(transferOrderDTO.getToWarehouse().getId())
        ) {
            throw new BadRequestAlertException("Kho xuất và Kho nhập không được trùng nhau!", "transferOrder", "same_warehouse");
        }
        //Lưu Phiếu Cha (TransferOrder) để lấy ID
        TransferOrder transferOrder = transferOrderMapper.toEntity(transferOrderDTO);
        transferOrder = transferOrderRepository.save(transferOrder);

        //Lưu danh sách Mặt hàng Con (TransferOrderLine) trong 1 lần bắn (Batch Insert)
        if (transferOrderDTO.getTransferOrderLines() != null && !transferOrderDTO.getTransferOrderLines().isEmpty()) {
            List<TransferOrderLine> linesToSave = new ArrayList<>();
            for (TransferOrderLineDTO lineDTO : transferOrderDTO.getTransferOrderLines()) {
                TransferOrderLine line = transferOrderLineMapper.toEntity(lineDTO);
                line.setTransferOrder(transferOrder);
                linesToSave.add(line);
            }
            transferOrderLineRepository.saveAll(linesToSave);
        }

        TransferOrderDTO result = transferOrderMapper.toDto(transferOrder);

        String currentLogin = SecurityUtils.getCurrentUserLogin().orElse("System");
        eventPublisher.publishEvent(
            new OrderNotificationEvent("TRANSFER", "CREATED", result.getId(), result.getTransferCode(), currentLogin, currentLogin)
        );

        return result;
    }

    @Override
    public TransferOrderDTO update(TransferOrderDTO transferOrderDTO) {
        log.debug("Request to update TransferOrder : {}", transferOrderDTO);
        TransferOrder oldOrder = transferOrderRepository
            .findById(transferOrderDTO.getId())
            .orElseThrow(() -> new RuntimeException("Không tìm thấy phiếu điều chuyển"));
        // Nhân viên phải có quyền trên kho xuất HIỆN TẠI của phiếu
        validateWarehouseManagerAccess(oldOrder.getFromWarehouse() != null ? oldOrder.getFromWarehouse().getId() : null);
        // Chỉ phiếu DRAFT mới được sửa
        if (oldOrder.getStatus() != OrderStatus.DRAFT) {
            throw new BadRequestAlertException(
                "Chỉ phiếu ở trạng thái NHÁP mới được phép chỉnh sửa.",
                "transferOrder",
                "cannot_edit_processed_order"
            );
        }

        if (oldOrder.getStatus() != transferOrderDTO.getStatus()) {
            throw new BadRequestAlertException("Không được đổi trạng thái ở đây!", "transferOrder", "status_change_forbidden");
        }

        // CẤM ĐỔI KHO XUẤT
        if (transferOrderDTO.getFromWarehouse() != null && oldOrder.getFromWarehouse() != null) {
            if (!transferOrderDTO.getFromWarehouse().getId().equals(oldOrder.getFromWarehouse().getId())) {
                throw new BadRequestAlertException(
                    "Không được phép thay đổi kho xuất của phiếu điều chuyển!",
                    "transferOrder",
                    "from_warehouse_immutable"
                );
            }
        }

        // Chặn trùng kho xuất - nhập
        if (
            transferOrderDTO.getFromWarehouse() != null &&
            transferOrderDTO.getToWarehouse() != null &&
            transferOrderDTO.getFromWarehouse().getId().equals(transferOrderDTO.getToWarehouse().getId())
        ) {
            throw new BadRequestAlertException("Kho xuất và Kho nhập không được trùng nhau!", "transferOrder", "same_warehouse");
        }

        // 2. Lưu Phiếu Cha
        TransferOrder transferOrder = transferOrderMapper.toEntity(transferOrderDTO);
        transferOrder = transferOrderRepository.save(transferOrder);

        // 3. Cập nhật Phiếu Con (Batch)
        if (transferOrderDTO.getTransferOrderLines() != null) {
            List<TransferOrderLine> oldLines = transferOrderLineRepository.findByTransferOrderId(transferOrder.getId());
            transferOrderLineRepository.deleteAll(oldLines);

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
                //Nhân viên phải có quyền trên kho xuất HIỆN TẠI của phiếu
                validateWarehouseManagerAccess(existingOrder.getFromWarehouse() != null ? existingOrder.getFromWarehouse().getId() : null);

                //Chỉ cho phép sửa khi phiếu ở trạng thái DRAFT
                if (existingOrder.getStatus() != OrderStatus.DRAFT) {
                    throw new BadRequestAlertException(
                        "Chỉ phiếu ở trạng thái NHÁP mới được phép chỉnh sửa.",
                        "transferOrder",
                        "cannot_edit_processed_order"
                    );
                }

                //CẤM TUYỆT ĐỐI đổi kho xuất (fromWarehouse)
                if (transferOrderDTO.getFromWarehouse() != null && existingOrder.getFromWarehouse() != null) {
                    if (!transferOrderDTO.getFromWarehouse().getId().equals(existingOrder.getFromWarehouse().getId())) {
                        throw new BadRequestAlertException(
                            "Không được phép thay đổi kho xuất của phiếu điều chuyển!",
                            "transferOrder",
                            "from_warehouse_immutable"
                        );
                    }
                }

                //Chặn lỗi trùng kho nếu họ thay đổi kho nhập (toWarehouse) trùng với kho xuất cố định
                if (transferOrderDTO.getToWarehouse() != null) {
                    Long fromWarehouseId = existingOrder.getFromWarehouse().getId();
                    if (transferOrderDTO.getToWarehouse().getId().equals(fromWarehouseId)) {
                        throw new BadRequestAlertException(
                            "Kho xuất và Kho nhập không được trùng nhau!",
                            "transferOrder",
                            "same_warehouse"
                        );
                    }
                }

                //Chặn việc tự ý đổi trạng thái trực tiếp qua PATCH
                if (transferOrderDTO.getStatus() != null && existingOrder.getStatus() != transferOrderDTO.getStatus()) {
                    throw new BadRequestAlertException("Không được đổi trạng thái ở đây!", "transferOrder", "status_change_forbidden");
                }

                transferOrderMapper.partialUpdate(existingOrder, transferOrderDTO);
                return existingOrder;
            })
            .map(transferOrderRepository::save)
            .map(savedOrder -> {
                // Xử lý cập nhật danh sách mặt hàng nếu có gửi kèm
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
        boolean isAccountant = SecurityUtils.hasCurrentUserThisAuthority(AuthoritiesConstants.ACCOUNTANT);

        // TẦNG 1: CHỈ CÓ ADMIN và KẾ TOÁN mới được xem toàn bộ các phiếu điều chuyển của tất cả các kho
        if (isAdmin || isAccountant) {
            if (eager) return transferOrderRepository.findAllWithEagerRelationships(pageable).map(transferOrderMapper::toDto);
            return transferOrderRepository.findAll(pageable).map(transferOrderMapper::toDto);
        }

        // TẦNG 2: Manager (Quản lý chi nhánh) và Warehouse (Thủ kho) -> Chỉ xem phiếu của kho mình
        String currentUserLogin = SecurityUtils.getCurrentUserLogin().orElseThrow(() -> new RuntimeException("Chưa đăng nhập!"));
        return transferOrderRepository.findAllByEmployeeScopedWarehouse(currentUserLogin, pageable).map(transferOrderMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<TransferOrderDTO> findOne(Long id) {
        log.debug("Request to get TransferOrder : {}", id);

        boolean isAdmin = SecurityUtils.hasCurrentUserThisAuthority(AuthoritiesConstants.ADMIN);
        boolean isAccountant = SecurityUtils.hasCurrentUserThisAuthority(AuthoritiesConstants.ACCOUNTANT);

        // TẦNG 1: Chỉ ADMIN và KẾ TOÁN xem được mọi phiếu
        if (isAdmin || isAccountant) {
            return transferOrderRepository.findOneWithEagerRelationships(id).map(transferOrderMapper::toDto);
        }
        // TẦNG 2: Quản lý và Nhân viên bị ép qua hàm Repo có kiểm tra bảo mật kho
        String currentUserLogin = SecurityUtils.getCurrentUserLogin().orElseThrow(() -> new RuntimeException("Chưa đăng nhập!"));
        return transferOrderRepository.findOneByIdAndEmployeeScopedWarehouse(id, currentUserLogin).map(transferOrderMapper::toDto);
    }

    @Override
    public void delete(Long id) {
        log.debug("Request to delete TransferOrder : {}", id);
        TransferOrder order = transferOrderRepository
            .findById(id)
            .orElseThrow(() -> new BadRequestAlertException("Không tìm thấy phiếu điều chuyển", "transferOrder", "id_not_found"));

        //Chặn xóa phiếu đã duyệt
        if (order.getStatus() != OrderStatus.DRAFT) {
            throw new BadRequestAlertException(
                "Đại kỵ! Chỉ được xóa vĩnh viễn phiếu điều chuyển ở trạng thái NHÁP (DRAFT).",
                "transferOrder",
                "cannot_delete_processed_order"
            );
        }

        //Cấm xóa phiếu của kho khác (Áp dụng cho cả Thủ kho và Quản lý chi nhánh)
        validateWarehouseManagerAccess(order.getFromWarehouse() != null ? order.getFromWarehouse().getId() : null);
        transferOrderRepository.deleteById(id);
    }

    //ham xu li kho
    private void processTransferInventory(TransferOrder order) {
        log.debug("Bắt đầu xử lý điều chuyển cho phiếu: {}", order.getId());

        List<TransferOrderLine> lines = transferOrderLineRepository.findByTransferOrderId(order.getId());
        if (lines.isEmpty()) return;

        //Gom tất cả ID sản phẩm có trong phiếu
        List<Long> productIds = lines.stream().map(line -> line.getProduct().getId()).collect(Collectors.toList());

        //Kéo toàn bộ tồn kho (Nguồn và Đích) lên bộ nhớ (RAM) và đưa vào Map để tra cứu O(1)
        Map<Long, InventoryBalance> sourceBalanceMap = inventoryBalanceRepository
            .findByWarehouseIdAndProductIdIn(order.getFromWarehouse().getId(), productIds)
            .stream()
            .collect(Collectors.toMap(b -> b.getProduct().getId(), b -> b));

        Map<Long, InventoryBalance> destBalanceMap = inventoryBalanceRepository
            .findByWarehouseIdAndProductIdIn(order.getToWarehouse().getId(), productIds)
            .stream()
            .collect(Collectors.toMap(b -> b.getProduct().getId(), b -> b));

        //Chuẩn bị danh sách Batch Insert
        List<InventoryTransaction> transactionsToSave = new ArrayList<>();
        Instant now = Instant.now();

        //Bắt đầu vòng lặp xử lý logic trong RAM (Hoàn toàn không gọi DB ở đây)
        for (TransferOrderLine line : lines) {
            Long productId = line.getProduct().getId();

            // --- TRỪ KHO XUẤT ---
            InventoryBalance sourceBalance = sourceBalanceMap.get(productId);
            if (sourceBalance == null) {
                throw new BadRequestAlertException("Sản phẩm không có trong kho xuất!", "transferOrder", "out_of_stock");
            }
            if (sourceBalance.getQuantity() < line.getQuantity()) {
                throw new BadRequestAlertException("Kho xuất không đủ hàng!", "transferOrder", "insufficient_stock");
            }

            // Cập nhật số lượng trên bộ nhớ
            sourceBalance.setQuantity(sourceBalance.getQuantity() - line.getQuantity());

            // Ghi nhận lịch sử xuất
            InventoryTransaction issueTx = new InventoryTransaction();
            issueTx.setType(TransactionType.ISSUE);
            issueTx.setQuantity(line.getQuantity());
            issueTx.setReferenceId(order.getId());
            issueTx.setCreatedDate(now);
            issueTx.setProduct(line.getProduct());
            issueTx.setWarehouse(order.getFromWarehouse());
            transactionsToSave.add(issueTx);

            // --- CỘNG KHO NHẬP ---
            InventoryBalance destBalance = destBalanceMap.get(productId);
            if (destBalance != null) {
                // Cập nhật số lượng trên bộ nhớ
                destBalance.setQuantity(destBalance.getQuantity() + line.getQuantity());
            } else {
                // Nếu kho nhập chưa từng có mặt hàng này -> Tạo mới và nhét vào Map
                InventoryBalance newDestBalance = new InventoryBalance();
                newDestBalance.setProduct(line.getProduct());
                newDestBalance.setWarehouse(order.getToWarehouse());
                newDestBalance.setQuantity(line.getQuantity());
                destBalanceMap.put(productId, newDestBalance);
            }

            // Ghi nhận lịch sử nhập
            InventoryTransaction receiptTx = new InventoryTransaction();
            receiptTx.setType(TransactionType.RECEIPT);
            receiptTx.setQuantity(line.getQuantity());
            receiptTx.setReferenceId(order.getId());
            receiptTx.setCreatedDate(now);
            receiptTx.setProduct(line.getProduct());
            receiptTx.setWarehouse(order.getToWarehouse());
            transactionsToSave.add(receiptTx);
        }

        //Lưu toàn bộ dữ liệu xuống DB trong 1 lần gọi (Batch Update/Insert)
        List<InventoryBalance> allBalancesToUpdate = new ArrayList<>();
        allBalancesToUpdate.addAll(sourceBalanceMap.values());
        allBalancesToUpdate.addAll(destBalanceMap.values());

        try {
            inventoryBalanceRepository.saveAll(allBalancesToUpdate);
            inventoryTransactionRepository.saveAll(transactionsToSave);
            inventoryBalanceRepository.flush();
            inventoryTransactionRepository.flush();

            log.debug(
                "Hoàn tất xử lý tồn kho với {} truy vấn thay vì gọi lặp lại.",
                allBalancesToUpdate.size() + transactionsToSave.size()
            );
        } catch (org.springframework.orm.ObjectOptimisticLockingFailureException e) {
            log.error("Lỗi xung đột dữ liệu (Optimistic Locking) khi điều chuyển kho: {}", e.getMessage());
            throw new BadRequestAlertException(
                "Dữ liệu tồn kho của một số mặt hàng đã bị thay đổi bởi người khác trong lúc bạn đang thao tác. Vui lòng tải lại trang và thực hiện lại!",
                "transferOrder",
                "optimistic_locking_inventory_conflict"
            );
        }
    }

    // Hàm Duyệt phiếu điều chuyển
    @Override
    public TransferOrderDTO approveOrder(Long id) {
        TransferOrder order = transferOrderRepository
            .findById(id)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy phiếu điều chuyển"));

        validateWarehouseManagerAccess(order.getFromWarehouse() != null ? order.getFromWarehouse().getId() : null);

        order.setStatus(OrderStatus.APPROVED);
        order = transferOrderRepository.save(order);

        processTransferInventory(order);

        String currentLogin = SecurityUtils.getCurrentUserLogin().orElse("System");
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

        validateWarehouseManagerAccess(order.getFromWarehouse() != null ? order.getFromWarehouse().getId() : null);

        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new BadRequestAlertException("Phiếu này đã bị hủy từ trước!", "transferOrder", "already_cancelled");
        }

        // NẾU ĐÃ DUYỆT -> PHẢI ĐẢO NGƯỢC LẠI KHO
        if (order.getStatus() == OrderStatus.APPROVED || order.getStatus() == OrderStatus.COMPLETED) {
            List<TransferOrderLine> lines = transferOrderLineRepository.findByTransferOrderId(order.getId());

            if (!lines.isEmpty()) {
                //Gom tất cả ID sản phẩm
                List<Long> productIds = lines.stream().map(line -> line.getProduct().getId()).collect(Collectors.toList());

                //Kéo toàn bộ tồn kho của kho Nguồn và kho Đích lên RAM (Tránh N+1 Query)
                Map<Long, InventoryBalance> destBalanceMap = inventoryBalanceRepository
                    .findByWarehouseIdAndProductIdIn(order.getToWarehouse().getId(), productIds)
                    .stream()
                    .collect(Collectors.toMap(b -> b.getProduct().getId(), b -> b));

                Map<Long, InventoryBalance> sourceBalanceMap = inventoryBalanceRepository
                    .findByWarehouseIdAndProductIdIn(order.getFromWarehouse().getId(), productIds)
                    .stream()
                    .collect(Collectors.toMap(b -> b.getProduct().getId(), b -> b));

                List<InventoryTransaction> transactionsToSave = new ArrayList<>();
                Instant now = Instant.now();

                //Xử lý cộng trừ tồn kho trên RAM
                for (TransferOrderLine line : lines) {
                    Long productId = line.getProduct().getId();

                    // ==========================================
                    // A. THU HỒI TỪ KHO ĐÍCH (TRỪ HÀNG)
                    // ==========================================
                    InventoryBalance destBalance = destBalanceMap.get(productId);
                    if (destBalance == null) {
                        throw new RuntimeException("Không tìm thấy tồn kho đích để thu hồi đối với sản phẩm ID: " + productId);
                    }

                    // Chặn lỗi: Kho đích đã lỡ bán mất hàng điều chuyển đến
                    if (destBalance.getQuantity() < line.getQuantity()) {
                        throw new BadRequestAlertException(
                            "Kho đích không còn đủ hàng để thu hồi do đã xuất bán!",
                            "transferOrder",
                            "insufficient_dest_stock"
                        );
                    }
                    destBalance.setQuantity(destBalance.getQuantity() - line.getQuantity());

                    InventoryTransaction destTx = new InventoryTransaction();
                    destTx.setType(TransactionType.ISSUE); // Xuất trả lại
                    destTx.setQuantity(line.getQuantity());
                    destTx.setReferenceId(order.getId());
                    destTx.setCreatedDate(now);
                    destTx.setProduct(line.getProduct());
                    destTx.setWarehouse(order.getToWarehouse());
                    transactionsToSave.add(destTx);

                    // ==========================================
                    // B. HOÀN TRẢ LẠI KHO NGUỒN (CỘNG HÀNG)
                    // ==========================================
                    InventoryBalance sourceBalance = sourceBalanceMap.get(productId);
                    if (sourceBalance == null) {
                        throw new RuntimeException("Không tìm thấy tồn kho nguồn để hoàn trả đối với sản phẩm ID: " + productId);
                    }

                    sourceBalance.setQuantity(sourceBalance.getQuantity() + line.getQuantity());

                    InventoryTransaction sourceTx = new InventoryTransaction();
                    sourceTx.setType(TransactionType.RECEIPT); // Nhập nhận lại
                    sourceTx.setQuantity(line.getQuantity());
                    sourceTx.setReferenceId(order.getId());
                    sourceTx.setCreatedDate(now);
                    sourceTx.setProduct(line.getProduct());
                    sourceTx.setWarehouse(order.getFromWarehouse());
                    transactionsToSave.add(sourceTx);
                }

                //Batch Update/Insert toàn bộ dữ liệu xuống DB và xử lý Optimistic Locking
                List<InventoryBalance> allBalancesToUpdate = new ArrayList<>();
                allBalancesToUpdate.addAll(destBalanceMap.values());
                allBalancesToUpdate.addAll(sourceBalanceMap.values());

                try {
                    inventoryBalanceRepository.saveAll(allBalancesToUpdate);
                    inventoryTransactionRepository.saveAll(transactionsToSave);
                    inventoryTransactionRepository.flush();
                    inventoryBalanceRepository.flush();
                } catch (org.springframework.orm.ObjectOptimisticLockingFailureException e) {
                    log.error("Lỗi xung đột dữ liệu (Optimistic Locking) khi hủy phiếu điều chuyển: {}", e.getMessage());
                    throw new BadRequestAlertException(
                        "Dữ liệu tồn kho của một số mặt hàng đã bị thay đổi bởi người khác trong lúc bạn đang thao tác. Vui lòng tải lại trang và thực hiện lại!",
                        "transferOrder",
                        "optimistic_locking_inventory_conflict"
                    );
                }
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

    /**
     * Hàm dùng chung để kiểm tra quyền của Quản lý / Thủ kho
     * - Yêu cầu tài khoản phải tồn tại hồ sơ nhân viên hệ thống.
     * - Bỏ qua kiểm tra phòng ban/kho nếu là ADMIN.
     * - Bắt buộc phải thuộc phòng ban WAREHOUSE.
     * - Bắt buộc phải thao tác trên đúng kho được giao (scopedWarehouse).
     */
    private void validateWarehouseManagerAccess(Long warehouseId) {
        String currentUserLogin = SecurityUtils
            .getCurrentUserLogin()
            .orElseThrow(() -> new RuntimeException("Không tìm thấy thông tin đăng nhập!"));

        Employee employee = employeeRepository
            .findByUserLogin(currentUserLogin)
            .orElseThrow(() ->
                new BadRequestAlertException(
                    "Tài khoản của bạn chưa được gắn với hồ sơ nhân viên nào!",
                    "transferOrder",
                    "employee_not_found"
                )
            );

        // Nếu là ADMIN -> Thông chốt luôn, bỏ qua các bước check nghiệp vụ kho phía dưới
        if (SecurityUtils.hasCurrentUserThisAuthority(AuthoritiesConstants.ADMIN)) {
            return;
        }

        //CHECK PHÒNG BAN: Phải là người của Phòng Kho vận
        if (
            employee.getDepartment() == null ||
            employee.getDepartment().getName() != com.mycompany.myapp.domain.enumeration.DepartmentName.WAREHOUSE
        ) {
            throw new BadRequestAlertException(
                "Tài khoản của bạn không thuộc phòng Kho vận. Bạn không có quyền thực hiện thao tác này!",
                "transferOrder",
                "invalid_department_approval"
            );
        }

        //Phiếu phải xác định kho xuất, và Nhân viên bắt buộc phải có kho quản lý
        if (warehouseId == null) {
            throw new BadRequestAlertException(
                "Phiếu điều chuyển bắt buộc phải có thông tin kho xuất!",
                "transferOrder",
                "warehouse_required"
            );
        }

        if (employee.getScopedWarehouse() == null) {
            throw new BadRequestAlertException(
                "Tài khoản của bạn chưa được phân công về quản lý kho nào. Vui lòng liên hệ bộ phận nhân sự!",
                "transferOrder",
                "no_scoped_warehouse"
            );
        }

        if (!employee.getScopedWarehouse().getId().equals(warehouseId)) {
            throw new BadRequestAlertException(
                "Bạn không có quyền thao tác trên phiếu của kho mà bạn không quản lý!",
                "transferOrder",
                "warehouse_access_denied"
            );
        }
    }
}
