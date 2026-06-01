package com.mycompany.myapp.service.dto;

import com.mycompany.myapp.domain.enumeration.OrderStatus;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;
import javax.validation.constraints.*;

/**
 * A DTO for the {@link com.mycompany.myapp.domain.TransferOrder} entity.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class TransferOrderDTO implements Serializable {

    private Long id;

    @NotNull
    private String transferCode;

    @NotNull
    private OrderStatus status;

    private WarehouseDTO fromWarehouse;

    private WarehouseDTO toWarehouse;

    // Thêm cái list này vào dưới các thuộc tính
    private List<TransferOrderLineDTO> transferOrderLines;

    // Thêm Getter / Setter
    public List<TransferOrderLineDTO> getTransferOrderLines() {
        return transferOrderLines;
    }

    public void setTransferOrderLines(List<TransferOrderLineDTO> transferOrderLines) {
        this.transferOrderLines = transferOrderLines;
    }

    private EmployeeDTO employee;

    public EmployeeDTO getEmployee() {
        return employee;
    }

    public void setEmployee(EmployeeDTO employee) {
        this.employee = employee;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTransferCode() {
        return transferCode;
    }

    public void setTransferCode(String transferCode) {
        this.transferCode = transferCode;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public WarehouseDTO getFromWarehouse() {
        return fromWarehouse;
    }

    public void setFromWarehouse(WarehouseDTO fromWarehouse) {
        this.fromWarehouse = fromWarehouse;
    }

    public WarehouseDTO getToWarehouse() {
        return toWarehouse;
    }

    public void setToWarehouse(WarehouseDTO toWarehouse) {
        this.toWarehouse = toWarehouse;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof TransferOrderDTO)) {
            return false;
        }

        TransferOrderDTO transferOrderDTO = (TransferOrderDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, transferOrderDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "TransferOrderDTO{" +
            "id=" + getId() +
            ", transferCode='" + getTransferCode() + "'" +
            ", status='" + getStatus() + "'" +
            ", fromWarehouse=" + getFromWarehouse() +
            ", toWarehouse=" + getToWarehouse() +
            "}";
    }
}
