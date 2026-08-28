package com.example.demo.repository;

import com.example.demo.model.SaleFact;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SaleFactRepository extends JpaRepository<SaleFact, Long> {

    List<SaleFact> findAllByOrderByIdDesc();

    // Temporal Point-in-Time Query: Find sales active as of a given timestamp
    @Query("SELECT s FROM SaleFact s WHERE s.validFrom <= :pointInTime AND (s.validTo IS NULL OR s.validTo >= :pointInTime)")
    List<SaleFact> findSalesAsOf(@Param("pointInTime") LocalDateTime pointInTime);

    // Temporal Range Query: Find sales valid during a given interval
    @Query("SELECT s FROM SaleFact s WHERE s.validFrom <= :endTime AND (s.validTo IS NULL OR s.validTo >= :startTime)")
    List<SaleFact> findSalesInInterval(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    // Multidimensional Query: Sales Grouped by Product Category
    @Query("SELECT s.productDimension.category, COUNT(s), SUM(s.quantity), SUM(s.totalAmount) FROM SaleFact s GROUP BY s.productDimension.category")
    List<Object[]> aggregateByCategory();

    // Multidimensional Query: Sales Grouped by Region
    @Query("SELECT s.storeDimension.region, COUNT(s), SUM(s.quantity), SUM(s.totalAmount) FROM SaleFact s GROUP BY s.storeDimension.region")
    List<Object[]> aggregateByRegion();

    // Multidimensional Query: Sales Grouped by Time (Year & Quarter)
    @Query("SELECT s.timeDimension.year, s.timeDimension.quarter, COUNT(s), SUM(s.quantity), SUM(s.totalAmount) FROM SaleFact s GROUP BY s.timeDimension.year, s.timeDimension.quarter ORDER BY s.timeDimension.year, s.timeDimension.quarter")
    List<Object[]> aggregateByTimeQuarter();

    // Multidimensional Query: Sales Grouped by Time (Month)
    @Query("SELECT s.timeDimension.year, s.timeDimension.month, s.timeDimension.monthName, COUNT(s), SUM(s.quantity), SUM(s.totalAmount) FROM SaleFact s GROUP BY s.timeDimension.year, s.timeDimension.month, s.timeDimension.monthName ORDER BY s.timeDimension.year, s.timeDimension.month")
    List<Object[]> aggregateByMonth();
}
