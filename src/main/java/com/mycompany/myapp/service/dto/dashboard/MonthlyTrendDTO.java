package com.mycompany.myapp.service.dto.dashboard;

import java.math.BigDecimal;

public class MonthlyTrendDTO {

    private String timeLabel; // Ví dụ: "01/2026", "02/2026"
    private BigDecimal revenue;
    private BigDecimal profit;

    public MonthlyTrendDTO() {}

    public MonthlyTrendDTO(String timeLabel, BigDecimal revenue, BigDecimal profit) {
        this.timeLabel = timeLabel;
        this.revenue = revenue;
        this.profit = profit;
    }

    public String getTimeLabel() {
        return timeLabel;
    }

    public void setTimeLabel(String timeLabel) {
        this.timeLabel = timeLabel;
    }

    public BigDecimal getRevenue() {
        return revenue;
    }

    public void setRevenue(BigDecimal revenue) {
        this.revenue = revenue;
    }

    public BigDecimal getProfit() {
        return profit;
    }

    public void setProfit(BigDecimal profit) {
        this.profit = profit;
    }
}
