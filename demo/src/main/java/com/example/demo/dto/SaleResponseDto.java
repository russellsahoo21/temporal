package com.example.demo.dto;

import com.example.demo.model.SaleFact;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class SaleResponseDto {
    private Long id;
    private String customerName;
    private Integer quantity;
    private Double unitPrice;
    private Double discount;
    private Double totalAmount;

    // Product Dimension
    private Long productId;
    private String productName;
    private String productCategory;
    private String productBrand;

    // Store Dimension
    private Long storeId;
    private String storeName;
    private String city;
    private String state;
    private String region;

    // Time Dimension
    private Long timeId;
    private LocalDate saleDate;
    private Integer year;
    private Integer quarter;
    private Integer month;
    private String monthName;
    private String season;
    private Boolean isWeekend;

    // Temporal Fields
    private LocalDateTime validFrom;
    private LocalDateTime validTo;
    private LocalDateTime transactionTime;
    private String status;

    public SaleResponseDto() {}

    public static SaleResponseDto fromEntity(SaleFact sale) {
        SaleResponseDto dto = new SaleResponseDto();
        dto.setId(sale.getId());
        dto.setCustomerName(sale.getCustomerName());
        dto.setQuantity(sale.getQuantity());
        dto.setUnitPrice(sale.getUnitPrice());
        dto.setDiscount(sale.getDiscount());
        dto.setTotalAmount(sale.getTotalAmount());

        if (sale.getProductDimension() != null) {
            dto.setProductId(sale.getProductDimension().getId());
            dto.setProductName(sale.getProductDimension().getProductName());
            dto.setProductCategory(sale.getProductDimension().getCategory());
            dto.setProductBrand(sale.getProductDimension().getBrand());
        }

        if (sale.getStoreDimension() != null) {
            dto.setStoreId(sale.getStoreDimension().getId());
            dto.setStoreName(sale.getStoreDimension().getStoreName());
            dto.setCity(sale.getStoreDimension().getCity());
            dto.setState(sale.getStoreDimension().getState());
            dto.setRegion(sale.getStoreDimension().getRegion());
        }

        if (sale.getTimeDimension() != null) {
            dto.setTimeId(sale.getTimeDimension().getId());
            dto.setSaleDate(sale.getTimeDimension().getDate());
            dto.setYear(sale.getTimeDimension().getYear());
            dto.setQuarter(sale.getTimeDimension().getQuarter());
            dto.setMonth(sale.getTimeDimension().getMonth());
            dto.setMonthName(sale.getTimeDimension().getMonthName());
            dto.setSeason(sale.getTimeDimension().getSeason());
            dto.setIsWeekend(sale.getTimeDimension().getIsWeekend());
        }

        dto.setValidFrom(sale.getValidFrom());
        dto.setValidTo(sale.getValidTo());
        dto.setTransactionTime(sale.getTransactionTime());
        dto.setStatus(sale.getStatus());

        return dto;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public Double getUnitPrice() { return unitPrice; }
    public void setUnitPrice(Double unitPrice) { this.unitPrice = unitPrice; }

    public Double getDiscount() { return discount; }
    public void setDiscount(Double discount) { this.discount = discount; }

    public Double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(Double totalAmount) { this.totalAmount = totalAmount; }

    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public String getProductCategory() { return productCategory; }
    public void setProductCategory(String productCategory) { this.productCategory = productCategory; }

    public String getProductBrand() { return productBrand; }
    public void setProductBrand(String productBrand) { this.productBrand = productBrand; }

    public Long getStoreId() { return storeId; }
    public void setStoreId(Long storeId) { this.storeId = storeId; }

    public String getStoreName() { return storeName; }
    public void setStoreName(String storeName) { this.storeName = storeName; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }

    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }

    public Long getTimeId() { return timeId; }
    public void setTimeId(Long timeId) { this.timeId = timeId; }

    public LocalDate getSaleDate() { return saleDate; }
    public void setSaleDate(LocalDate saleDate) { this.saleDate = saleDate; }

    public Integer getYear() { return year; }
    public void setYear(Integer year) { this.year = year; }

    public Integer getQuarter() { return quarter; }
    public void setQuarter(Integer quarter) { this.quarter = quarter; }

    public Integer getMonth() { return month; }
    public void setMonth(Integer month) { this.month = month; }

    public String getMonthName() { return monthName; }
    public void setMonthName(String monthName) { this.monthName = monthName; }

    public String getSeason() { return season; }
    public void setSeason(String season) { this.season = season; }

    public Boolean getIsWeekend() { return isWeekend; }
    public void setIsWeekend(Boolean weekend) { isWeekend = weekend; }

    public LocalDateTime getValidFrom() { return validFrom; }
    public void setValidFrom(LocalDateTime validFrom) { this.validFrom = validFrom; }

    public LocalDateTime getValidTo() { return validTo; }
    public void setValidTo(LocalDateTime validTo) { this.validTo = validTo; }

    public LocalDateTime getTransactionTime() { return transactionTime; }
    public void setTransactionTime(LocalDateTime transactionTime) { this.transactionTime = transactionTime; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
