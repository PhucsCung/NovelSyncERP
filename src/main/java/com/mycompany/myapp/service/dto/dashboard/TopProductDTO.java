package com.mycompany.myapp.service.dto.dashboard;

import java.math.BigDecimal;

public class TopProductDTO {

    private Long productId;
    private String productName;
    private Integer quantitySold;
    private BigDecimal profitBrought;

    public TopProductDTO() {}

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public Integer getQuantitySold() {
        return quantitySold;
    }

    public void setQuantitySold(Integer quantitySold) {
        this.quantitySold = quantitySold;
    }

    public BigDecimal getProfitBrought() {
        return profitBrought;
    }

    public void setProfitBrought(BigDecimal profitBrought) {
        this.profitBrought = profitBrought;
    }
}
