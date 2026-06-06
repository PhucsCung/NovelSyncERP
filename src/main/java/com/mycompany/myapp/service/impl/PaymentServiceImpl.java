package com.mycompany.myapp.service.impl;

import com.mycompany.myapp.domain.*;
import com.mycompany.myapp.domain.enumeration.PaymentStatus;
import com.mycompany.myapp.domain.enumeration.PaymentType;
import com.mycompany.myapp.repository.*;
import com.mycompany.myapp.service.PaymentService;
import com.mycompany.myapp.service.SalesOrderService;
import com.mycompany.myapp.service.dto.PaymentDTO;
import com.mycompany.myapp.service.mapper.PaymentMapper;
import com.mycompany.myapp.web.rest.errors.BadRequestAlertException;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link Payment}.
 */
@Service
@Transactional
public class PaymentServiceImpl implements PaymentService {

    private final Logger log = LoggerFactory.getLogger(PaymentServiceImpl.class);
    private static final String ENTITY_NAME = "payment";

    private final PaymentRepository paymentRepository;
    private final PaymentMapper paymentMapper;
    private final CustomerRepository customerRepository;
    private final SupplierRepository supplierRepository;
    private final SalesOrderService salesOrderService;

    public PaymentServiceImpl(
        PaymentRepository paymentRepository,
        PaymentMapper paymentMapper,
        CustomerRepository customerRepository,
        SupplierRepository supplierRepository,
        SalesOrderService salesOrderService
    ) {
        this.paymentRepository = paymentRepository;
        this.paymentMapper = paymentMapper;
        this.customerRepository = customerRepository;
        this.supplierRepository = supplierRepository;
        this.salesOrderService = salesOrderService;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PaymentDTO> findAll(Pageable pageable) {
        log.debug("Request to get all Payments");
        return paymentRepository.findAll(pageable).map(paymentMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PaymentDTO> findAllWithEagerRelationships(Pageable pageable) {
        return paymentRepository.findAllWithEagerRelationships(pageable).map(paymentMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<PaymentDTO> findOne(Long id) {
        log.debug("Request to get Payment : {}", id);
        return paymentRepository.findOneWithEagerRelationships(id).map(paymentMapper::toDto);
    }

    // =================================================================================
    // 2. LUỒNG NGHIỆP VỤ HYBRID ERP (VNPAY IPN + KẾ TOÁN DUYỆT)
    // =================================================================================

    @Override
    public PaymentDTO handleBankWebhook(String bankTransactionId, BigDecimal amount, String content, Long customerId, Long orderId) {
        log.debug("Nhận Webhook từ VNPay/Bank. Mã GD: {}, Số tiền: {}", bankTransactionId, amount);

        Payment payment = new Payment();
        payment.setPaymentCode("VNPAY-" + bankTransactionId);
        payment.setType(PaymentType.RECEIPT);
        payment.setAmount(amount);
        payment.setStatus(PaymentStatus.PENDING); // CHƯA TRỪ NỢ
        payment.setNote("VNPay Webhook: " + content);

        if (orderId != null) {
            payment.setReferenceOrderId(orderId);
        }

        if (customerId != null) {
            Customer customer = customerRepository.findById(customerId).orElse(null);
            payment.setCustomer(customer);
        }

        payment = paymentRepository.save(payment);
        log.info("🚀 VNPay Webhook đã tự động tạo phiếu thu PENDING: {}", payment.getPaymentCode());
        return paymentMapper.toDto(payment);
    }

    @Override
    public PaymentDTO approveAndReconcile(Long id) {
        log.debug("Kế toán duyệt và ghi sổ phiếu thanh toán ID: {}", id);

        Payment payment = paymentRepository
            .findById(id)
            .orElseThrow(() -> new BadRequestAlertException("Không tìm thấy phiếu thanh toán!", ENTITY_NAME, "id_not_found"));

        if (payment.getStatus() == PaymentStatus.COMPLETED) {
            throw new BadRequestAlertException("Phiếu này đã được ghi sổ từ trước!", ENTITY_NAME, "already_approved");
        }

        payment.setStatus(PaymentStatus.COMPLETED);
        applyPaymentDebt(payment);
        //hoàn thành đơn
        if (payment.getType() == PaymentType.RECEIPT && payment.getReferenceOrderId() != null) {
            log.info("Tự động hoàn thành SalesOrder ID: {} vì đã thu đủ tiền", payment.getReferenceOrderId());

            // Gọi sang hàm completeOrder của SalesOrderService
            // (Hàm này của bạn sẽ tự động lo việc đổi status sang COMPLETED và + nợ)
            salesOrderService.completeOrder(payment.getReferenceOrderId());
        }

        payment = paymentRepository.save(payment);
        log.info("🎉 Kế toán đã duyệt thành công! Công nợ đã được cấn trừ cho phiếu: {}", payment.getPaymentCode());
        return paymentMapper.toDto(payment);
    }

    private void applyPaymentDebt(Payment payment) {
        if (payment.getType() == PaymentType.RECEIPT && payment.getCustomer() != null) {
            Customer customer = payment.getCustomer();
            BigDecimal currentDebt = customer.getCurrentDebt() != null ? customer.getCurrentDebt() : BigDecimal.ZERO;

            customer.setCurrentDebt(currentDebt.subtract(payment.getAmount()));
            customerRepository.save(customer);
        } else if (payment.getType() == PaymentType.DISBURSEMENT && payment.getSupplier() != null) {
            Supplier supplier = payment.getSupplier();
            BigDecimal currentDebt = supplier.getCurrentDebt() != null ? supplier.getCurrentDebt() : BigDecimal.ZERO;

            supplier.setCurrentDebt(currentDebt.subtract(payment.getAmount()));
            supplierRepository.save(supplier);
        }
    }

    /**
     * Hàm dành riêng cho Kế toán tạo Phiếu Chi (Trả nợ Nhà cung cấp)
     * Vì là Kế toán tự tạo sau khi đã chuyển khoản thật, phiếu sẽ auto COMPLETED
     */
    @Override
    public PaymentDTO createSupplierDisbursement(Long supplierId, BigDecimal amount, String note) {
        log.debug("Kế toán tạo Phiếu chi trả nợ Nhà cung cấp ID: {}, Số tiền: {}", supplierId, amount);

        Supplier supplier = supplierRepository
            .findById(supplierId)
            .orElseThrow(() -> new BadRequestAlertException("Không tìm thấy Nhà cung cấp!", "payment", "supplier_not_found"));

        Payment payment = new Payment();
        payment.setPaymentCode("PC-" + Instant.now().toEpochMilli()); // PC = Phiếu Chi
        payment.setType(PaymentType.DISBURSEMENT); // Loại: CHI
        payment.setAmount(amount);
        payment.setStatus(PaymentStatus.COMPLETED); // Tạo phát là Hoàn thành luôn
        payment.setNote(note);
        payment.setSupplier(supplier);

        // Trừ nợ Supplier
        BigDecimal currentDebt = supplier.getCurrentDebt() != null ? supplier.getCurrentDebt() : BigDecimal.ZERO;
        supplier.setCurrentDebt(currentDebt.subtract(amount));
        supplierRepository.save(supplier);

        payment = paymentRepository.save(payment);
        return paymentMapper.toDto(payment);
    }
}
