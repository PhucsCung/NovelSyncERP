package com.mycompany.myapp.service.dto;

import java.io.Serializable;

public class AiPredictResponseDTO implements Serializable {

    private Long product_id;
    private Long warehouse_id;
    private Integer predicted_sales;
    private Integer current_stock;
    private Integer recommend_restock;

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

    public Integer getPredicted_sales() {
        return predicted_sales;
    }

    public void setPredicted_sales(Integer predicted_sales) {
        this.predicted_sales = predicted_sales;
    }

    public Integer getCurrent_stock() {
        return current_stock;
    }

    public void setCurrent_stock(Integer current_stock) {
        this.current_stock = current_stock;
    }

    public Integer getRecommend_restock() {
        return recommend_restock;
    }

    public void setRecommend_restock(Integer recommend_restock) {
        this.recommend_restock = recommend_restock;
    }
}
