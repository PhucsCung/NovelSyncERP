package com.mycompany.myapp.service;

import com.mycompany.myapp.service.dto.PaymentDTO;
import java.math.BigDecimal;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service Interface for managing {@link com.mycompany.myapp.domain.Payment}.
 */
public interface PaymentService {
    Page<PaymentDTO> findAll(Pageable pageable);

    Page<PaymentDTO> findAllWithEagerRelationships(Pageable pageable);

    Optional<PaymentDTO> findOne(Long id);

    /**
     * Hàm dùng cho Ngân hàng (Webhook) gọi vào để tự động tạo Phiếu Thu PENDING.
     */
    PaymentDTO handleBankWebhook(String bankTransactionId, BigDecimal amount, String content, Long customerId, Long orderId);

    /**
     * Hàm dành cho Kế toán (Checker) bấm xác nhận ghi sổ chứng từ (Chuyển sang COMPLETED).
     */
    PaymentDTO approveAndReconcile(Long id);
    /**
     * Kế toán tự tạo Phiếu Chi trả nợ cho Nhà cung cấp (Tạo xong là COMPLETED luôn)
     */
    PaymentDTO createSupplierDisbursement(Long supplierId, BigDecimal amount, String note);
}
