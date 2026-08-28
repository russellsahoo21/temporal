package com.example.demo.model;

import jakarta.persistence.*;

@Entity
@Table(name = "dim_store")
public class StoreDimension {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String storeName;

    private String city;
    private String state;
    private String region;

    public StoreDimension() {}

    public StoreDimension(String storeName, String city, String state, String region) {
        this.storeName = storeName;
        this.city = city;
        this.state = state;
        this.region = region;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getStoreName() {
        return storeName;
    }

    public void setStoreName(String storeName) {
        this.storeName = storeName;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }
}
