package com.example.demo.dto;

import java.util.List;

public class CubeQueryRequest {
    private String rowDimension = "productCategory"; // productCategory, region, storeName, productBrand, timePeriod
    private String colDimension = "timePeriod";      // timePeriod, region, productCategory, storeName
    private String timeHierarchy = "QUARTER";        // YEAR, QUARTER, MONTH
    private String measure = "REVENUE";              // REVENUE, QUANTITY, COUNT
    
    // Slice parameters (fixing 1 dimension to 1 value)
    private String sliceDimension;                   // e.g. "timeYear", "region", "productCategory"
    private String sliceValue;                       // e.g. "2025"

    // Dice parameters (filtering multiple dimensions to a subset)
    private List<String> diceCategories;
    private List<String> diceRegions;
    private List<Integer> diceYears;

    public CubeQueryRequest() {}

    public String getRowDimension() {
        return rowDimension;
    }

    public void setRowDimension(String rowDimension) {
        this.rowDimension = rowDimension;
    }

    public String getColDimension() {
        return colDimension;
    }

    public void setColDimension(String colDimension) {
        this.colDimension = colDimension;
    }

    public String getTimeHierarchy() {
        return timeHierarchy;
    }

    public void setTimeHierarchy(String timeHierarchy) {
        this.timeHierarchy = timeHierarchy;
    }

    public String getMeasure() {
        return measure;
    }

    public void setMeasure(String measure) {
        this.measure = measure;
    }

    public String getSliceDimension() {
        return sliceDimension;
    }

    public void setSliceDimension(String sliceDimension) {
        this.sliceDimension = sliceDimension;
    }

    public String getSliceValue() {
        return sliceValue;
    }

    public void setSliceValue(String sliceValue) {
        this.sliceValue = sliceValue;
    }

    public List<String> getDiceCategories() {
        return diceCategories;
    }

    public void setDiceCategories(List<String> diceCategories) {
        this.diceCategories = diceCategories;
    }

    public List<String> getDiceRegions() {
        return diceRegions;
    }

    public void setDiceRegions(List<String> diceRegions) {
        this.diceRegions = diceRegions;
    }

    public List<Integer> getDiceYears() {
        return diceYears;
    }

    public void setDiceYears(List<Integer> diceYears) {
        this.diceYears = diceYears;
    }
}
