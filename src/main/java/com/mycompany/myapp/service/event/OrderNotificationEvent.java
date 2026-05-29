package com.mycompany.myapp.service.event;

public class OrderNotificationEvent {

    // Phân loại: "SALES", "PURCHASE", "TRANSFER"
    private final String orderType;

    // Hành động: "CREATED" (Tạo nháp), "APPROVED" (Duyệt), "COMPLETED" (Hoàn thành), "CANCELLED" (Hủy)
    private final String action;

    private final Long orderId;
    private final String orderCode;

    // Người đang thao tác (Ví dụ: Nhân viên bấm tạo đơn, hoặc Sếp bấm duyệt đơn)
    private final String actionByUserLogin;

    // Chủ nhân gốc của đơn hàng (Để khi Sếp duyệt thì biết đường gửi thông báo báo lại cho nhân viên này)
    private final String originalCreatorLogin;

    public OrderNotificationEvent(
        String orderType,
        String action,
        Long orderId,
        String orderCode,
        String actionByUserLogin,
        String originalCreatorLogin
    ) {
        this.orderType = orderType;
        this.action = action;
        this.orderId = orderId;
        this.orderCode = orderCode;
        this.actionByUserLogin = actionByUserLogin;
        this.originalCreatorLogin = originalCreatorLogin;
    }

    // Các Getters (Bắt buộc phải có để Listener lấy dữ liệu)
    public String getOrderType() {
        return orderType;
    }

    public String getAction() {
        return action;
    }

    public Long getOrderId() {
        return orderId;
    }

    public String getOrderCode() {
        return orderCode;
    }

    public String getActionByUserLogin() {
        return actionByUserLogin;
    }

    public String getOriginalCreatorLogin() {
        return originalCreatorLogin;
    }
}
