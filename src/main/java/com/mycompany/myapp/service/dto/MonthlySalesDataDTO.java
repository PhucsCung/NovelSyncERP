package com.mycompany.myapp.service.dto;

import java.io.Serializable;
import java.math.BigDecimal;

public class MonthlySalesDataDTO implements Serializable {

    private Integer month;
    private BigDecimal price;
    private BigDecimal discount_percent;
    private BigDecimal marketing_spend;
    private Integer is_holiday;
    private Integer sales_volume;

    public Integer getMonth() {
        return month;
    }

    public void setMonth(Integer month) {
        this.month = month;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public BigDecimal getDiscount_percent() {
        return discount_percent;
    }

    public void setDiscount_percent(BigDecimal discount_percent) {
        this.discount_percent = discount_percent;
    }

    public BigDecimal getMarketing_spend() {
        return marketing_spend;
    }

    public void setMarketing_spend(BigDecimal marketing_spend) {
        this.marketing_spend = marketing_spend;
    }

    public Integer getIs_holiday() {
        return is_holiday;
    }

    public void setIs_holiday(Integer is_holiday) {
        this.is_holiday = is_holiday;
    }

    public Integer getSales_volume() {
        return sales_volume;
    }

    public void setSales_volume(Integer sales_volume) {
        this.sales_volume = sales_volume;
    }
}
