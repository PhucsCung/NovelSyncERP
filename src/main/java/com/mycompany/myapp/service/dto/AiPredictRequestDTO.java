package com.mycompany.myapp.service.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

public class AiPredictRequestDTO implements Serializable {

    private Long product_id;
    private Long warehouse_id;
    private Integer current_stock;
    private List<MonthlySalesDataDTO> history;

    private BigDecimal next_month_price;
    private BigDecimal next_month_discount;
    private BigDecimal next_month_marketing;
    private Integer next_month_is_holiday;

    public Long getProduct_id() {
        return product_id;
    }

    public void setProduct_id(Long product_id) {
        this.product_id = product_id;
    }

    public Long getWarehouse_id() {
        return warehouse_id;
    }

    public void setWarehouse_id(Long warehouse_id) {
        this.warehouse_id = warehouse_id;
    }

    public Integer getCurrent_stock() {
        return current_stock;
    }

    public void setCurrent_stock(Integer current_stock) {
        this.current_stock = current_stock;
    }

    public List<MonthlySalesDataDTO> getHistory() {
        return history;
    }

    public void setHistory(List<MonthlySalesDataDTO> history) {
        this.history = history;
    }

    public BigDecimal getNext_month_price() {
        return next_month_price;
    }

    public void setNext_month_price(BigDecimal next_month_price) {
        this.next_month_price = next_month_price;
    }

    public BigDecimal getNext_month_discount() {
        return next_month_discount;
    }

    public void setNext_month_discount(BigDecimal next_month_discount) {
        this.next_month_discount = next_month_discount;
    }

    public BigDecimal getNext_month_marketing() {
        return next_month_marketing;
    }

    public void setNext_month_marketing(BigDecimal next_month_marketing) {
        this.next_month_marketing = next_month_marketing;
    }

    public Integer getNext_month_is_holiday() {
        return next_month_is_holiday;
    }

    public void setNext_month_is_holiday(Integer next_month_is_holiday) {
        this.next_month_is_holiday = next_month_is_holiday;
    }
}
