package com.mycompany.myapp.service.impl;

import com.mycompany.myapp.domain.*;
import com.mycompany.myapp.domain.enumeration.PaymentType;
import com.mycompany.myapp.repository.*;
import com.mycompany.myapp.service.PaymentService;
import com.mycompany.myapp.service.dto.PaymentDTO;
import com.mycompany.myapp.service.mapper.PaymentMapper;
import java.math.BigDecimal;
import java.util.List;
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

    private final PaymentRepository paymentRepository;

    private final PaymentMapper paymentMapper;
    private final CustomerRepository customerRepository;
    private final SupplierRepository supplierRepository;
    private final SalesOrderRepository salesOrderRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;

    public PaymentServiceImpl(
        PaymentRepository paymentRepository,
        PaymentMapper paymentMapper,
        CustomerRepository customerRepository,
        SupplierRepository supplierRepository,
        SalesOrderRepository salesOrderRepository,
        PurchaseOrderRepository purchaseOrderRepository
    ) {
        this.paymentRepository = paymentRepository;
        this.paymentMapper = paymentMapper;
        this.customerRepository = customerRepository;
        this.supplierRepository = supplierRepository;
        this.salesOrderRepository = salesOrderRepository;
        this.purchaseOrderRepository = purchaseOrderRepository;
    }

    @Override
    public PaymentDTO save(PaymentDTO paymentDTO) {
        log.debug("Request to save Payment : {}", paymentDTO);
        Payment payment = paymentMapper.toEntity(paymentDTO);
        payment = paymentRepository.save(payment);
        // Chỉ cần gọi hàm Áp dụng
        applyPaymentDebt(payment);
        return paymentMapper.toDto(payment);
    }

    @Override
    public PaymentDTO update(PaymentDTO paymentDTO) {
        log.debug("Request to update Payment : {}", paymentDTO);
        // 1. Lấy phiếu cũ dưới DB lên và Hoàn lại tiền cũ
        Payment oldPayment = paymentRepository
            .findById(paymentDTO.getId())
            .orElseThrow(() -> new RuntimeException("Không tìm thấy Phiếu thanh toán"));
        revertPaymentDebt(oldPayment);

        // 2. Lưu phiếu mới
        Payment payment = paymentMapper.toEntity(paymentDTO);
        payment = paymentRepository.save(payment);

        // 3. Áp dụng lại tiền mới
        applyPaymentDebt(payment);

        return paymentMapper.toDto(payment);
    }

    @Override
    public Optional<PaymentDTO> partialUpdate(PaymentDTO paymentDTO) {
        log.debug("Request to partially update Payment : {}", paymentDTO);

        return paymentRepository
            .findById(paymentDTO.getId())
            .map(existingPayment -> {
                // 1. Hoàn lại tiền cũ trước khi map dữ liệu mới vào
                revertPaymentDebt(existingPayment);

                // 2. Map dữ liệu mới đè lên
                paymentMapper.partialUpdate(existingPayment, paymentDTO);
                return existingPayment;
            })
            .map(paymentRepository::save)
            .map(savedPayment -> {
                // 3. Áp dụng tiền mới sau khi đã lưu xong
                applyPaymentDebt(savedPayment);
                return paymentMapper.toDto(savedPayment);
            });
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PaymentDTO> findAll(Pageable pageable) {
        log.debug("Request to get all Payments");
        return paymentRepository.findAll(pageable).map(paymentMapper::toDto);
    }

    public Page<PaymentDTO> findAllWithEagerRelationships(Pageable pageable) {
        return paymentRepository.findAllWithEagerRelationships(pageable).map(paymentMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<PaymentDTO> findOne(Long id) {
        log.debug("Request to get Payment : {}", id);
        return paymentRepository.findOneWithEagerRelationships(id).map(paymentMapper::toDto);
    }

    @Override
    public void delete(Long id) {
        log.debug("Request to delete Payment : {}", id);
        // 1. Tìm phiếu trước khi xóa, nếu có thì Hoàn lại tiền cho Khách
        paymentRepository.findById(id).ifPresent(this::revertPaymentDebt);

        // 2. Xóa phiếu khỏi DB
        paymentRepository.deleteById(id);
    }

    /**
     * Hàm tính tổng tiền đã thu và so sánh với giá trị Đơn Bán Hàng
     */
    private void checkSalesOrderPaymentStatus(Long salesOrderId) {
        SalesOrder order = salesOrderRepository.findById(salesOrderId).orElse(null);
        if (order == null) return;

        // Lấy tất cả Phiếu THU gắn với đơn hàng này
        List<Payment> payments = paymentRepository.findByReferenceOrderIdAndType(salesOrderId, PaymentType.RECEIPT);

        // Cộng dồn tổng tiền đã trả
        BigDecimal totalPaid = BigDecimal.ZERO;
        for (Payment p : payments) {
            totalPaid = totalPaid.add(p.getAmount());
        }

        BigDecimal orderTotal = order.getTotalAmount() != null ? order.getTotalAmount() : BigDecimal.ZERO;

        // Kiểm tra nếu Đã trả >= Tổng đơn hàng
        if (totalPaid.compareTo(orderTotal) >= 0) {
            log.info("🎉 Đơn bán hàng {} ĐÃ ĐƯỢC THANH TOÁN ĐỦ! Tổng thu: {}", salesOrderId, totalPaid);
            // TODO: Bác có thể cập nhật trạng thái đơn hàng thành PAID tại đây nếu Database có hỗ trợ.
        } else {
            log.info("⏳ Đơn bán hàng {} mới thanh toán một phần. Còn thiếu: {}", salesOrderId, orderTotal.subtract(totalPaid));
        }
    }

    /**
     * Hàm tính tổng tiền đã chi và so sánh với giá trị Đơn Mua Hàng
     */
    private void checkPurchaseOrderPaymentStatus(Long purchaseOrderId) {
        PurchaseOrder order = purchaseOrderRepository.findById(purchaseOrderId).orElse(null);
        if (order == null) return;

        // Lấy tất cả Phiếu CHI gắn với đơn hàng này
        List<Payment> payments = paymentRepository.findByReferenceOrderIdAndType(purchaseOrderId, PaymentType.DISBURSEMENT);

        BigDecimal totalPaid = BigDecimal.ZERO;
        for (Payment p : payments) {
            totalPaid = totalPaid.add(p.getAmount());
        }

        BigDecimal orderTotal = order.getTotalAmount() != null ? order.getTotalAmount() : BigDecimal.ZERO;

        if (totalPaid.compareTo(orderTotal) >= 0) {
            log.info("🎉 Đơn mua hàng {} ĐÃ ĐƯỢC THANH TOÁN ĐỦ! Tổng chi: {}", purchaseOrderId, totalPaid);
            // TODO: Bác có thể cập nhật trạng thái đơn hàng thành PAID tại đây nếu Database có hỗ trợ.
        }
    }

    /**
     * Hàm ÁP DỤNG: Trừ nợ khi tạo mới Phiếu Thu/Chi
     */
    private void applyPaymentDebt(Payment payment) {
        if (payment.getType() == PaymentType.RECEIPT && payment.getCustomer() != null) {
            Customer customer = customerRepository.findById(payment.getCustomer().getId()).orElseThrow();
            BigDecimal currentDebt = customer.getCurrentDebt() != null ? customer.getCurrentDebt() : BigDecimal.ZERO;

            // ÁP DỤNG: Trừ nợ
            customer.setCurrentDebt(currentDebt.subtract(payment.getAmount()));
            customerRepository.save(customer);

            if (payment.getReferenceOrderId() != null) checkSalesOrderPaymentStatus(payment.getReferenceOrderId());
        } else if (payment.getType() == PaymentType.DISBURSEMENT && payment.getSupplier() != null) {
            Supplier supplier = supplierRepository.findById(payment.getSupplier().getId()).orElseThrow();
            BigDecimal currentDebt = supplier.getCurrentDebt() != null ? supplier.getCurrentDebt() : BigDecimal.ZERO;

            // ÁP DỤNG: Trừ nợ
            supplier.setCurrentDebt(currentDebt.subtract(payment.getAmount()));
            supplierRepository.save(supplier);

            if (payment.getReferenceOrderId() != null) checkPurchaseOrderPaymentStatus(payment.getReferenceOrderId());
        }
    }

    /**
     * Hàm HOÀN LẠI (ROLLBACK): Cộng nợ trả lại khi Xóa hoặc Sửa Phiếu Thu/Chi
     */
    private void revertPaymentDebt(Payment payment) {
        if (payment.getType() == PaymentType.RECEIPT && payment.getCustomer() != null) {
            Customer customer = customerRepository.findById(payment.getCustomer().getId()).orElseThrow();
            BigDecimal currentDebt = customer.getCurrentDebt() != null ? customer.getCurrentDebt() : BigDecimal.ZERO;

            // HOÀN LẠI: Cộng trả lại tiền đã trừ lỡ tay
            customer.setCurrentDebt(currentDebt.add(payment.getAmount()));
            customerRepository.save(customer);

            if (payment.getReferenceOrderId() != null) checkSalesOrderPaymentStatus(payment.getReferenceOrderId());
        } else if (payment.getType() == PaymentType.DISBURSEMENT && payment.getSupplier() != null) {
            Supplier supplier = supplierRepository.findById(payment.getSupplier().getId()).orElseThrow();
            BigDecimal currentDebt = supplier.getCurrentDebt() != null ? supplier.getCurrentDebt() : BigDecimal.ZERO;

            // HOÀN LẠI: Cộng trả lại tiền
            supplier.setCurrentDebt(currentDebt.add(payment.getAmount()));
            supplierRepository.save(supplier);

            if (payment.getReferenceOrderId() != null) checkPurchaseOrderPaymentStatus(payment.getReferenceOrderId());
        }
    }
}
