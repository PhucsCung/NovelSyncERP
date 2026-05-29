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
 * A PurchaseOrder.
 */
@Entity
@Table(name = "purchase_order")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@SuppressWarnings("common-java:DuplicatedBlocks")
public class PurchaseOrder implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @NotNull
    @Column(name = "po_code", nullable = false, unique = true)
    private String poCode;

    @Column(name = "total_amount", precision = 21, scale = 2)
    private BigDecimal totalAmount;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private OrderStatus status;

    @OneToMany(mappedBy = "purchaseOrder")
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @JsonIgnoreProperties(value = { "product", "purchaseOrder" }, allowSetters = true)
    private Set<PurchaseOrderLine> orderLines = new HashSet<>();

    @ManyToOne
    private Supplier supplier;

    @ManyToOne
    @JsonIgnoreProperties(value = { "user", "manager", "scopedWarehouse", "department" }, allowSetters = true)
    private Employee employee;

    @ManyToOne
    private Warehouse warehouse;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public PurchaseOrder id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPoCode() {
        return this.poCode;
    }

    public PurchaseOrder poCode(String poCode) {
        this.setPoCode(poCode);
        return this;
    }

    public void setPoCode(String poCode) {
        this.poCode = poCode;
    }

    public BigDecimal getTotalAmount() {
        return this.totalAmount;
    }

    public PurchaseOrder totalAmount(BigDecimal totalAmount) {
        this.setTotalAmount(totalAmount);
        return this;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public OrderStatus getStatus() {
        return this.status;
    }

    public PurchaseOrder status(OrderStatus status) {
        this.setStatus(status);
        return this;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public Set<PurchaseOrderLine> getOrderLines() {
        return this.orderLines;
    }

    public void setOrderLines(Set<PurchaseOrderLine> purchaseOrderLines) {
        if (this.orderLines != null) {
            this.orderLines.forEach(i -> i.setPurchaseOrder(null));
        }
        if (purchaseOrderLines != null) {
            purchaseOrderLines.forEach(i -> i.setPurchaseOrder(this));
        }
        this.orderLines = purchaseOrderLines;
    }

    public PurchaseOrder orderLines(Set<PurchaseOrderLine> purchaseOrderLines) {
        this.setOrderLines(purchaseOrderLines);
        return this;
    }

    public PurchaseOrder addOrderLine(PurchaseOrderLine purchaseOrderLine) {
        this.orderLines.add(purchaseOrderLine);
        purchaseOrderLine.setPurchaseOrder(this);
        return this;
    }

    public PurchaseOrder removeOrderLine(PurchaseOrderLine purchaseOrderLine) {
        this.orderLines.remove(purchaseOrderLine);
        purchaseOrderLine.setPurchaseOrder(null);
        return this;
    }

    public Supplier getSupplier() {
        return this.supplier;
    }

    public void setSupplier(Supplier supplier) {
        this.supplier = supplier;
    }

    public PurchaseOrder supplier(Supplier supplier) {
        this.setSupplier(supplier);
        return this;
    }

    public Employee getEmployee() {
        return this.employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    public PurchaseOrder employee(Employee employee) {
        this.setEmployee(employee);
        return this;
    }

    public Warehouse getWarehouse() {
        return this.warehouse;
    }

    public void setWarehouse(Warehouse warehouse) {
        this.warehouse = warehouse;
    }

    public PurchaseOrder warehouse(Warehouse warehouse) {
        this.setWarehouse(warehouse);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof PurchaseOrder)) {
            return false;
        }
        return id != null && id.equals(((PurchaseOrder) o).id);
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "PurchaseOrder{" +
            "id=" + getId() +
            ", poCode='" + getPoCode() + "'" +
            ", totalAmount=" + getTotalAmount() +
            ", status='" + getStatus() + "'" +
            "}";
    }
}
