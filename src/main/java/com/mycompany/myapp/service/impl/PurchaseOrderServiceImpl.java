package com.mycompany.myapp.service.impl;

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
 * Service Implementation for managing {@link PurchaseOrder}.
 */
@Service
@Transactional
public class PurchaseOrderServiceImpl implements PurchaseOrderService {

    private final Logger log = LoggerFactory.getLogger(PurchaseOrderServiceImpl.class);

    private static final String ENTITY_NAME = "purchaseOrder";

    private final PurchaseOrderRepository purchaseOrderRepository;
    private final PurchaseOrderMapper purchaseOrderMapper;
    private final PurchaseOrderLineRepository purchaseOrderLineRepository;
    private final InventoryTransactionRepository inventoryTransactionRepository;
    private final InventoryBalanceRepository inventoryBalanceRepository;
    private final PurchaseOrderLineMapper purchaseOrderLineMapper;
    private final SupplierRepository supplierRepository;
    private final WarehouseRepository warehouseRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final EmployeeRepository employeeRepository;
    private final ProductRepository productRepository;

    public PurchaseOrderServiceImpl(
        PurchaseOrderRepository purchaseOrderRepository,
        PurchaseOrderMapper purchaseOrderMapper,
        PurchaseOrderLineRepository purchaseOrderLineRepository,
        InventoryTransactionRepository inventoryTransactionRepository,
        InventoryBalanceRepository inventoryBalanceRepository,
        PurchaseOrderLineMapper purchaseOrderLineMapper,
        SupplierRepository supplierRepository,
        WarehouseRepository warehouseRepository,
        ApplicationEventPublisher eventPublisher,
        EmployeeRepository employeeRepository,
        ProductRepository productRepository
    ) {
        this.purchaseOrderRepository = purchaseOrderRepository;
        this.purchaseOrderMapper = purchaseOrderMapper;
        this.purchaseOrderLineRepository = purchaseOrderLineRepository;
        this.inventoryTransactionRepository = inventoryTransactionRepository;
        this.inventoryBalanceRepository = inventoryBalanceRepository;
        this.purchaseOrderLineMapper = purchaseOrderLineMapper;
        this.supplierRepository = supplierRepository;
        this.warehouseRepository = warehouseRepository;
        this.eventPublisher = eventPublisher;
        this.employeeRepository = employeeRepository;
        this.productRepository = productRepository;
    }

    private Employee validatePurchaserAccess(Long warehouseId) {
        String currentUserLogin = SecurityUtils.getCurrentUserLogin().orElseThrow(() -> new RuntimeException("Chưa đăng nhập!"));

        Employee employee = employeeRepository
            .findByUserLogin(currentUserLogin)
            .orElseThrow(() ->
                new BadRequestAlertException("Tài khoản của bạn chưa được gắn với hồ sơ nhân viên nào!", ENTITY_NAME, "employee_not_found")
            );

        if (SecurityUtils.hasCurrentUserThisAuthority(AuthoritiesConstants.ADMIN)) {
            return employee;
        }

        // Bắt buộc thuộc phòng Mua hàng (PURCHASE)
        if (
            employee.getDepartment() == null ||
            employee.getDepartment().getName() != com.mycompany.myapp.domain.enumeration.DepartmentName.PURCHASE
        ) {
            throw new BadRequestAlertException("Tài khoản của bạn không thuộc phòng Mua hàng!", ENTITY_NAME, "invalid_department");
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
                "Bạn không có quyền thao tác trên đơn hàng của chi nhánh khác!",
                ENTITY_NAME,
                "warehouse_access_denied"
            );
        }

        return employee;
    }

    private void checkOrderOwnership(PurchaseOrder order) {
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
                    "Bạn không có quyền thao tác trên đơn mua hàng của người khác!",
                    ENTITY_NAME,
                    "access_denied"
                );
            }
        }
    }

    @Override
    public PurchaseOrderDTO save(PurchaseOrderDTO purchaseOrderDTO) {
        log.debug("Request to save PurchaseOrder : {}", purchaseOrderDTO);

        if (purchaseOrderDTO.getSupplier() == null || purchaseOrderDTO.getSupplier().getId() == null) {
            throw new BadRequestAlertException("Đơn mua hàng bắt buộc phải chọn Nhà cung cấp!", ENTITY_NAME, "supplier_required");
        }
        if (purchaseOrderDTO.getWarehouse() == null || purchaseOrderDTO.getWarehouse().getId() == null) {
            throw new BadRequestAlertException("Đơn mua hàng bắt buộc phải chọn Kho nhập hàng!", ENTITY_NAME, "warehouse_required");
        }

        Employee currentEmployee = validatePurchaserAccess(purchaseOrderDTO.getWarehouse().getId());

        if (purchaseOrderDTO.getPurchaseOrderLines() == null || purchaseOrderDTO.getPurchaseOrderLines().isEmpty()) {
            throw new BadRequestAlertException("Đơn mua hàng phải có ít nhất một mặt hàng!", ENTITY_NAME, "empty_lines");
        }

        // Backend tự sinh mã PO (Purchase Order)
        purchaseOrderDTO.setPoCode("PO-" + Instant.now().toEpochMilli());
        purchaseOrderDTO.setStatus(OrderStatus.DRAFT);

        List<Long> productIds = purchaseOrderDTO
            .getPurchaseOrderLines()
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
        for (PurchaseOrderLineDTO line : purchaseOrderDTO.getPurchaseOrderLines()) {
            Product product = productMap.get(line.getProduct().getId());
            if (product == null) {
                throw new BadRequestAlertException(
                    "Không tìm thấy sản phẩm có ID: " + line.getProduct().getId(),
                    ENTITY_NAME,
                    "product_not_found"
                );
            }

            // ÉP CỨNG GIÁ NHẬP (PURCHASE PRICE) TỪ DB ĐỂ CHỐNG HACK
            line.setUnitPrice(product.getPurchasePrice());

            if (line.getQuantity() == null) {
                throw new BadRequestAlertException("Số lượng không hợp lệ!", ENTITY_NAME, "invalid_quantity");
            }
            BigDecimal lineTotal = line.getUnitPrice().multiply(new BigDecimal(line.getQuantity()));
            calculatedTotal = calculatedTotal.add(lineTotal);
        }

        if (purchaseOrderDTO.getTotalAmount() == null || purchaseOrderDTO.getTotalAmount().compareTo(calculatedTotal) != 0) {
            throw new BadRequestAlertException("Tổng tiền đơn hàng không hợp lệ!", ENTITY_NAME, "total_amount_mismatch");
        }

        Supplier supplier = supplierRepository
            .findById(purchaseOrderDTO.getSupplier().getId())
            .orElseThrow(() -> new BadRequestAlertException("Supplier not found", ENTITY_NAME, "supplier_not_found"));
        Warehouse warehouse = warehouseRepository
            .findById(purchaseOrderDTO.getWarehouse().getId())
            .orElseThrow(() -> new BadRequestAlertException("Warehouse not found", ENTITY_NAME, "warehouse_not_found"));

        PurchaseOrder purchaseOrder = purchaseOrderMapper.toEntity(purchaseOrderDTO);
        purchaseOrder.setSupplier(supplier);
        purchaseOrder.setWarehouse(warehouse);
        purchaseOrder.setEmployee(currentEmployee);
        purchaseOrder = purchaseOrderRepository.save(purchaseOrder);

        List<PurchaseOrderLine> linesToSave = new ArrayList<>();
        for (PurchaseOrderLineDTO lineDTO : purchaseOrderDTO.getPurchaseOrderLines()) {
            PurchaseOrderLine line = purchaseOrderLineMapper.toEntity(lineDTO);
            line.setProduct(productMap.get(lineDTO.getProduct().getId()));
            line.setPurchaseOrder(purchaseOrder);
            linesToSave.add(line);
        }
        purchaseOrderLineRepository.saveAll(linesToSave);

        PurchaseOrderDTO result = purchaseOrderMapper.toDto(purchaseOrder);
        String currentLogin = SecurityUtils.getCurrentUserLogin().orElse("System");
        eventPublisher.publishEvent(
            new OrderNotificationEvent("PURCHASE", "CREATED", result.getId(), result.getPoCode(), currentLogin, currentLogin)
        );

        return result;
    }

    @Override
    public PurchaseOrderDTO update(PurchaseOrderDTO purchaseOrderDTO) {
        log.debug("Request to update PurchaseOrder : {}", purchaseOrderDTO);

        if (purchaseOrderDTO.getSupplier() == null || purchaseOrderDTO.getSupplier().getId() == null) {
            throw new BadRequestAlertException("Đơn mua hàng bắt buộc phải chọn Nhà cung cấp!", ENTITY_NAME, "supplier_required");
        }
        if (purchaseOrderDTO.getWarehouse() == null || purchaseOrderDTO.getWarehouse().getId() == null) {
            throw new BadRequestAlertException("Đơn mua hàng bắt buộc phải chọn Kho nhập hàng!", ENTITY_NAME, "warehouse_required");
        }
        if (purchaseOrderDTO.getPurchaseOrderLines() == null || purchaseOrderDTO.getPurchaseOrderLines().isEmpty()) {
            throw new BadRequestAlertException("Đơn mua hàng phải có ít nhất một mặt hàng!", ENTITY_NAME, "empty_lines");
        }

        PurchaseOrder oldOrder = purchaseOrderRepository
            .findById(purchaseOrderDTO.getId())
            .orElseThrow(() -> new BadRequestAlertException("Không tìm thấy đơn hàng", ENTITY_NAME, "id_not_found"));

        validatePurchaserAccess(oldOrder.getWarehouse().getId());
        checkOrderOwnership(oldOrder);

        if (oldOrder.getStatus() != OrderStatus.DRAFT) {
            throw new BadRequestAlertException(
                "Chỉ được phép chỉnh sửa đơn hàng ở trạng thái NHÁP (DRAFT)!",
                ENTITY_NAME,
                "cannot_edit_processed_order"
            );
        }

        if (!purchaseOrderDTO.getWarehouse().getId().equals(oldOrder.getWarehouse().getId())) {
            throw new BadRequestAlertException(
                "Không được phép thay đổi kho nhập của đơn mua hàng đã tạo!",
                ENTITY_NAME,
                "warehouse_immutable"
            );
        }

        purchaseOrderDTO.setPoCode(oldOrder.getPoCode());
        purchaseOrderDTO.setStatus(OrderStatus.DRAFT);

        List<Long> productIds = purchaseOrderDTO
            .getPurchaseOrderLines()
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
        for (PurchaseOrderLineDTO line : purchaseOrderDTO.getPurchaseOrderLines()) {
            Product product = productMap.get(line.getProduct().getId());
            if (product == null) {
                throw new BadRequestAlertException(
                    "Không tìm thấy sản phẩm có ID: " + line.getProduct().getId(),
                    ENTITY_NAME,
                    "product_not_found"
                );
            }

            line.setUnitPrice(product.getPurchasePrice());

            if (line.getQuantity() == null) {
                throw new BadRequestAlertException("Số lượng không hợp lệ!", ENTITY_NAME, "invalid_quantity");
            }
            BigDecimal lineTotal = line.getUnitPrice().multiply(new BigDecimal(line.getQuantity()));
            calculatedTotal = calculatedTotal.add(lineTotal);
        }

        if (purchaseOrderDTO.getTotalAmount() == null || purchaseOrderDTO.getTotalAmount().compareTo(calculatedTotal) != 0) {
            throw new BadRequestAlertException("Tổng tiền đơn hàng không hợp lệ!", ENTITY_NAME, "total_amount_mismatch");
        }

        Supplier supplier = supplierRepository
            .findById(purchaseOrderDTO.getSupplier().getId())
            .orElseThrow(() -> new BadRequestAlertException("Supplier not found", ENTITY_NAME, "supplier_not_found"));
        Warehouse warehouse = warehouseRepository
            .findById(purchaseOrderDTO.getWarehouse().getId())
            .orElseThrow(() -> new BadRequestAlertException("Warehouse not found", ENTITY_NAME, "warehouse_not_found"));

        PurchaseOrder purchaseOrder = purchaseOrderMapper.toEntity(purchaseOrderDTO);
        purchaseOrder.setSupplier(supplier);
        purchaseOrder.setWarehouse(warehouse);
        purchaseOrder.setEmployee(oldOrder.getEmployee());
        purchaseOrder = purchaseOrderRepository.save(purchaseOrder);

        List<PurchaseOrderLine> oldLines = purchaseOrderLineRepository.findByPurchaseOrderId(purchaseOrder.getId());
        purchaseOrderLineRepository.deleteAll(oldLines);

        List<PurchaseOrderLine> newLines = new ArrayList<>();
        for (PurchaseOrderLineDTO lineDTO : purchaseOrderDTO.getPurchaseOrderLines()) {
            PurchaseOrderLine line = purchaseOrderLineMapper.toEntity(lineDTO);
            line.setProduct(productMap.get(lineDTO.getProduct().getId()));
            line.setPurchaseOrder(purchaseOrder);
            newLines.add(line);
        }
        purchaseOrderLineRepository.saveAll(newLines);

        return purchaseOrderMapper.toDto(purchaseOrder);
    }

    @Override
    public Optional<PurchaseOrderDTO> partialUpdate(PurchaseOrderDTO purchaseOrderDTO) {
        log.debug("Request to partially update PurchaseOrder : {}", purchaseOrderDTO);

        return purchaseOrderRepository
            .findById(purchaseOrderDTO.getId())
            .map(existingOrder -> {
                validatePurchaserAccess(existingOrder.getWarehouse().getId());
                checkOrderOwnership(existingOrder);

                if (existingOrder.getStatus() != OrderStatus.DRAFT) {
                    throw new BadRequestAlertException(
                        "Chỉ được phép chỉnh sửa đơn hàng ở trạng thái NHÁP!",
                        ENTITY_NAME,
                        "cannot_edit_processed_order"
                    );
                }

                if (
                    purchaseOrderDTO.getWarehouse() != null &&
                    !purchaseOrderDTO.getWarehouse().getId().equals(existingOrder.getWarehouse().getId())
                ) {
                    throw new BadRequestAlertException("Không được phép thay đổi kho nhập!", ENTITY_NAME, "warehouse_immutable");
                }
                if (purchaseOrderDTO.getPoCode() != null && !purchaseOrderDTO.getPoCode().equals(existingOrder.getPoCode())) {
                    throw new BadRequestAlertException("Không được phép thay đổi mã đơn!", ENTITY_NAME, "po_code_immutable");
                }
                if (purchaseOrderDTO.getStatus() != null && purchaseOrderDTO.getStatus() != OrderStatus.DRAFT) {
                    throw new BadRequestAlertException("Không được đổi trạng thái qua API này!", ENTITY_NAME, "status_change_forbidden");
                }

                if (purchaseOrderDTO.getPurchaseOrderLines() != null) {
                    if (purchaseOrderDTO.getPurchaseOrderLines().isEmpty()) {
                        throw new BadRequestAlertException("Đơn mua hàng phải có ít nhất một mặt hàng!", ENTITY_NAME, "empty_lines");
                    }

                    List<Long> productIds = purchaseOrderDTO
                        .getPurchaseOrderLines()
                        .stream()
                        .map(line -> {
                            if (line.getProduct() == null || line.getProduct().getId() == null) {
                                throw new BadRequestAlertException(
                                    "Mặt hàng không được để trống sản phẩm!",
                                    ENTITY_NAME,
                                    "invalid_product"
                                );
                            }
                            return line.getProduct().getId();
                        })
                        .collect(Collectors.toList());

                    Map<Long, Product> productMap = productRepository
                        .findAllById(productIds)
                        .stream()
                        .collect(Collectors.toMap(Product::getId, p -> p));

                    BigDecimal calculatedTotal = BigDecimal.ZERO;
                    for (PurchaseOrderLineDTO line : purchaseOrderDTO.getPurchaseOrderLines()) {
                        Product product = productMap.get(line.getProduct().getId());
                        if (product == null) {
                            throw new BadRequestAlertException(
                                "Không tìm thấy sản phẩm có ID: " + line.getProduct().getId(),
                                ENTITY_NAME,
                                "product_not_found"
                            );
                        }

                        // Ép cứng giá nhập từ bảng Product
                        line.setUnitPrice(product.getPurchasePrice());

                        if (line.getQuantity() == null) {
                            throw new BadRequestAlertException("Mặt hàng không được để trống số lượng!", ENTITY_NAME, "invalid_line_data");
                        }
                        calculatedTotal = calculatedTotal.add(line.getUnitPrice().multiply(new BigDecimal(line.getQuantity())));
                    }
                    if (purchaseOrderDTO.getTotalAmount() != null && purchaseOrderDTO.getTotalAmount().compareTo(calculatedTotal) != 0) {
                        throw new BadRequestAlertException("Tổng tiền đơn hàng không hợp lệ!", ENTITY_NAME, "total_amount_mismatch");
                    }
                    purchaseOrderDTO.setTotalAmount(calculatedTotal);
                }

                purchaseOrderMapper.partialUpdate(existingOrder, purchaseOrderDTO);
                return existingOrder;
            })
            .map(purchaseOrderRepository::save)
            .map(savedOrder -> {
                if (purchaseOrderDTO.getPurchaseOrderLines() != null) {
                    List<PurchaseOrderLine> oldLines = purchaseOrderLineRepository.findByPurchaseOrderId(savedOrder.getId());
                    purchaseOrderLineRepository.deleteAll(oldLines);

                    List<PurchaseOrderLine> newLines = new ArrayList<>();
                    for (PurchaseOrderLineDTO lineDTO : purchaseOrderDTO.getPurchaseOrderLines()) {
                        PurchaseOrderLine line = purchaseOrderLineMapper.toEntity(lineDTO);
                        Product product = productRepository
                            .findById(lineDTO.getProduct().getId())
                            .orElseThrow(() -> new BadRequestAlertException("Product not found", ENTITY_NAME, "product_not_found"));
                        line.setProduct(product);
                        line.setPurchaseOrder(savedOrder);
                        newLines.add(line);
                    }
                    purchaseOrderLineRepository.saveAll(newLines);
                }
                return purchaseOrderMapper.toDto(savedOrder);
            });
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

    private Page<PurchaseOrderDTO> getFilteredPurchaseOrders(Pageable pageable, boolean eager) {
        boolean isAdmin = SecurityUtils.hasCurrentUserThisAuthority(AuthoritiesConstants.ADMIN);
        boolean isAccountant = SecurityUtils.hasCurrentUserThisAuthority(AuthoritiesConstants.ACCOUNTANT);
        boolean isManager = SecurityUtils.hasCurrentUserThisAuthority(AuthoritiesConstants.MANAGER);

        if (isAdmin || isAccountant) {
            if (eager) return purchaseOrderRepository.findAllWithEagerRelationships(pageable).map(purchaseOrderMapper::toDto);
            return purchaseOrderRepository.findAll(pageable).map(purchaseOrderMapper::toDto);
        }

        String currentUserLogin = SecurityUtils.getCurrentUserLogin().orElseThrow(() -> new RuntimeException("Chưa đăng nhập!"));

        if (isManager) {
            return purchaseOrderRepository.findAllByEmployeeScopedWarehouse(currentUserLogin, pageable).map(purchaseOrderMapper::toDto);
        }

        return purchaseOrderRepository.findAllByEmployeeUserLogin(currentUserLogin, pageable).map(purchaseOrderMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<PurchaseOrderDTO> findOne(Long id) {
        log.debug("Request to get PurchaseOrder : {}", id);

        boolean isAdmin = SecurityUtils.hasCurrentUserThisAuthority(AuthoritiesConstants.ADMIN);
        boolean isAccountant = SecurityUtils.hasCurrentUserThisAuthority(AuthoritiesConstants.ACCOUNTANT);
        boolean isManager = SecurityUtils.hasCurrentUserThisAuthority(AuthoritiesConstants.MANAGER);

        if (isAdmin || isAccountant) {
            return purchaseOrderRepository.findOneWithEagerRelationships(id).map(purchaseOrderMapper::toDto);
        }

        String currentUserLogin = SecurityUtils.getCurrentUserLogin().orElseThrow();

        if (isManager) {
            return purchaseOrderRepository.findOneByIdAndEmployeeScopedWarehouse(id, currentUserLogin).map(purchaseOrderMapper::toDto);
        }

        return purchaseOrderRepository.findOneByIdAndEmployeeUserLogin(id, currentUserLogin).map(purchaseOrderMapper::toDto);
    }

    @Override
    public void delete(Long id) {
        log.debug("Request to delete PurchaseOrder : {}", id);

        PurchaseOrder order = purchaseOrderRepository
            .findById(id)
            .orElseThrow(() -> new BadRequestAlertException("Không tìm thấy đơn mua hàng", ENTITY_NAME, "id_not_found"));

        validatePurchaserAccess(order.getWarehouse().getId());
        checkOrderOwnership(order);

        if (order.getStatus() != OrderStatus.DRAFT) {
            throw new BadRequestAlertException(
                "Đại kỵ! Chỉ được xóa vĩnh viễn đơn hàng ở trạng thái NHÁP (DRAFT)!",
                ENTITY_NAME,
                "cannot_delete_processed_order"
            );
        }

        List<PurchaseOrderLine> lines = purchaseOrderLineRepository.findByPurchaseOrderId(id);
        purchaseOrderLineRepository.deleteAll(lines);
        purchaseOrderRepository.deleteById(id);
    }

    @Transactional
    @Override
    public PurchaseOrderDTO approveOrder(Long id) {
        log.debug("Request to approve PurchaseOrder : {}", id);

        PurchaseOrder order = purchaseOrderRepository.findById(id).orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));

        validatePurchaserAccess(order.getWarehouse().getId());

        if (order.getStatus() == OrderStatus.APPROVED) {
            throw new BadRequestAlertException("Đơn hàng này đã được duyệt rồi!", ENTITY_NAME, "already_approved");
        }

        order.setStatus(OrderStatus.APPROVED);
        order = purchaseOrderRepository.save(order);

        String currentLogin = SecurityUtils.getCurrentUserLogin().orElse("System");
        String creatorLogin = order.getEmployee().getUser().getLogin();
        eventPublisher.publishEvent(
            new OrderNotificationEvent("PURCHASE", "APPROVED", order.getId(), order.getPoCode(), currentLogin, creatorLogin)
        );

        return purchaseOrderMapper.toDto(order);
    }

    /**
     * Hàm gom Batch Update chống N+1 để Nhập kho
     */
    private void processInboundInventory(PurchaseOrder order) {
        log.debug("Bắt đầu xử lý nhập kho cho Đơn mua hàng: {}", order.getPoCode());

        List<PurchaseOrderLine> lines = purchaseOrderLineRepository.findByPurchaseOrderId(order.getId());
        if (lines.isEmpty()) return;

        List<Long> productIds = lines.stream().map(line -> line.getProduct().getId()).collect(Collectors.toList());

        // Kéo tồn kho lên RAM
        Map<Long, InventoryBalance> balanceMap = inventoryBalanceRepository
            .findByWarehouseIdAndProductIdIn(order.getWarehouse().getId(), productIds)
            .stream()
            .collect(Collectors.toMap(b -> b.getProduct().getId(), b -> b));

        List<InventoryBalance> newBalances = new ArrayList<>();
        List<InventoryTransaction> transactionsToSave = new ArrayList<>();
        Instant now = Instant.now();

        for (PurchaseOrderLine line : lines) {
            Long productId = line.getProduct().getId();
            InventoryBalance balance = balanceMap.get(productId);

            if (balance != null) {
                // Hàng đã từng tồn tại -> Cộng thêm
                balance.setQuantity(balance.getQuantity() + line.getQuantity());
            } else {
                // Hàng mới tinh -> Tạo dòng tồn kho mới
                balance = new InventoryBalance();
                balance.setProduct(line.getProduct());
                balance.setWarehouse(order.getWarehouse());
                balance.setQuantity(line.getQuantity());
                newBalances.add(balance);
            }

            // Thẻ kho: NHẬP KHO (RECEIPT)
            InventoryTransaction transaction = new InventoryTransaction();
            transaction.setType(TransactionType.RECEIPT);
            transaction.setQuantity(line.getQuantity());
            transaction.setUnitCost(line.getUnitPrice());
            transaction.setReferenceId(order.getId());
            transaction.setCreatedDate(now);
            transaction.setProduct(line.getProduct());
            transaction.setWarehouse(order.getWarehouse());
            transactionsToSave.add(transaction);
        }

        try {
            inventoryBalanceRepository.saveAll(balanceMap.values()); // Lưu những thằng cập nhật
            inventoryBalanceRepository.saveAll(newBalances); // Lưu những thằng mới
            inventoryTransactionRepository.saveAll(transactionsToSave);

            inventoryBalanceRepository.flush();
            inventoryTransactionRepository.flush();
        } catch (org.springframework.orm.ObjectOptimisticLockingFailureException e) {
            log.error("Lỗi xung đột dữ liệu (Optimistic Locking) khi nhập kho đơn mua hàng: {}", e.getMessage());
            throw new BadRequestAlertException(
                "Dữ liệu tồn kho bị biến động bởi giao dịch khác. Vui lòng thử lại!",
                ENTITY_NAME,
                "optimistic_locking_inventory_conflict"
            );
        }
    }

    @Transactional
    @Override
    public PurchaseOrderDTO completeOrder(Long id) {
        log.debug("Request to complete PurchaseOrder : {}", id);

        PurchaseOrder order = purchaseOrderRepository.findById(id).orElseThrow(() -> new RuntimeException("Không tìm thấy đơn mua hàng"));

        if (order.getStatus() == OrderStatus.COMPLETED) {
            throw new BadRequestAlertException("Đơn hàng này đã được hoàn thành trước đó!", ENTITY_NAME, "already_completed");
        }
        if (order.getStatus() != OrderStatus.PROCESSING) {
            throw new BadRequestAlertException(
                "Chỉ có thể chốt đơn khi hàng đang được Shipper chở về kho (PROCESSING)!",
                ENTITY_NAME,
                "invalid_status"
            );
        }
        processInboundInventory(order);

        Supplier supplier = order.getSupplier();
        if (supplier == null) {
            throw new BadRequestAlertException(
                "Lỗi dữ liệu: Đơn mua hàng không có thông tin Nhà cung cấp!",
                ENTITY_NAME,
                "supplier_missing"
            );
        }

        BigDecimal currentDebt = supplier.getCurrentDebt() != null ? supplier.getCurrentDebt() : BigDecimal.ZERO;
        BigDecimal orderTotal = order.getTotalAmount() != null ? order.getTotalAmount() : BigDecimal.ZERO;

        // Cộng nợ nhà cung cấp
        supplier.setCurrentDebt(currentDebt.add(orderTotal));
        order.setStatus(OrderStatus.COMPLETED);

        try {
            supplierRepository.save(supplier);
            order = purchaseOrderRepository.save(order);
            supplierRepository.flush();
        } catch (org.springframework.orm.ObjectOptimisticLockingFailureException e) {
            log.error("Xung đột dữ liệu khi cập nhật công nợ nhà cung cấp: {}", e.getMessage());
            throw new BadRequestAlertException(
                "Công nợ của Nhà cung cấp này đang bị biến động. Vui lòng thử lại!",
                ENTITY_NAME,
                "optimistic_locking_supplier_debt"
            );
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

    @Transactional
    @Override
    public PurchaseOrderDTO cancelOrder(Long id) {
        log.debug("Request to cancel PurchaseOrder : {}", id);

        PurchaseOrder order = purchaseOrderRepository.findById(id).orElseThrow(() -> new RuntimeException("Không tìm thấy đơn mua hàng"));

        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new BadRequestAlertException("Đơn hàng này đã bị hủy từ trước!", ENTITY_NAME, "already_cancelled");
        }

        // CHỐT CHẶN CỨNG: CẤM HỦY ĐƠN ĐÃ COMPLETED
        if (order.getStatus() == OrderStatus.COMPLETED) {
            throw new BadRequestAlertException(
                "Không thể hủy đơn mua hàng đã HOÀN THÀNH (Đã chốt công nợ). Vui lòng sử dụng quy trình Trả Hàng (Return Order)!",
                ENTITY_NAME,
                "cannot_cancel_completed_order"
            );
        }

        // PHÂN QUYỀN ĐỘNG
        boolean isAdmin = SecurityUtils.hasCurrentUserThisAuthority(AuthoritiesConstants.ADMIN);
        boolean isManager = SecurityUtils.hasCurrentUserThisAuthority(AuthoritiesConstants.MANAGER);

        if (!isAdmin && !isManager) {
            throw new BadRequestAlertException(
                "Chỉ Quản lý chi nhánh hoặc Admin mới được hủy đơn ở giai đoạn này!",
                ENTITY_NAME,
                "manager_required_for_cancel"
            );
        }

        if (isManager) {
            validatePurchaserAccess(order.getWarehouse().getId());
        }

        // ĐẢO NGƯỢC TỒN KHO (Chỉ áp dụng khi đơn đã APPROVED vì đã lỡ nhập kho)
        if (order.getStatus() == OrderStatus.APPROVED) {
            List<PurchaseOrderLine> lines = purchaseOrderLineRepository.findByPurchaseOrderId(order.getId());

            if (!lines.isEmpty()) {
                List<Long> productIds = lines.stream().map(line -> line.getProduct().getId()).collect(Collectors.toList());
                Map<Long, InventoryBalance> balanceMap = inventoryBalanceRepository
                    .findByWarehouseIdAndProductIdIn(order.getWarehouse().getId(), productIds)
                    .stream()
                    .collect(Collectors.toMap(b -> b.getProduct().getId(), b -> b));

                List<InventoryTransaction> issueTransactions = new ArrayList<>();
                Instant now = Instant.now();

                for (PurchaseOrderLine line : lines) {
                    InventoryBalance balance = balanceMap.get(line.getProduct().getId());
                    if (balance == null || balance.getQuantity() < line.getQuantity()) {
                        throw new BadRequestAlertException(
                            "Tồn kho của " + line.getProduct().getName() + " hiện tại không đủ để xuất trả nhà cung cấp!",
                            ENTITY_NAME,
                            "insufficient_stock_to_return"
                        );
                    }

                    // Trừ kho (Vì đang Hủy Nhập = Xuất trả)
                    balance.setQuantity(balance.getQuantity() - line.getQuantity());

                    InventoryTransaction issueTx = new InventoryTransaction();
                    issueTx.setType(TransactionType.ISSUE);
                    issueTx.setQuantity(line.getQuantity());
                    issueTx.setUnitCost(line.getUnitPrice());
                    issueTx.setReferenceId(order.getId());
                    issueTx.setCreatedDate(now);
                    issueTx.setProduct(line.getProduct());
                    issueTx.setWarehouse(order.getWarehouse());
                    issueTransactions.add(issueTx);
                }

                try {
                    inventoryBalanceRepository.saveAll(balanceMap.values());
                    inventoryTransactionRepository.saveAll(issueTransactions);
                    inventoryBalanceRepository.flush();
                    inventoryTransactionRepository.flush();
                } catch (org.springframework.orm.ObjectOptimisticLockingFailureException e) {
                    throw new BadRequestAlertException(
                        "Xung đột dữ liệu tồn kho khi xuất trả hàng!",
                        ENTITY_NAME,
                        "optimistic_locking_inventory_conflict"
                    );
                }
            }
        }

        // CHUYỂN TRẠNG THÁI VÀ LƯU PHIẾU
        order.setStatus(OrderStatus.CANCELLED);
        order = purchaseOrderRepository.save(order);

        // BẮN SỰ KIỆN THÔNG BÁO
        String currentLogin = SecurityUtils.getCurrentUserLogin().orElse("System");
        String creatorLogin = (order.getEmployee() != null && order.getEmployee().getUser() != null)
            ? order.getEmployee().getUser().getLogin()
            : currentLogin;
        eventPublisher.publishEvent(
            new OrderNotificationEvent("PURCHASE", "CANCELLED", order.getId(), order.getPoCode(), currentLogin, creatorLogin)
        );

        return purchaseOrderMapper.toDto(order);
    }

    @Transactional
    @Override
    public PurchaseOrderDTO startDelivery(Long id) {
        log.debug("Shipper bắt đầu lấy hàng từ Supplier cho đơn: {}", id);
        PurchaseOrder order = purchaseOrderRepository.findById(id).orElseThrow();

        if (order.getStatus() != OrderStatus.APPROVED) {
            throw new BadRequestAlertException(
                "Chỉ có thể đi lấy hàng khi đơn đã được duyệt mua (APPROVED)!",
                ENTITY_NAME,
                "invalid_status"
            );
        }

        order.setStatus(OrderStatus.PROCESSING); // Hàng đang trên xe
        order = purchaseOrderRepository.save(order);

        eventPublisher.publishEvent(
            new OrderNotificationEvent(
                "PURCHASE",
                "PROCESSING",
                order.getId(),
                "Shipper đang chở hàng về kho",
                "System",
                order.getEmployee().getUser().getLogin()
            )
        );
        return purchaseOrderMapper.toDto(order);
    }

    @Transactional
    @Override
    public PurchaseOrderDTO confirmDelivery(Long id) {
        log.debug("Shipper báo cáo hàng đã về tới kho: {}", id);
        PurchaseOrder order = purchaseOrderRepository.findById(id).orElseThrow();

        if (order.getStatus() != OrderStatus.PROCESSING) {
            throw new BadRequestAlertException("Đơn hàng chưa ở trạng thái Đang giao!", ENTITY_NAME, "invalid_status");
        }

        // Bắn Noti gọi Thủ kho ra nhận hàng, Kế toán chuẩn bị chốt tiền
        eventPublisher.publishEvent(
            new OrderNotificationEvent(
                "PURCHASE",
                "ARRIVED",
                order.getId(),
                "Xe hàng " + order.getPoCode() + " đã về tới. Thủ kho ra nhận hàng!",
                "System",
                "WAREHOUSE_GROUP"
            )
        );
        return purchaseOrderMapper.toDto(order);
    }
}
