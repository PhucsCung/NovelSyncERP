package com.mycompany.myapp.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.mycompany.myapp.domain.enumeration.OrderStatus;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.*;
import javax.validation.constraints.*;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * A TransferOrder.
 */
@Entity
@Table(name = "transfer_order")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@SuppressWarnings("common-java:DuplicatedBlocks")
public class TransferOrder extends AbstractAuditingEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @NotNull
    @Column(name = "transfer_code", nullable = false, unique = true)
    private String transferCode;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private OrderStatus status;

    @OneToMany(mappedBy = "transferOrder")
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @JsonIgnoreProperties(value = { "product", "transferOrder" }, allowSetters = true)
    private Set<TransferOrderLine> orderLines = new HashSet<>();

    @ManyToOne
    private Warehouse fromWarehouse;

    @ManyToOne
    private Warehouse toWarehouse;

    @ManyToOne
    @JsonIgnoreProperties(value = { "user", "manager", "scopedWarehouse", "department" }, allowSetters = true)
    private Employee employee;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Employee getEmployee() {
        return this.employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    public TransferOrder employee(Employee employee) {
        this.setEmployee(employee);
        return this;
    }

    public Long getId() {
        return this.id;
    }

    public TransferOrder id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTransferCode() {
        return this.transferCode;
    }

    public TransferOrder transferCode(String transferCode) {
        this.setTransferCode(transferCode);
        return this;
    }

    public void setTransferCode(String transferCode) {
        this.transferCode = transferCode;
    }

    public OrderStatus getStatus() {
        return this.status;
    }

    public TransferOrder status(OrderStatus status) {
        this.setStatus(status);
        return this;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public Set<TransferOrderLine> getOrderLines() {
        return this.orderLines;
    }

    public void setOrderLines(Set<TransferOrderLine> transferOrderLines) {
        if (this.orderLines != null) {
            this.orderLines.forEach(i -> i.setTransferOrder(null));
        }
        if (transferOrderLines != null) {
            transferOrderLines.forEach(i -> i.setTransferOrder(this));
        }
        this.orderLines = transferOrderLines;
    }

    public TransferOrder orderLines(Set<TransferOrderLine> transferOrderLines) {
        this.setOrderLines(transferOrderLines);
        return this;
    }

    public TransferOrder addOrderLine(TransferOrderLine transferOrderLine) {
        this.orderLines.add(transferOrderLine);
        transferOrderLine.setTransferOrder(this);
        return this;
    }

    public TransferOrder removeOrderLine(TransferOrderLine transferOrderLine) {
        this.orderLines.remove(transferOrderLine);
        transferOrderLine.setTransferOrder(null);
        return this;
    }

    public Warehouse getFromWarehouse() {
        return this.fromWarehouse;
    }

    public void setFromWarehouse(Warehouse warehouse) {
        this.fromWarehouse = warehouse;
    }

    public TransferOrder fromWarehouse(Warehouse warehouse) {
        this.setFromWarehouse(warehouse);
        return this;
    }

    public Warehouse getToWarehouse() {
        return this.toWarehouse;
    }

    public void setToWarehouse(Warehouse warehouse) {
        this.toWarehouse = warehouse;
    }

    public TransferOrder toWarehouse(Warehouse warehouse) {
        this.setToWarehouse(warehouse);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof TransferOrder)) {
            return false;
        }
        return id != null && id.equals(((TransferOrder) o).id);
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "TransferOrder{" +
            "id=" + getId() +
            ", transferCode='" + getTransferCode() + "'" +
            ", status='" + getStatus() + "'" +
            "}";
    }
}
