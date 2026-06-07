package com.mycompany.myapp.service.dto;

import com.mycompany.myapp.domain.enumeration.OrderStatus;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import javax.validation.constraints.*;

/**
 * A DTO for the {@link com.mycompany.myapp.domain.SalesOrder} entity.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class SalesOrderDTO implements Serializable {

    private Long id;

    @NotNull
    private String orderCode;

    private BigDecimal totalAmount;

    @NotNull
    private OrderStatus status;

    private CustomerDTO customer;

    private EmployeeDTO employee;

    private WarehouseDTO warehouse;

    private List<SalesOrderLineDTO> salesOrderLines;

    private Instant createdDate;

    public Instant getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Instant createdDate) {
        this.createdDate = createdDate;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getOrderCode() {
        return orderCode;
    }

    public void setOrderCode(String orderCode) {
        this.orderCode = orderCode;
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

    public CustomerDTO getCustomer() {
        return customer;
    }

    public void setCustomer(CustomerDTO customer) {
        this.customer = customer;
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

    public List<SalesOrderLineDTO> getSalesOrderLines() {
        return salesOrderLines;
    }

    public void setSalesOrderLines(List<SalesOrderLineDTO> salesOrderLines) {
        this.salesOrderLines = salesOrderLines;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof SalesOrderDTO)) {
            return false;
        }

        SalesOrderDTO salesOrderDTO = (SalesOrderDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, salesOrderDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "SalesOrderDTO{" +
            "id=" + getId() +
            ", orderCode='" + getOrderCode() + "'" +
            ", createdDate=" + getCreatedDate() +
            ", totalAmount=" + getTotalAmount() +
            ", status='" + getStatus() + "'" +
            ", customer=" + getCustomer() +
            ", employee=" + getEmployee() +
            ", warehouse=" + getWarehouse() +
            "}";
    }
}
