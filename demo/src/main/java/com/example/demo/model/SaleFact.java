package com.example.demo.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "fact_sales")
public class SaleFact {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "time_id", nullable = false)
    private TimeDimension timeDimension;

    @ManyToOne(optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private ProductDimension productDimension;

    @ManyToOne(optional = false)
    @JoinColumn(name = "store_id", nullable = false)
    private StoreDimension storeDimension;

    @Column(nullable = false)
    private String customerName;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false)
    private Double unitPrice;

    private Double discount;

    @Column(nullable = false)
    private Double totalAmount;

    // --- Temporal Database Fields ---
    @Column(name = "valid_from", nullable = false)
    private LocalDateTime validFrom;

    @Column(name = "valid_to")
    private LocalDateTime validTo;

    @Column(name = "transaction_time", nullable = false)
    private LocalDateTime transactionTime;

    @Column(name = "status", nullable = false)
    private String status = "ACTIVE"; // ACTIVE, REVISED, CANCELLED

    public SaleFact() {}

    @PrePersist
    public void onPrePersist() {
        if (this.transactionTime == null) {
            this.transactionTime = LocalDateTime.now();
        }
        if (this.validFrom == null) {
            this.validFrom = LocalDateTime.now();
        }
        if (this.status == null) {
            this.status = "ACTIVE";
        }
        if (this.discount == null) {
            this.discount = 0.0;
        }
        if (this.unitPrice != null && this.quantity != null) {
            this.totalAmount = (this.quantity * this.unitPrice) - (this.discount != null ? this.discount : 0.0);
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public TimeDimension getTimeDimension() {
        return timeDimension;
    }

    public void setTimeDimension(TimeDimension timeDimension) {
        this.timeDimension = timeDimension;
    }

    public ProductDimension getProductDimension() {
        return productDimension;
    }

    public void setProductDimension(ProductDimension productDimension) {
        this.productDimension = productDimension;
    }

    public StoreDimension getStoreDimension() {
        return storeDimension;
    }

    public void setStoreDimension(StoreDimension storeDimension) {
        this.storeDimension = storeDimension;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Double getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(Double unitPrice) {
        this.unitPrice = unitPrice;
    }

    public Double getDiscount() {
        return discount;
    }

    public void setDiscount(Double discount) {
        this.discount = discount;
    }

    public Double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(Double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public LocalDateTime getValidFrom() {
        return validFrom;
    }

    public void setValidFrom(LocalDateTime validFrom) {
        this.validFrom = validFrom;
    }

    public LocalDateTime getValidTo() {
        return validTo;
    }

    public void setValidTo(LocalDateTime validTo) {
        this.validTo = validTo;
    }

    public LocalDateTime getTransactionTime() {
        return transactionTime;
    }

    public void setTransactionTime(LocalDateTime transactionTime) {
        this.transactionTime = transactionTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
