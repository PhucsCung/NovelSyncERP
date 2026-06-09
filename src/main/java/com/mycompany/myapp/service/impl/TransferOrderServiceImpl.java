package com.mycompany.myapp.service.impl;

import com.mycompany.myapp.domain.*;
import com.mycompany.myapp.domain.enumeration.DepartmentName;
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

    private static final String ENTITY_NAME = "transferOrder";

    private final TransferOrderRepository transferOrderRepository;
    private final TransferOrderMapper transferOrderMapper;
    private final TransferOrderLineRepository transferOrderLineRepository;
    private final TransferOrderLineMapper transferOrderLineMapper;
    private final InventoryTransactionRepository inventoryTransactionRepository;
    private final InventoryBalanceRepository inventoryBalanceRepository;
    private final EmployeeRepository employeeRepository;
    private final ApplicationEventPublisher eventPublisher;

    public TransferOrderServiceImpl(
        TransferOrderRepository transferOrderRepository,
        TransferOrderMapper transferOrderMapper,
        TransferOrderLineRepository transferOrderLineRepository,
        TransferOrderLineMapper transferOrderLineMapper,
        InventoryTransactionRepository inventoryTransactionRepository,
        InventoryBalanceRepository inventoryBalanceRepository,
        EmployeeRepository employeeRepository,
        ApplicationEventPublisher eventPublisher
    ) {
        this.transferOrderRepository = transferOrderRepository;
        this.transferOrderMapper = transferOrderMapper;
        this.transferOrderLineRepository = transferOrderLineRepository;
        this.transferOrderLineMapper = transferOrderLineMapper;
        this.inventoryTransactionRepository = inventoryTransactionRepository;
        this.inventoryBalanceRepository = inventoryBalanceRepository;
        this.employeeRepository = employeeRepository;
        this.eventPublisher = eventPublisher;
    }

    private Employee validateWarehouseAccess(Long fromWarehouseId) {
        String currentUserLogin = SecurityUtils.getCurrentUserLogin().orElseThrow(() -> new RuntimeException("Chưa đăng nhập!"));

        Employee employee = employeeRepository
            .findByUserLogin(currentUserLogin)
            .orElseThrow(() -> new BadRequestAlertException("Tài khoản chưa có hồ sơ nhân viên!", ENTITY_NAME, "employee_not_found"));

        if (SecurityUtils.hasCurrentUserThisAuthority(AuthoritiesConstants.ADMIN)) {
            return employee;
        }

        // Bắt buộc phải là nhân viên phòng Kho vận (WAREHOUSE)
        if (
            employee.getDepartment() == null ||
            employee.getDepartment().getName() != com.mycompany.myapp.domain.enumeration.DepartmentName.WAREHOUSE
        ) {
            throw new BadRequestAlertException("Tài khoản của bạn không thuộc phòng Kho vận!", ENTITY_NAME, "invalid_department");
        }

        // Phải được phân công kho và thao tác đúng kho xuất
        if (employee.getScopedWarehouse() == null) {
            throw new BadRequestAlertException("Tài khoản chưa được phân công về kho nào!", ENTITY_NAME, "no_scoped_warehouse");
        }

        if (!employee.getScopedWarehouse().getId().equals(fromWarehouseId)) {
            throw new BadRequestAlertException(
                "Bạn không có quyền xuất hàng từ kho mà bạn không quản lý!",
                ENTITY_NAME,
                "warehouse_access_denied"
            );
        }

        return employee;
    }

    private Employee validateReceivingWarehouseAccess(Long toWarehouseId) {
        String currentUserLogin = SecurityUtils.getCurrentUserLogin().orElseThrow(() -> new RuntimeException("Chưa đăng nhập!"));
        Employee employee = employeeRepository
            .findByUserLogin(currentUserLogin)
            .orElseThrow(() -> new BadRequestAlertException("Tài khoản chưa có hồ sơ nhân viên!", ENTITY_NAME, "employee_not_found"));

        if (SecurityUtils.hasCurrentUserThisAuthority(AuthoritiesConstants.ADMIN)) return employee;

        if (employee.getScopedWarehouse() == null || !employee.getScopedWarehouse().getId().equals(toWarehouseId)) {
            throw new BadRequestAlertException(
                "Bạn không quản lý kho nhập này, không thể bấm nhận hàng!",
                ENTITY_NAME,
                "warehouse_access_denied"
            );
        }
        return employee;
    }

    private void checkOrderOwnership(TransferOrder order) {
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
                    "Bạn không có quyền sửa/xóa phiếu điều chuyển của người khác!",
                    ENTITY_NAME,
                    "access_denied"
                );
            }
        }
    }

    @Override
    public TransferOrderDTO save(TransferOrderDTO transferOrderDTO) {
        log.debug("Request to save TransferOrder : {}", transferOrderDTO);

        if (transferOrderDTO.getFromWarehouse() == null || transferOrderDTO.getToWarehouse() == null) {
            throw new BadRequestAlertException("Phải chọn đầy đủ Kho xuất và Kho nhập!", ENTITY_NAME, "warehouses_required");
        }

        if (transferOrderDTO.getFromWarehouse().getId().equals(transferOrderDTO.getToWarehouse().getId())) {
            throw new BadRequestAlertException("Kho nhập và Kho xuất không được trùng nhau!", ENTITY_NAME, "warehouses_must_be_different");
        }

        Employee currentEmployee = validateWarehouseAccess(transferOrderDTO.getFromWarehouse().getId());

        if (transferOrderDTO.getTransferOrderLines() == null || transferOrderDTO.getTransferOrderLines().isEmpty()) {
            throw new BadRequestAlertException("Phiếu điều chuyển phải có ít nhất 1 mặt hàng!", ENTITY_NAME, "empty_lines");
        }

        // Tự động sinh mã (Ví dụ TO-171822...)
        transferOrderDTO.setTransferCode("TO-" + Instant.now().toEpochMilli());
        transferOrderDTO.setStatus(OrderStatus.DRAFT);

        for (TransferOrderLineDTO line : transferOrderDTO.getTransferOrderLines()) {
            if (line.getQuantity() == null || line.getQuantity() <= 0) {
                throw new BadRequestAlertException("Số lượng điều chuyển phải lớn hơn 0!", ENTITY_NAME, "invalid_quantity");
            }
        }

        TransferOrder transferOrder = transferOrderMapper.toEntity(transferOrderDTO);
        transferOrder.setEmployee(currentEmployee);
        transferOrder = transferOrderRepository.save(transferOrder);

        List<TransferOrderLine> linesToSave = new ArrayList<>();
        for (TransferOrderLineDTO lineDTO : transferOrderDTO.getTransferOrderLines()) {
            TransferOrderLine line = transferOrderLineMapper.toEntity(lineDTO);
            line.setTransferOrder(transferOrder);
            linesToSave.add(line);
        }
        transferOrderLineRepository.saveAll(linesToSave);

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
            .orElseThrow(() -> new BadRequestAlertException("Không tìm thấy phiếu điều chuyển", ENTITY_NAME, "id_not_found"));

        validateWarehouseAccess(oldOrder.getFromWarehouse().getId());
        checkOrderOwnership(oldOrder);

        if (oldOrder.getStatus() != OrderStatus.DRAFT) {
            throw new BadRequestAlertException(
                "Chỉ được phép chỉnh sửa phiếu ở trạng thái NHÁP!",
                ENTITY_NAME,
                "cannot_edit_processed_order"
            );
        }

        if (
            !transferOrderDTO.getFromWarehouse().getId().equals(oldOrder.getFromWarehouse().getId()) ||
            !transferOrderDTO.getToWarehouse().getId().equals(oldOrder.getToWarehouse().getId())
        ) {
            throw new BadRequestAlertException("Không được đổi Kho nhập/Kho xuất của phiếu đã tạo!", ENTITY_NAME, "warehouses_immutable");
        }

        transferOrderDTO.setTransferCode(oldOrder.getTransferCode());
        transferOrderDTO.setStatus(OrderStatus.DRAFT);

        TransferOrder transferOrder = transferOrderMapper.toEntity(transferOrderDTO);
        transferOrder.setEmployee(oldOrder.getEmployee());
        transferOrder = transferOrderRepository.save(transferOrder);

        List<TransferOrderLine> oldLines = transferOrderLineRepository.findByTransferOrderId(transferOrder.getId());
        transferOrderLineRepository.deleteAll(oldLines);

        List<TransferOrderLine> newLines = new ArrayList<>();
        for (TransferOrderLineDTO lineDTO : transferOrderDTO.getTransferOrderLines()) {
            TransferOrderLine line = transferOrderLineMapper.toEntity(lineDTO);
            line.setTransferOrder(transferOrder);
            newLines.add(line);
        }
        transferOrderLineRepository.saveAll(newLines);

        return transferOrderMapper.toDto(transferOrder);
    }

    @Override
    public Optional<TransferOrderDTO> partialUpdate(TransferOrderDTO transferOrderDTO) {
        log.debug("Request to partially update TransferOrder : {}", transferOrderDTO);
        return transferOrderRepository
            .findById(transferOrderDTO.getId())
            .map(existingOrder -> {
                validateWarehouseAccess(existingOrder.getFromWarehouse().getId());
                checkOrderOwnership(existingOrder);

                if (existingOrder.getStatus() != OrderStatus.DRAFT) {
                    throw new BadRequestAlertException(
                        "Chỉ được phép chỉnh sửa phiếu ở trạng thái NHÁP!",
                        ENTITY_NAME,
                        "cannot_edit_processed_order"
                    );
                }

                // Chặn sửa code và status
                if (
                    transferOrderDTO.getTransferCode() != null &&
                    !transferOrderDTO.getTransferCode().equals(existingOrder.getTransferCode())
                ) {
                    throw new BadRequestAlertException("Không được đổi mã phiếu!", ENTITY_NAME, "code_immutable");
                }

                transferOrderMapper.partialUpdate(existingOrder, transferOrderDTO);
                return existingOrder;
            })
            .map(transferOrderRepository::save)
            .map(savedOrder -> {
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
        return getFilteredOrders(pageable, false);
    }

    public Page<TransferOrderDTO> findAllWithEagerRelationships(Pageable pageable) {
        log.debug("Request to get all TransferOrders with eager relationships and Data Filtering");
        return getFilteredOrders(pageable, true);
    }

    private Page<TransferOrderDTO> getFilteredOrders(Pageable pageable, boolean eager) {
        boolean isAdmin = SecurityUtils.hasCurrentUserThisAuthority(AuthoritiesConstants.ADMIN);
        // boolean isManager =
        // SecurityUtils.hasCurrentUserThisAuthority(AuthoritiesConstants.MANAGER);
        // boolean isShipper =
        // SecurityUtils.hasCurrentUserThisAuthority(AuthoritiesConstants.SHIPPER);

        if (isAdmin) {
            if (eager) return transferOrderRepository.findAllWithEagerRelationships(pageable).map(transferOrderMapper::toDto);
            return transferOrderRepository.findAll(pageable).map(transferOrderMapper::toDto);
        }

        String currentUserLogin = SecurityUtils.getCurrentUserLogin().orElseThrow();
        // Quản lý hoặc Thủ kho đều lọc theo kho của họ
        return transferOrderRepository.findAllByEmployeeScopedWarehouse(currentUserLogin, pageable).map(transferOrderMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<TransferOrderDTO> findOne(Long id) {
        log.debug("Request to get TransferOrder : {}", id);
        boolean isAdmin = SecurityUtils.hasCurrentUserThisAuthority(AuthoritiesConstants.ADMIN);
        if (isAdmin) {
            return transferOrderRepository.findOneWithEagerRelationships(id).map(transferOrderMapper::toDto);
        }

        String currentUserLogin = SecurityUtils.getCurrentUserLogin().orElseThrow();
        return transferOrderRepository.findOneByIdAndEmployeeScopedWarehouse(id, currentUserLogin).map(transferOrderMapper::toDto);
    }

    @Override
    public void delete(Long id) {
        log.debug("Request to delete TransferOrder : {}", id);
        TransferOrder order = transferOrderRepository
            .findById(id)
            .orElseThrow(() -> new BadRequestAlertException("Không tìm thấy phiếu điều chuyển", ENTITY_NAME, "id_not_found"));

        validateWarehouseAccess(order.getFromWarehouse().getId());
        checkOrderOwnership(order);

        if (order.getStatus() != OrderStatus.DRAFT) {
            throw new BadRequestAlertException("Chỉ được xóa phiếu điều chuyển NHÁP!", ENTITY_NAME, "cannot_delete_processed_order");
        }

        List<TransferOrderLine> lines = transferOrderLineRepository.findByTransferOrderId(id);
        transferOrderLineRepository.deleteAll(lines);
        transferOrderRepository.deleteById(id);
    }

    @Transactional
    @Override
    public TransferOrderDTO approveOrder(Long id) {
        TransferOrder order = transferOrderRepository.findById(id).orElseThrow();
        validateWarehouseAccess(order.getFromWarehouse().getId());
        if (order.getStatus() == OrderStatus.APPROVED) throw new BadRequestAlertException("Đã duyệt!", ENTITY_NAME, "already_approved");

        processOutboundTransfer(order, false); // CHỈ TRỪ KHO A
        order.setStatus(OrderStatus.APPROVED);
        return transferOrderMapper.toDto(transferOrderRepository.save(order));
    }

    @Transactional
    @Override
    public TransferOrderDTO cancelOrder(Long id) {
        TransferOrder order = transferOrderRepository.findById(id).orElseThrow();
        validateWarehouseAccess(order.getFromWarehouse().getId());
        if (order.getStatus() == OrderStatus.COMPLETED) throw new BadRequestAlertException(
            "Không thể hủy phiếu đã hoàn thành!",
            ENTITY_NAME,
            "cannot_cancel_completed"
        );

        // Hàng đã xuất hoặc đang đi đường thì phải trả lại Kho A
        if (order.getStatus() == OrderStatus.APPROVED || order.getStatus() == OrderStatus.PROCESSING) {
            processOutboundTransfer(order, true);
        }
        order.setStatus(OrderStatus.CANCELLED);
        return transferOrderMapper.toDto(transferOrderRepository.save(order));
    }

    @Transactional
    @Override
    public TransferOrderDTO startDelivery(Long id) {
        TransferOrder order = transferOrderRepository.findById(id).orElseThrow();
        if (order.getStatus() != OrderStatus.APPROVED) throw new BadRequestAlertException(
            "Kho chưa xuất, chưa thể chở!",
            ENTITY_NAME,
            "invalid_status"
        );

        order.setStatus(OrderStatus.PROCESSING);
        return transferOrderMapper.toDto(transferOrderRepository.save(order));
    }

    @Transactional
    @Override
    public TransferOrderDTO confirmDelivery(Long id) {
        TransferOrder order = transferOrderRepository.findById(id).orElseThrow();
        if (order.getStatus() != OrderStatus.PROCESSING) throw new BadRequestAlertException(
            "Chưa trên đường giao!",
            ENTITY_NAME,
            "invalid_status"
        );

        // Đánh tiếng cho Kho B ra nhận hàng
        eventPublisher.publishEvent(
            new OrderNotificationEvent(
                "TRANSFER",
                "ARRIVED",
                order.getId(),
                "Xe hàng điều chuyển " + order.getTransferCode() + " đã tới. Kho B kiểm đếm và nhận hàng!",
                "System",
                "WAREHOUSE_GROUP"
            )
        );
        return transferOrderMapper.toDto(order);
    }

    @Transactional
    @Override
    public TransferOrderDTO completeOrder(Long id) {
        TransferOrder order = transferOrderRepository.findById(id).orElseThrow();
        validateReceivingWarehouseAccess(order.getToWarehouse().getId()); // KHO B BẤM NHẬN

        if (order.getStatus() != OrderStatus.PROCESSING) throw new BadRequestAlertException(
            "Hàng chưa tới nơi!",
            ENTITY_NAME,
            "invalid_status"
        );

        processInboundTransfer(order); // CỘNG KHO B
        order.setStatus(OrderStatus.COMPLETED);
        return transferOrderMapper.toDto(transferOrderRepository.save(order));
    }

    /**
     * Nhịp 1: Kho Xuất duyệt -> Trừ kho A (Hủy thì cộng lại kho A)
     */
    private void processOutboundTransfer(TransferOrder order, boolean isReverse) {
        log.debug("Xử lý Xuất kho điều chuyển (Kho A). isReverse: {}", isReverse);
        List<TransferOrderLine> lines = transferOrderLineRepository.findByTransferOrderId(order.getId());
        if (lines.isEmpty()) return;

        List<Long> productIds = lines.stream().map(line -> line.getProduct().getId()).collect(Collectors.toList());
        Map<Long, InventoryBalance> balances = inventoryBalanceRepository
            .findByWarehouseIdAndProductIdIn(order.getFromWarehouse().getId(), productIds)
            .stream()
            .collect(Collectors.toMap(b -> b.getProduct().getId(), b -> b));

        List<InventoryTransaction> transactions = new ArrayList<>();
        Instant now = Instant.now();

        for (TransferOrderLine line : lines) {
            InventoryBalance balance = balances.get(line.getProduct().getId());

            if (!isReverse) { // Trừ kho xuất đi
                if (balance == null || balance.getQuantity() < line.getQuantity()) {
                    throw new BadRequestAlertException("Kho xuất không đủ hàng!", ENTITY_NAME, "insufficient_stock");
                }
                balance.setQuantity(balance.getQuantity() - line.getQuantity());
            } else { // Hoàn kho do hủy đơn
                balance.setQuantity(balance.getQuantity() + line.getQuantity());
            }

            InventoryTransaction tx = new InventoryTransaction();
            tx.setType(isReverse ? TransactionType.TRANSFER_IN : TransactionType.TRANSFER_OUT);
            tx.setQuantity(line.getQuantity());
            tx.setReferenceId(order.getId());
            tx.setCreatedDate(now);
            tx.setProduct(line.getProduct());
            tx.setWarehouse(order.getFromWarehouse());
            transactions.add(tx);
        }
        inventoryBalanceRepository.saveAll(balances.values());
        inventoryTransactionRepository.saveAll(transactions);
    }

    /**
     * Nhịp 2: Kho Nhập nhận -> Cộng kho B
     */
    private void processInboundTransfer(TransferOrder order) {
        log.debug("Xử lý Nhập kho điều chuyển (Kho B)");
        List<TransferOrderLine> lines = transferOrderLineRepository.findByTransferOrderId(order.getId());
        if (lines.isEmpty()) return;

        List<Long> productIds = lines.stream().map(line -> line.getProduct().getId()).collect(Collectors.toList());
        Map<Long, InventoryBalance> balances = inventoryBalanceRepository
            .findByWarehouseIdAndProductIdIn(order.getToWarehouse().getId(), productIds)
            .stream()
            .collect(Collectors.toMap(b -> b.getProduct().getId(), b -> b));

        List<InventoryBalance> newBalances = new ArrayList<>();
        List<InventoryTransaction> transactions = new ArrayList<>();
        Instant now = Instant.now();

        for (TransferOrderLine line : lines) {
            InventoryBalance balance = balances.get(line.getProduct().getId());
            if (balance != null) {
                balance.setQuantity(balance.getQuantity() + line.getQuantity());
            } else {
                balance = new InventoryBalance();
                balance.setProduct(line.getProduct());
                balance.setWarehouse(order.getToWarehouse());
                balance.setQuantity(line.getQuantity());
                newBalances.add(balance);
            }

            InventoryTransaction tx = new InventoryTransaction();
            tx.setType(TransactionType.TRANSFER_IN);
            tx.setQuantity(line.getQuantity());
            tx.setReferenceId(order.getId());
            tx.setCreatedDate(now);
            tx.setProduct(line.getProduct());
            tx.setWarehouse(order.getToWarehouse());
            transactions.add(tx);
        }
        inventoryBalanceRepository.saveAll(balances.values());
        inventoryBalanceRepository.saveAll(newBalances);
        inventoryTransactionRepository.saveAll(transactions);
    }
}
