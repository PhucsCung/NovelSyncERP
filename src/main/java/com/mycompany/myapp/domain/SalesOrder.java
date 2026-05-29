package com.mycompany.myapp.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.mycompany.myapp.domain.enumeration.OrderStatus;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.*;
import javax.validation.constraints.*;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * A SalesOrder.
 */
@Entity
@Table(name = "sales_order")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@SuppressWarnings("common-java:DuplicatedBlocks")
public class SalesOrder implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @NotNull
    @Column(name = "order_code", nullable = false, unique = true)
    private String orderCode;

    @Column(name = "total_amount", precision = 21, scale = 2)
    private BigDecimal totalAmount;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private OrderStatus status;

    @OneToMany(mappedBy = "salesOrder")
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @JsonIgnoreProperties(value = { "product", "salesOrder" }, allowSetters = true)
    private Set<SalesOrderLine> orderLines = new HashSet<>();

    @ManyToOne
    private Customer customer;

    @ManyToOne
    @JsonIgnoreProperties(value = { "user", "manager", "scopedWarehouse", "department" }, allowSetters = true)
    private Employee employee;

    @ManyToOne
    private Warehouse warehouse;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public SalesOrder id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getOrderCode() {
        return this.orderCode;
    }

    public SalesOrder orderCode(String orderCode) {
        this.setOrderCode(orderCode);
        return this;
    }

    public void setOrderCode(String orderCode) {
        this.orderCode = orderCode;
    }

    public BigDecimal getTotalAmount() {
        return this.totalAmount;
    }

    public SalesOrder totalAmount(BigDecimal totalAmount) {
        this.setTotalAmount(totalAmount);
        return this;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public OrderStatus getStatus() {
        return this.status;
    }

    public SalesOrder status(OrderStatus status) {
        this.setStatus(status);
        return this;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public Set<SalesOrderLine> getOrderLines() {
        return this.orderLines;
    }

    public void setOrderLines(Set<SalesOrderLine> salesOrderLines) {
        if (this.orderLines != null) {
            this.orderLines.forEach(i -> i.setSalesOrder(null));
        }
        if (salesOrderLines != null) {
            salesOrderLines.forEach(i -> i.setSalesOrder(this));
        }
        this.orderLines = salesOrderLines;
    }

    public SalesOrder orderLines(Set<SalesOrderLine> salesOrderLines) {
        this.setOrderLines(salesOrderLines);
        return this;
    }

    public SalesOrder addOrderLine(SalesOrderLine salesOrderLine) {
        this.orderLines.add(salesOrderLine);
        salesOrderLine.setSalesOrder(this);
        return this;
    }

    public SalesOrder removeOrderLine(SalesOrderLine salesOrderLine) {
        this.orderLines.remove(salesOrderLine);
        salesOrderLine.setSalesOrder(null);
        return this;
    }

    public Customer getCustomer() {
        return this.customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public SalesOrder customer(Customer customer) {
        this.setCustomer(customer);
        return this;
    }

    public Employee getEmployee() {
        return this.employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    public SalesOrder employee(Employee employee) {
        this.setEmployee(employee);
        return this;
    }

    public Warehouse getWarehouse() {
        return this.warehouse;
    }

    public void setWarehouse(Warehouse warehouse) {
        this.warehouse = warehouse;
    }

    public SalesOrder warehouse(Warehouse warehouse) {
        this.setWarehouse(warehouse);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof SalesOrder)) {
            return false;
        }
        return id != null && id.equals(((SalesOrder) o).id);
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "SalesOrder{" +
            "id=" + getId() +
            ", orderCode='" + getOrderCode() + "'" +
            ", totalAmount=" + getTotalAmount() +
            ", status='" + getStatus() + "'" +
            "}";
    }
}
