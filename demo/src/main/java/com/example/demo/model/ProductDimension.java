package com.example.demo.model;

import jakarta.persistence.*;

@Entity
@Table(name = "dim_product")
public class ProductDimension {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String productName;

    private String category;
    private String brand;
    private Double unitCost;

    public ProductDimension() {}

    public ProductDimension(String productName, String category, String brand, Double unitCost) {
        this.productName = productName;
        this.category = category;
        this.brand = brand;
        this.unitCost = unitCost;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public Double getUnitCost() {
        return unitCost;
    }

    public void setUnitCost(Double unitCost) {
        this.unitCost = unitCost;
    }
}
