package com.mycompany.myapp.service.dto.dashboard;

import java.util.List;

public class DashboardResponseDTO {

    private DashboardSummaryDTO summary;
    private List<MonthlyTrendDTO> trendCharts;
    private List<TopProductDTO> topProducts;

    public DashboardResponseDTO() {}

    public DashboardSummaryDTO getSummary() {
        return summary;
    }

    public void setSummary(DashboardSummaryDTO summary) {
        this.summary = summary;
    }

    public List<MonthlyTrendDTO> getTrendCharts() {
        return trendCharts;
    }

    public void setTrendCharts(List<MonthlyTrendDTO> trendCharts) {
        this.trendCharts = trendCharts;
    }

    public List<TopProductDTO> getTopProducts() {
        return topProducts;
    }

    public void setTopProducts(List<TopProductDTO> topProducts) {
        this.topProducts = topProducts;
    }
}
