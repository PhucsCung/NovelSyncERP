package com.mycompany.myapp.domain;

import com.mycompany.myapp.domain.enumeration.PaymentStatus;
import com.mycompany.myapp.domain.enumeration.PaymentType;
import java.io.Serializable;
import java.math.BigDecimal;
import javax.persistence.*;
import javax.validation.constraints.*;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * A Payment.
 */
@Entity
@Table(name = "payment")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@SuppressWarnings("common-java:DuplicatedBlocks")
public class Payment extends AbstractAuditingEntity<Long> implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @NotNull
    @Column(name = "payment_code", nullable = false, unique = true)
    private String paymentCode;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private PaymentType type;

    @NotNull
    @Column(name = "amount", precision = 21, scale = 2, nullable = false)
    private BigDecimal amount;

    @Column(name = "reference_order_id")
    private Long referenceOrderId;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private PaymentStatus status;

    @Column(name = "note", length = 1000)
    private String note;

    @ManyToOne
    private Customer customer;

    @ManyToOne
    private Supplier supplier;

    @Override
    public Long getId() {
        return this.id;
    }

    public Payment id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPaymentCode() {
        return this.paymentCode;
    }

    public Payment paymentCode(String paymentCode) {
        this.setPaymentCode(paymentCode);
        return this;
    }

    public void setPaymentCode(String paymentCode) {
        this.paymentCode = paymentCode;
    }

    public PaymentType getType() {
        return this.type;
    }

    public Payment type(PaymentType type) {
        this.setType(type);
        return this;
    }

    public void setType(PaymentType type) {
        this.type = type;
    }

    public BigDecimal getAmount() {
        return this.amount;
    }

    public Payment amount(BigDecimal amount) {
        this.setAmount(amount);
        return this;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public Long getReferenceOrderId() {
        return this.referenceOrderId;
    }

    public Payment referenceOrderId(Long referenceOrderId) {
        this.setReferenceOrderId(referenceOrderId);
        return this;
    }

    public void setReferenceOrderId(Long referenceOrderId) {
        this.referenceOrderId = referenceOrderId;
    }

    public PaymentStatus getStatus() {
        return this.status;
    }

    public Payment status(PaymentStatus status) {
        this.setStatus(status);
        return this;
    }

    public void setStatus(PaymentStatus status) {
        this.status = status;
    }

    public String getNote() {
        return this.note;
    }

    public Payment note(String note) {
        this.setNote(note);
        return this;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public Customer getCustomer() {
        return this.customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public Payment customer(Customer customer) {
        this.setCustomer(customer);
        return this;
    }

    public Supplier getSupplier() {
        return this.supplier;
    }

    public void setSupplier(Supplier supplier) {
        this.supplier = supplier;
    }

    public Payment supplier(Supplier supplier) {
        this.setSupplier(supplier);
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Payment)) {
            return false;
        }
        return id != null && id.equals(((Payment) o).id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return (
            "Payment{" +
            "id=" +
            getId() +
            ", paymentCode='" +
            getPaymentCode() +
            "'" +
            ", type='" +
            getType() +
            "'" +
            ", amount=" +
            getAmount() +
            ", referenceOrderId=" +
            getReferenceOrderId() +
            ", status='" +
            getStatus() +
            "'" +
            ", note='" +
            getNote() +
            "'" +
            "}"
        );
    }
}
