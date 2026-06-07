package com.mycompany.myapp.service.dto.dashboard;

import java.math.BigDecimal;

public class DashboardSummaryDTO {

    private BigDecimal totalRevenue;
    private BigDecimal totalProfit;

    public DashboardSummaryDTO() {}

    public DashboardSummaryDTO(BigDecimal totalRevenue, BigDecimal totalProfit) {
        this.totalRevenue = totalRevenue;
        this.totalProfit = totalProfit;
    }

    public BigDecimal getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(BigDecimal totalRevenue) {
        this.totalRevenue = totalRevenue;
    }

    public BigDecimal getTotalProfit() {
        return totalProfit;
    }

    public void setTotalProfit(BigDecimal totalProfit) {
        this.totalProfit = totalProfit;
    }
}
