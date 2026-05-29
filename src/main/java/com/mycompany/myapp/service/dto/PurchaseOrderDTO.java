package com.mycompany.myapp.service.dto;

import com.mycompany.myapp.domain.enumeration.OrderStatus;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import javax.validation.constraints.*;

/**
 * A DTO for the {@link com.mycompany.myapp.domain.PurchaseOrder} entity.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class PurchaseOrderDTO implements Serializable {

    private Long id;

    @NotNull
    private String poCode;

    private BigDecimal totalAmount;

    @NotNull
    private OrderStatus status;

    private SupplierDTO supplier;

    private EmployeeDTO employee;

    private WarehouseDTO warehouse;

    // 1. Thêm cái giỏ đựng danh sách mặt hàng
    private List<PurchaseOrderLineDTO> purchaseOrderLines;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPoCode() {
        return poCode;
    }

    public void setPoCode(String poCode) {
        this.poCode = poCode;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public SupplierDTO getSupplier() {
        return supplier;
    }

    public void setSupplier(SupplierDTO supplier) {
        this.supplier = supplier;
    }

    public EmployeeDTO getEmployee() {
        return employee;
    }

    public void setEmployee(EmployeeDTO employee) {
        this.employee = employee;
    }

    public WarehouseDTO getWarehouse() {
        return warehouse;
    }

    public void setWarehouse(WarehouseDTO warehouse) {
        this.warehouse = warehouse;
    }

    // 2. Thêm Getter và Setter cho nó
    public List<PurchaseOrderLineDTO> getPurchaseOrderLines() {
        return purchaseOrderLines;
    }

    public void setPurchaseOrderLines(List<PurchaseOrderLineDTO> purchaseOrderLines) {
        this.purchaseOrderLines = purchaseOrderLines;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof PurchaseOrderDTO)) {
            return false;
        }

        PurchaseOrderDTO purchaseOrderDTO = (PurchaseOrderDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, purchaseOrderDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "PurchaseOrderDTO{" +
            "id=" + getId() +
            ", poCode='" + getPoCode() + "'" +
            ", totalAmount=" + getTotalAmount() +
            ", status='" + getStatus() + "'" +
            ", supplier=" + getSupplier() +
            ", employee=" + getEmployee() +
            ", warehouse=" + getWarehouse() +
            "}";
    }
}
