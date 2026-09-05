package com.example.demo.dto;

import java.util.List;
import java.util.Map;

public class CubeMatrixResponse {
    private String rowDimension;
    private String colDimension;
    private String timeHierarchy;
    private String measure;
    
    private List<String> rowHeaders;
    private List<String> colHeaders;
    private Double[][] matrix;
    private Double[] rowTotals;
    private Double[] colTotals;
    private Double grandTotal;

    // Active OLAP Operations & Explanations for demoing to teacher
    private String appliedOperation; // "PIVOT", "SLICE", "DICE", "DRILL_DOWN", "ROLL_UP", "STANDARD"
    private String operationTitle;
    private String operationExplanation;

    public CubeMatrixResponse() {}

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

    public List<String> getRowHeaders() {
        return rowHeaders;
    }

    public void setRowHeaders(List<String> rowHeaders) {
        this.rowHeaders = rowHeaders;
    }

    public List<String> getColHeaders() {
        return colHeaders;
    }

    public void setColHeaders(List<String> colHeaders) {
        this.colHeaders = colHeaders;
    }

    public Double[][] getMatrix() {
        return matrix;
    }

    public void setMatrix(Double[][] matrix) {
        this.matrix = matrix;
    }

    public Double[] getRowTotals() {
        return rowTotals;
    }

    public void setRowTotals(Double[] rowTotals) {
        this.rowTotals = rowTotals;
    }

    public Double[] getColTotals() {
        return colTotals;
    }

    public void setColTotals(Double[] colTotals) {
        this.colTotals = colTotals;
    }

    public Double getGrandTotal() {
        return grandTotal;
    }

    public void setGrandTotal(Double grandTotal) {
        this.grandTotal = grandTotal;
    }

    public String getAppliedOperation() {
        return appliedOperation;
    }

    public void setAppliedOperation(String appliedOperation) {
        this.appliedOperation = appliedOperation;
    }

    public String getOperationTitle() {
        return operationTitle;
    }

    public void setOperationTitle(String operationTitle) {
        this.operationTitle = operationTitle;
    }

    public String getOperationExplanation() {
        return operationExplanation;
    }

    public void setOperationExplanation(String operationExplanation) {
        this.operationExplanation = operationExplanation;
    }
}
