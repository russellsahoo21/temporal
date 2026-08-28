package com.example.demo.service;

import com.example.demo.dto.MultidimensionalSummaryDto;
import com.example.demo.dto.SaleRequestDto;
import com.example.demo.dto.SaleResponseDto;
import com.example.demo.model.ProductDimension;
import com.example.demo.model.SaleFact;
import com.example.demo.model.StoreDimension;
import com.example.demo.model.TimeDimension;
import com.example.demo.repository.ProductDimensionRepository;
import com.example.demo.repository.SaleFactRepository;
import com.example.demo.repository.StoreDimensionRepository;
import com.example.demo.repository.TimeDimensionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class SalesService {

    private final SaleFactRepository saleFactRepository;
    private final ProductDimensionRepository productDimensionRepository;
    private final StoreDimensionRepository storeDimensionRepository;
    private final TimeDimensionRepository timeDimensionRepository;

    public SalesService(SaleFactRepository saleFactRepository,
                        ProductDimensionRepository productDimensionRepository,
                        StoreDimensionRepository storeDimensionRepository,
                        TimeDimensionRepository timeDimensionRepository) {
        this.saleFactRepository = saleFactRepository;
        this.productDimensionRepository = productDimensionRepository;
        this.storeDimensionRepository = storeDimensionRepository;
        this.timeDimensionRepository = timeDimensionRepository;
    }

    public List<SaleResponseDto> getAllSales() {
        return saleFactRepository.findAllByOrderByIdDesc().stream()
                .map(SaleResponseDto::fromEntity)
                .collect(Collectors.toList());
    }

    public SaleResponseDto getSaleById(Long id) {
        SaleFact sale = saleFactRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Sale record not found with ID: " + id));
        return SaleResponseDto.fromEntity(sale);
    }

    public SaleResponseDto createSale(SaleRequestDto req) {
        SaleFact sale = new SaleFact();
        populateSaleFromDto(sale, req);
        
        if (sale.getValidFrom() == null) {
            sale.setValidFrom(LocalDateTime.now());
        }
        sale.setTransactionTime(LocalDateTime.now());
        if (sale.getStatus() == null || sale.getStatus().isBlank()) {
            sale.setStatus("ACTIVE");
        }

        SaleFact saved = saleFactRepository.save(sale);
        return SaleResponseDto.fromEntity(saved);
    }

    public SaleResponseDto updateSale(Long id, SaleRequestDto req, boolean createTemporalRevision) {
        SaleFact existing = saleFactRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Sale record not found with ID: " + id));

        LocalDateTime now = LocalDateTime.now();

        if (createTemporalRevision) {
            // Temporal DB Pattern: End validity of previous version
            existing.setValidTo(now);
            existing.setStatus("REVISED");
            saleFactRepository.save(existing);

            // Create new active version starting now
            SaleFact revised = new SaleFact();
            populateSaleFromDto(revised, req);
            revised.setValidFrom(now);
            revised.setValidTo(req.getValidTo());
            revised.setTransactionTime(now);
            revised.setStatus("ACTIVE");
            SaleFact saved = saleFactRepository.save(revised);
            return SaleResponseDto.fromEntity(saved);
        } else {
            // Standard In-Place update
            populateSaleFromDto(existing, req);
            SaleFact saved = saleFactRepository.save(existing);
            return SaleResponseDto.fromEntity(saved);
        }
    }

    public void deleteSale(Long id, boolean softTemporalDelete) {
        SaleFact existing = saleFactRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Sale record not found with ID: " + id));

        if (softTemporalDelete) {
            // Temporal DB soft delete: expire valid_to and mark CANCELLED
            existing.setValidTo(LocalDateTime.now());
            existing.setStatus("CANCELLED");
            saleFactRepository.save(existing);
        } else {
            saleFactRepository.delete(existing);
        }
    }

    public List<SaleResponseDto> getSalesAsOf(LocalDateTime asOf) {
        return saleFactRepository.findSalesAsOf(asOf).stream()
                .map(SaleResponseDto::fromEntity)
                .collect(Collectors.toList());
    }

    public List<SaleResponseDto> getSalesInInterval(LocalDateTime start, LocalDateTime end) {
        return saleFactRepository.findSalesInInterval(start, end).stream()
                .map(SaleResponseDto::fromEntity)
                .collect(Collectors.toList());
    }

    public MultidimensionalSummaryDto getMultidimensionalSummary() {
        List<SaleFact> allSales = saleFactRepository.findAll();
        
        long totalCount = allSales.size();
        long totalUnits = allSales.stream().mapToLong(s -> s.getQuantity() != null ? s.getQuantity() : 0).sum();
        double totalRevenue = allSales.stream().mapToDouble(s -> s.getTotalAmount() != null ? s.getTotalAmount() : 0.0).sum();

        MultidimensionalSummaryDto dto = new MultidimensionalSummaryDto();
        dto.setTotalSalesCount(totalCount);
        dto.setTotalUnitsSold(totalUnits);
        dto.setTotalRevenue(Math.round(totalRevenue * 100.0) / 100.0);

        // Category breakdown
        List<Object[]> catRows = saleFactRepository.aggregateByCategory();
        List<MultidimensionalSummaryDto.CategoryStat> catStats = new ArrayList<>();
        for (Object[] row : catRows) {
            String category = (String) row[0];
            long count = ((Number) row[1]).longValue();
            long units = row[2] != null ? ((Number) row[2]).longValue() : 0;
            double rev = row[3] != null ? ((Number) row[3]).doubleValue() : 0.0;
            catStats.add(new MultidimensionalSummaryDto.CategoryStat(category != null ? category : "Other", count, units, Math.round(rev * 100.0) / 100.0));
        }
        dto.setCategoryBreakdown(catStats);

        // Region breakdown
        List<Object[]> regRows = saleFactRepository.aggregateByRegion();
        List<MultidimensionalSummaryDto.RegionStat> regStats = new ArrayList<>();
        for (Object[] row : regRows) {
            String region = (String) row[0];
            long count = ((Number) row[1]).longValue();
            long units = row[2] != null ? ((Number) row[2]).longValue() : 0;
            double rev = row[3] != null ? ((Number) row[3]).doubleValue() : 0.0;
            regStats.add(new MultidimensionalSummaryDto.RegionStat(region != null ? region : "Other", count, units, Math.round(rev * 100.0) / 100.0));
        }
        dto.setRegionBreakdown(regStats);

        // Time Quarter breakdown
        List<Object[]> tqRows = saleFactRepository.aggregateByTimeQuarter();
        List<MultidimensionalSummaryDto.TimeQuarterStat> tqStats = new ArrayList<>();
        for (Object[] row : tqRows) {
            int year = ((Number) row[0]).intValue();
            int quarter = ((Number) row[1]).intValue();
            long count = ((Number) row[2]).longValue();
            long units = row[3] != null ? ((Number) row[3]).longValue() : 0;
            double rev = row[4] != null ? ((Number) row[4]).doubleValue() : 0.0;
            tqStats.add(new MultidimensionalSummaryDto.TimeQuarterStat(year, quarter, count, units, Math.round(rev * 100.0) / 100.0));
        }
        dto.setTimeQuarterBreakdown(tqStats);

        // Month breakdown
        List<Object[]> mRows = saleFactRepository.aggregateByMonth();
        List<MultidimensionalSummaryDto.MonthStat> mStats = new ArrayList<>();
        for (Object[] row : mRows) {
            int year = ((Number) row[0]).intValue();
            int month = ((Number) row[1]).intValue();
            String monthName = (String) row[2];
            long count = ((Number) row[3]).longValue();
            long units = row[4] != null ? ((Number) row[4]).longValue() : 0;
            double rev = row[5] != null ? ((Number) row[5]).doubleValue() : 0.0;
            mStats.add(new MultidimensionalSummaryDto.MonthStat(year, month, monthName, count, units, Math.round(rev * 100.0) / 100.0));
        }
        dto.setMonthBreakdown(mStats);

        return dto;
    }

    private void populateSaleFromDto(SaleFact sale, SaleRequestDto req) {
        sale.setCustomerName(req.getCustomerName() != null && !req.getCustomerName().isBlank() ? req.getCustomerName() : "General Customer");
        sale.setQuantity(req.getQuantity() != null ? req.getQuantity() : 1);
        sale.setUnitPrice(req.getUnitPrice() != null ? req.getUnitPrice() : 100.0);
        sale.setDiscount(req.getDiscount() != null ? req.getDiscount() : 0.0);
        
        double total = (sale.getQuantity() * sale.getUnitPrice()) - sale.getDiscount();
        sale.setTotalAmount(Math.max(0.0, Math.round(total * 100.0) / 100.0));

        if (req.getValidFrom() != null) {
            sale.setValidFrom(req.getValidFrom());
        }
        sale.setValidTo(req.getValidTo());
        if (req.getStatus() != null && !req.getStatus().isBlank()) {
            sale.setStatus(req.getStatus());
        }

        // 1. Resolve Product Dimension
        ProductDimension product;
        if (req.getProductId() != null) {
            product = productDimensionRepository.findById(req.getProductId())
                    .orElseGet(() -> createOrGetProduct(req.getProductName(), req.getProductCategory(), req.getProductBrand(), req.getUnitPrice()));
        } else if (req.getProductName() != null && !req.getProductName().isBlank()) {
            product = createOrGetProduct(req.getProductName(), req.getProductCategory(), req.getProductBrand(), req.getUnitPrice());
        } else {
            product = productDimensionRepository.findAll().stream().findFirst()
                    .orElseGet(() -> createOrGetProduct("Standard Item", "General", "Generic", 50.0));
        }
        sale.setProductDimension(product);

        // 2. Resolve Store Dimension
        StoreDimension store;
        if (req.getStoreId() != null) {
            store = storeDimensionRepository.findById(req.getStoreId())
                    .orElseGet(() -> createOrGetStore(req.getStoreName(), req.getCity(), req.getState(), req.getRegion()));
        } else if (req.getStoreName() != null && !req.getStoreName().isBlank()) {
            store = createOrGetStore(req.getStoreName(), req.getCity(), req.getState(), req.getRegion());
        } else {
            store = storeDimensionRepository.findAll().stream().findFirst()
                    .orElseGet(() -> createOrGetStore("Main Branch", "New York", "NY", "East"));
        }
        sale.setStoreDimension(store);

        // 3. Resolve Time Dimension
        LocalDate targetDate = req.getSaleDate();
        if (targetDate == null && sale.getValidFrom() != null) {
            targetDate = sale.getValidFrom().toLocalDate();
        }
        if (targetDate == null) {
            targetDate = LocalDate.now();
        }
        final LocalDate lookupDate = targetDate;
        TimeDimension timeDim = timeDimensionRepository.findByDate(lookupDate)
                .orElseGet(() -> timeDimensionRepository.save(new TimeDimension(lookupDate)));
        sale.setTimeDimension(timeDim);
    }

    private ProductDimension createOrGetProduct(String name, String category, String brand, Double price) {
        String prodName = (name != null && !name.isBlank()) ? name : "Default Product";
        return productDimensionRepository.findByProductName(prodName)
                .orElseGet(() -> {
                    ProductDimension p = new ProductDimension();
                    p.setProductName(prodName);
                    p.setCategory((category != null && !category.isBlank()) ? category : "Electronics");
                    p.setBrand((brand != null && !brand.isBlank()) ? brand : "Global");
                    p.setUnitCost(price != null ? price * 0.7 : 50.0);
                    return productDimensionRepository.save(p);
                });
    }

    private StoreDimension createOrGetStore(String name, String city, String state, String region) {
        String sName = (name != null && !name.isBlank()) ? name : "Downtown Hub";
        return storeDimensionRepository.findByStoreName(sName)
                .orElseGet(() -> {
                    StoreDimension s = new StoreDimension();
                    s.setStoreName(sName);
                    s.setCity((city != null && !city.isBlank()) ? city : "New York");
                    s.setState((state != null && !state.isBlank()) ? state : "NY");
                    s.setRegion((region != null && !region.isBlank()) ? region : "East");
                    return storeDimensionRepository.save(s);
                });
    }
}
