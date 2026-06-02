package com.mycompany.myapp.domain.enumeration;

/**
 * The PaymentStatus enumeration.
 */
public enum PaymentStatus {
    PENDING, // Chờ đối soát (Ngân hàng bắn Webhook về, chờ kế toán xác nhận)
    COMPLETED, // Đã hoàn thành/Ghi sổ (Kế toán đã duyệt, chính thức trừ nợ)
    //    CANCELLED,
}
