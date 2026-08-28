package com.example.demo.dto;

import java.util.List;
import java.util.Map;

public class MultidimensionalSummaryDto {
    private long totalSalesCount;
    private double totalRevenue;
    private long totalUnitsSold;
    private List<CategoryStat> categoryBreakdown;
    private List<RegionStat> regionBreakdown;
    private List<TimeQuarterStat> timeQuarterBreakdown;
    private List<MonthStat> monthBreakdown;

    public static class CategoryStat {
        private String category;
        private long salesCount;
        private long totalUnits;
        private double totalRevenue;

        public CategoryStat(String category, long salesCount, long totalUnits, double totalRevenue) {
            this.category = category;
            this.salesCount = salesCount;
            this.totalUnits = totalUnits;
            this.totalRevenue = totalRevenue;
        }

        public String getCategory() { return category; }
        public long getSalesCount() { return salesCount; }
        public long getTotalUnits() { return totalUnits; }
        public double getTotalRevenue() { return totalRevenue; }
    }

    public static class RegionStat {
        private String region;
        private long salesCount;
        private long totalUnits;
        private double totalRevenue;

        public RegionStat(String region, long salesCount, long totalUnits, double totalRevenue) {
            this.region = region;
            this.salesCount = salesCount;
            this.totalUnits = totalUnits;
            this.totalRevenue = totalRevenue;
        }

        public String getRegion() { return region; }
        public long getSalesCount() { return salesCount; }
        public long getTotalUnits() { return totalUnits; }
        public double getTotalRevenue() { return totalRevenue; }
    }

    public static class TimeQuarterStat {
        private int year;
        private int quarter;
        private long salesCount;
        private long totalUnits;
        private double totalRevenue;

        public TimeQuarterStat(int year, int quarter, long salesCount, long totalUnits, double totalRevenue) {
            this.year = year;
            this.quarter = quarter;
            this.salesCount = salesCount;
            this.totalUnits = totalUnits;
            this.totalRevenue = totalRevenue;
        }

        public int getYear() { return year; }
        public int getQuarter() { return quarter; }
        public long getSalesCount() { return salesCount; }
        public long getTotalUnits() { return totalUnits; }
        public double getTotalRevenue() { return totalRevenue; }
    }

    public static class MonthStat {
        private int year;
        private int month;
        private String monthName;
        private long salesCount;
        private long totalUnits;
        private double totalRevenue;

        public MonthStat(int year, int month, String monthName, long salesCount, long totalUnits, double totalRevenue) {
            this.year = year;
            this.month = month;
            this.monthName = monthName;
            this.salesCount = salesCount;
            this.totalUnits = totalUnits;
            this.totalRevenue = totalRevenue;
        }

        public int getYear() { return year; }
        public int getMonth() { return month; }
        public String getMonthName() { return monthName; }
        public long getSalesCount() { return salesCount; }
        public long getTotalUnits() { return totalUnits; }
        public double getTotalRevenue() { return totalRevenue; }
    }

    public MultidimensionalSummaryDto() {}

    public long getTotalSalesCount() { return totalSalesCount; }
    public void setTotalSalesCount(long totalSalesCount) { this.totalSalesCount = totalSalesCount; }

    public double getTotalRevenue() { return totalRevenue; }
    public void setTotalRevenue(double totalRevenue) { this.totalRevenue = totalRevenue; }

    public long getTotalUnitsSold() { return totalUnitsSold; }
    public void setTotalUnitsSold(long totalUnitsSold) { this.totalUnitsSold = totalUnitsSold; }

    public List<CategoryStat> getCategoryBreakdown() { return categoryBreakdown; }
    public void setCategoryBreakdown(List<CategoryStat> categoryBreakdown) { this.categoryBreakdown = categoryBreakdown; }

    public List<RegionStat> getRegionBreakdown() { return regionBreakdown; }
    public void setRegionBreakdown(List<RegionStat> regionBreakdown) { this.regionBreakdown = regionBreakdown; }

    public List<TimeQuarterStat> getTimeQuarterBreakdown() { return timeQuarterBreakdown; }
    public void setTimeQuarterBreakdown(List<TimeQuarterStat> timeQuarterBreakdown) { this.timeQuarterBreakdown = timeQuarterBreakdown; }

    public List<MonthStat> getMonthBreakdown() { return monthBreakdown; }
    public void setMonthBreakdown(List<MonthStat> monthBreakdown) { this.monthBreakdown = monthBreakdown; }
}
