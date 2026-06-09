package com.mycompany.myapp.service.event;

public class OrderNotificationEvent {

    private final String orderType;
    private final String action;
    private final Long orderId;
    private final String orderCode;
    private final String actionByUserLogin;
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
