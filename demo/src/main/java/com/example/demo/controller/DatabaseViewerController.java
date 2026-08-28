package com.example.demo.controller;

import com.example.demo.model.ProductDimension;
import com.example.demo.model.SaleFact;
import com.example.demo.model.StoreDimension;
import com.example.demo.model.TimeDimension;
import com.example.demo.repository.ProductDimensionRepository;
import com.example.demo.repository.SaleFactRepository;
import com.example.demo.repository.StoreDimensionRepository;
import com.example.demo.repository.TimeDimensionRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/db-viewer")
@CrossOrigin(origins = "*")
public class DatabaseViewerController {

    private final TimeDimensionRepository timeRepo;
    private final ProductDimensionRepository productRepo;
    private final StoreDimensionRepository storeRepo;
    private final SaleFactRepository saleRepo;

    public DatabaseViewerController(TimeDimensionRepository timeRepo,
                                    ProductDimensionRepository productRepo,
                                    StoreDimensionRepository storeRepo,
                                    SaleFactRepository saleRepo) {
        this.timeRepo = timeRepo;
        this.productRepo = productRepo;
        this.storeRepo = storeRepo;
        this.saleRepo = saleRepo;
    }

    @GetMapping("/overview")
    public ResponseEntity<Map<String, Object>> getDatabaseOverview() {
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("dim_time_count", timeRepo.count());
        stats.put("dim_product_count", productRepo.count());
        stats.put("dim_store_count", storeRepo.count());
        stats.put("fact_sales_count", saleRepo.count());
        
        List<Map<String, String>> tables = List.of(
            Map.of("name", "fact_sales", "type", "Fact Table (Temporal)", "description", "Stores sales transactions with valid_from, valid_to temporal tracking"),
            Map.of("name", "dim_time", "type", "Dimension Table", "description", "Stores calendar hierarchies: year, quarter, month, season, weekday"),
            Map.of("name", "dim_product", "type", "Dimension Table", "description", "Stores product hierarchies: brand, category, unit cost"),
            Map.of("name", "dim_store", "type", "Dimension Table", "description", "Stores geographical hierarchy: store name, city, state, region")
        );
        stats.put("tables", tables);

        return ResponseEntity.ok(stats);
    }

    @GetMapping("/table/{tableName}")
    public ResponseEntity<?> getTableData(@PathVariable String tableName) {
        switch (tableName.toLowerCase()) {
            case "dim_time":
                return ResponseEntity.ok(timeRepo.findAll());
            case "dim_product":
                return ResponseEntity.ok(productRepo.findAll());
            case "dim_store":
                return ResponseEntity.ok(storeRepo.findAll());
            case "fact_sales":
                return ResponseEntity.ok(saleRepo.findAll());
            default:
                return ResponseEntity.badRequest().body(Map.of("error", "Table not found: " + tableName));
        }
    }
}
