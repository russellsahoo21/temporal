package com.example.demo.controller;

import com.example.demo.dto.CubeMatrixResponse;
import com.example.demo.dto.CubeQueryRequest;
import com.example.demo.repository.ProductDimensionRepository;
import com.example.demo.repository.StoreDimensionRepository;
import com.example.demo.repository.TimeDimensionRepository;
import com.example.demo.service.OlapCubeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/cube")
@CrossOrigin(origins = "*")
public class OlapCubeController {

    private final OlapCubeService olapCubeService;
    private final ProductDimensionRepository productRepo;
    private final StoreDimensionRepository storeRepo;
    private final TimeDimensionRepository timeRepo;

    public OlapCubeController(OlapCubeService olapCubeService,
                              ProductDimensionRepository productRepo,
                              StoreDimensionRepository storeRepo,
                              TimeDimensionRepository timeRepo) {
        this.olapCubeService = olapCubeService;
        this.productRepo = productRepo;
        this.storeRepo = storeRepo;
        this.timeRepo = timeRepo;
    }

    @PostMapping("/query")
    public ResponseEntity<CubeMatrixResponse> queryCubePost(@RequestBody CubeQueryRequest request) {
        return ResponseEntity.ok(olapCubeService.queryCube(request));
    }

    @GetMapping("/query")
    public ResponseEntity<CubeMatrixResponse> queryCubeGet(
            @RequestParam(defaultValue = "productCategory") String rowDimension,
            @RequestParam(defaultValue = "timePeriod") String colDimension,
            @RequestParam(defaultValue = "QUARTER") String timeHierarchy,
            @RequestParam(defaultValue = "REVENUE") String measure,
            @RequestParam(required = false) String sliceDimension,
            @RequestParam(required = false) String sliceValue,
            @RequestParam(required = false) List<String> diceCategories,
            @RequestParam(required = false) List<String> diceRegions,
            @RequestParam(required = false) List<Integer> diceYears) {
        CubeQueryRequest req = new CubeQueryRequest();
        req.setRowDimension(rowDimension);
        req.setColDimension(colDimension);
        req.setTimeHierarchy(timeHierarchy);
        req.setMeasure(measure);
        req.setSliceDimension(sliceDimension);
        req.setSliceValue(sliceValue);
        req.setDiceCategories(diceCategories);
        req.setDiceRegions(diceRegions);
        req.setDiceYears(diceYears);

        return ResponseEntity.ok(olapCubeService.queryCube(req));
    }

    @GetMapping("/metadata")
    public ResponseEntity<Map<String, Object>> getCubeMetadata() {
        Map<String, Object> meta = new HashMap<>();

        Set<String> categories = productRepo.findAll().stream()
                .map(p -> p.getCategory())
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(TreeSet::new));

        Set<String> regions = storeRepo.findAll().stream()
                .map(s -> s.getRegion())
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(TreeSet::new));

        Set<Integer> years = timeRepo.findAll().stream()
                .map(t -> t.getYear())
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(TreeSet::new));

        meta.put("categories", categories);
        meta.put("regions", regions);
        meta.put("years", years);
        meta.put("dimensions", List.of(
                Map.of("id", "productCategory", "label", "Product Category"),
                Map.of("id", "region", "label", "Geography / Region"),
                Map.of("id", "timePeriod", "label", "Time Period"),
                Map.of("id", "productBrand", "label", "Product Brand"),
                Map.of("id", "storeName", "label", "Store Name")
        ));
        meta.put("measures", List.of(
                Map.of("id", "REVENUE", "label", "Total Revenue ($)"),
                Map.of("id", "QUANTITY", "label", "Total Units Sold"),
                Map.of("id", "COUNT", "label", "Transaction Count")
        ));
        meta.put("timeHierarchies", List.of("YEAR", "QUARTER", "MONTH"));

        return ResponseEntity.ok(meta);
    }
}
