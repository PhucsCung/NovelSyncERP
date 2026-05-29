package com.mycompany.myapp.service.dto;

import java.io.Serializable;
import java.util.Objects;
import javax.validation.constraints.*;

/**
 * A DTO for the {@link com.mycompany.myapp.domain.TransferOrderLine} entity.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class TransferOrderLineDTO implements Serializable {

    private Long id;

    @NotNull
    private Integer quantity;

    private ProductDTO product;

    private TransferOrderDTO transferOrder;

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

    public TransferOrderDTO getTransferOrder() {
        return transferOrder;
    }

    public void setTransferOrder(TransferOrderDTO transferOrder) {
        this.transferOrder = transferOrder;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof TransferOrderLineDTO)) {
            return false;
        }

        TransferOrderLineDTO transferOrderLineDTO = (TransferOrderLineDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, transferOrderLineDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "TransferOrderLineDTO{" +
            "id=" + getId() +
            ", quantity=" + getQuantity() +
            ", product=" + getProduct() +
            ", transferOrder=" + getTransferOrder() +
            "}";
    }
}
