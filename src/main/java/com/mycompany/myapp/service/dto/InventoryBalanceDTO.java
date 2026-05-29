package com.mycompany.myapp.service.dto;

import java.io.Serializable;
import java.util.Objects;
import javax.validation.constraints.*;

/**
 * A DTO for the {@link com.mycompany.myapp.domain.InventoryBalance} entity.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class InventoryBalanceDTO implements Serializable {

    private Long id;

    @NotNull
    private Integer quantity;

    private ProductDTO product;

    private WarehouseDTO warehouse;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public ProductDTO getProduct() {
        return product;
    }

    public void setProduct(ProductDTO product) {
        this.product = product;
    }

    public WarehouseDTO getWarehouse() {
        return warehouse;
    }

    public void setWarehouse(WarehouseDTO warehouse) {
        this.warehouse = warehouse;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof InventoryBalanceDTO)) {
            return false;
        }

        InventoryBalanceDTO inventoryBalanceDTO = (InventoryBalanceDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, inventoryBalanceDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "InventoryBalanceDTO{" +
            "id=" + getId() +
            ", quantity=" + getQuantity() +
            ", product=" + getProduct() +
            ", warehouse=" + getWarehouse() +
            "}";
    }
}
