package com.example.demo.service;

import com.example.demo.dto.CubeMatrixResponse;
import com.example.demo.dto.CubeQueryRequest;
import com.example.demo.model.SaleFact;
import com.example.demo.repository.SaleFactRepository;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class OlapCubeService {

    private final SaleFactRepository saleFactRepository;

    public OlapCubeService(SaleFactRepository saleFactRepository) {
        this.saleFactRepository = saleFactRepository;
    }

    public CubeMatrixResponse queryCube(CubeQueryRequest req) {
        List<SaleFact> sales = saleFactRepository.findAll();

        // 1. Detect & Apply SLICE Operation (Fixing 1 dimension)
        boolean isSliced = false;
        String sliceExplanation = "";
        if (req.getSliceDimension() != null && !req.getSliceDimension().isBlank()
                && req.getSliceValue() != null && !req.getSliceValue().isBlank()) {
            isSliced = true;
            String dim = req.getSliceDimension().trim();
            String val = req.getSliceValue().trim();
            sliceExplanation = "Slice applied on " + dim + " = '" + val + "'. A 2D sub-plane was extracted from the 3D cube.";

            sales = sales.stream().filter(s -> {
                if (dim.equalsIgnoreCase("timeYear") && s.getTimeDimension() != null) {
                    return String.valueOf(s.getTimeDimension().getYear()).equals(val);
                } else if (dim.equalsIgnoreCase("region") && s.getStoreDimension() != null) {
                    return s.getStoreDimension().getRegion().equalsIgnoreCase(val);
                } else if (dim.equalsIgnoreCase("productCategory") && s.getProductDimension() != null) {
                    return s.getProductDimension().getCategory().equalsIgnoreCase(val);
                }
                return true;
            }).collect(Collectors.toList());
        }

        // 2. Detect & Apply DICE Operation (Filtering multiple dimensions to subsets)
        boolean isDiced = false;
        StringBuilder diceExplanation = new StringBuilder();
        if (req.getDiceCategories() != null && !req.getDiceCategories().isEmpty()) {
            isDiced = true;
            sales = sales.stream()
                    .filter(s -> s.getProductDimension() != null && req.getDiceCategories().contains(s.getProductDimension().getCategory()))
                    .collect(Collectors.toList());
            diceExplanation.append("Categories: ").append(req.getDiceCategories()).append("; ");
        }
        if (req.getDiceRegions() != null && !req.getDiceRegions().isEmpty()) {
            isDiced = true;
            sales = sales.stream()
                    .filter(s -> s.getStoreDimension() != null && req.getDiceRegions().contains(s.getStoreDimension().getRegion()))
                    .collect(Collectors.toList());
            diceExplanation.append("Regions: ").append(req.getDiceRegions()).append("; ");
        }
        if (req.getDiceYears() != null && !req.getDiceYears().isEmpty()) {
            isDiced = true;
            sales = sales.stream()
                    .filter(s -> s.getTimeDimension() != null && req.getDiceYears().contains(s.getTimeDimension().getYear()))
                    .collect(Collectors.toList());
            diceExplanation.append("Years: ").append(req.getDiceYears()).append("; ");
        }

        // 3. Extract distinct Row Headers and Column Headers
        String rowDim = req.getRowDimension() != null ? req.getRowDimension() : "productCategory";
        String colDim = req.getColDimension() != null ? req.getColDimension() : "timePeriod";
        String timeHier = req.getTimeHierarchy() != null ? req.getTimeHierarchy().toUpperCase() : "QUARTER";
        String measure = req.getMeasure() != null ? req.getMeasure().toUpperCase() : "REVENUE";

        TreeSet<String> rowSet = new TreeSet<>();
        TreeSet<String> colSet = new TreeSet<>();

        for (SaleFact s : sales) {
            rowSet.add(getDimValue(s, rowDim, timeHier));
            colSet.add(getDimValue(s, colDim, timeHier));
        }

        List<String> rowHeaders = new ArrayList<>(rowSet);
        List<String> colHeaders = new ArrayList<>(colSet);

        // Map cells: rowKey -> colKey -> aggregate measure
        Map<String, Map<String, Double>> cellMap = new HashMap<>();
        for (String r : rowHeaders) {
            cellMap.put(r, new HashMap<>());
            for (String c : colHeaders) {
                cellMap.get(r).put(c, 0.0);
            }
        }

        for (SaleFact s : sales) {
            String rKey = getDimValue(s, rowDim, timeHier);
            String cKey = getDimValue(s, colDim, timeHier);

            double val = 0.0;
            if (measure.equals("REVENUE")) {
                val = s.getTotalAmount() != null ? s.getTotalAmount() : 0.0;
            } else if (measure.equals("QUANTITY")) {
                val = s.getQuantity() != null ? s.getQuantity() : 0.0;
            } else if (measure.equals("COUNT")) {
                val = 1.0;
            }

            Double current = cellMap.get(rKey).get(cKey);
            cellMap.get(rKey).put(cKey, current + val);
        }

        // 4. Build Matrix and Totals
        int numRows = rowHeaders.size();
        int numCols = colHeaders.size();
        Double[][] matrix = new Double[numRows][numCols];
        Double[] rowTotals = new Double[numRows];
        Double[] colTotals = new Double[numCols];
        Arrays.fill(rowTotals, 0.0);
        Arrays.fill(colTotals, 0.0);
        double grandTotal = 0.0;

        for (int i = 0; i < numRows; i++) {
            String r = rowHeaders.get(i);
            for (int j = 0; j < numCols; j++) {
                String c = colHeaders.get(j);
                double raw = cellMap.get(r).get(c);
                double rounded = Math.round(raw * 100.0) / 100.0;
                matrix[i][j] = rounded;
                rowTotals[i] += rounded;
                colTotals[j] += rounded;
                grandTotal += rounded;
            }
            rowTotals[i] = Math.round(rowTotals[i] * 100.0) / 100.0;
        }
        for (int j = 0; j < numCols; j++) {
            colTotals[j] = Math.round(colTotals[j] * 100.0) / 100.0;
        }
        grandTotal = Math.round(grandTotal * 100.0) / 100.0;

        // 5. Construct Response with Educational Metadata
        CubeMatrixResponse resp = new CubeMatrixResponse();
        resp.setRowDimension(rowDim);
        resp.setColDimension(colDim);
        resp.setTimeHierarchy(timeHier);
        resp.setMeasure(measure);
        resp.setRowHeaders(rowHeaders);
        resp.setColHeaders(colHeaders);
        resp.setMatrix(matrix);
        resp.setRowTotals(rowTotals);
        resp.setColTotals(colTotals);
        resp.setGrandTotal(grandTotal);

        // Classify Operation for Teacher Demonstration
        if (isSliced) {
            resp.setAppliedOperation("SLICE");
            resp.setOperationTitle("Slice Operation");
            resp.setOperationExplanation(sliceExplanation);
        } else if (isDiced) {
            resp.setAppliedOperation("DICE");
            resp.setOperationTitle("Dice Operation");
            resp.setOperationExplanation("Dice applied on multiple dimensions (" + diceExplanation.toString().trim() + "). A filtered sub-cube was isolated.");
        } else if (timeHier.equals("MONTH") || timeHier.equals("YEAR")) {
            resp.setAppliedOperation(timeHier.equals("MONTH") ? "DRILL_DOWN" : "ROLL_UP");
            resp.setOperationTitle(timeHier.equals("MONTH") ? "Drill-down (Time Dimension)" : "Roll-up (Time Dimension)");
            resp.setOperationExplanation(timeHier.equals("MONTH")
                    ? "Drill-down navigated from Quarter level down to Month level for finer granularity."
                    : "Roll-up aggregated the Time hierarchy up to Year level for broad trend analysis.");
        } else if (rowDim.equalsIgnoreCase("region") || rowDim.equalsIgnoreCase("storeName")) {
            resp.setAppliedOperation("PIVOT");
            resp.setOperationTitle("Pivot Operation");
            resp.setOperationExplanation("Rotated the cube's orientation: rows represent '" + rowDim + "' and columns represent '" + colDim + "'.");
        } else {
            resp.setAppliedOperation("STANDARD");
            resp.setOperationTitle("Full Multidimensional Cube View");
            resp.setOperationExplanation("Displaying all dimensions (Product x Time x Region) aggregated across the entire dataset.");
        }

        return resp;
    }

    private String getDimValue(SaleFact s, String dimName, String timeHierarchy) {
        if (s == null) return "Unknown";

        switch (dimName.toLowerCase()) {
            case "productcategory":
            case "category":
                return s.getProductDimension() != null ? s.getProductDimension().getCategory() : "Other";
            case "productbrand":
            case "brand":
                return s.getProductDimension() != null ? s.getProductDimension().getBrand() : "Generic";
            case "productname":
            case "product":
                return s.getProductDimension() != null ? s.getProductDimension().getProductName() : "Product";
            case "region":
                return s.getStoreDimension() != null ? s.getStoreDimension().getRegion() : "Region";
            case "storename":
            case "store":
                return s.getStoreDimension() != null ? s.getStoreDimension().getStoreName() : "Store";
            case "city":
                return s.getStoreDimension() != null ? s.getStoreDimension().getCity() : "City";
            case "timeperiod":
            case "time":
            default:
                if (s.getTimeDimension() == null) return "Unknown Date";
                if (timeHierarchy.equalsIgnoreCase("YEAR")) {
                    return String.valueOf(s.getTimeDimension().getYear());
                } else if (timeHierarchy.equalsIgnoreCase("MONTH")) {
                    return s.getTimeDimension().getMonthName() + " " + s.getTimeDimension().getYear();
                } else {
                    return s.getTimeDimension().getYear() + " Q" + s.getTimeDimension().getQuarter();
                }
        }
    }
}
