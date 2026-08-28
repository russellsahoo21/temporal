package com.example.demo.controller;

import com.example.demo.model.ProductDimension;
import com.example.demo.model.StoreDimension;
import com.example.demo.model.TimeDimension;
import com.example.demo.repository.ProductDimensionRepository;
import com.example.demo.repository.StoreDimensionRepository;
import com.example.demo.repository.TimeDimensionRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/dimensions")
@CrossOrigin(origins = "*")
public class DimensionController {

    private final ProductDimensionRepository productRepo;
    private final StoreDimensionRepository storeRepo;
    private final TimeDimensionRepository timeRepo;

    public DimensionController(ProductDimensionRepository productRepo,
                               StoreDimensionRepository storeRepo,
                               TimeDimensionRepository timeRepo) {
        this.productRepo = productRepo;
        this.storeRepo = storeRepo;
        this.timeRepo = timeRepo;
    }

    @GetMapping("/products")
    public ResponseEntity<List<ProductDimension>> getProducts() {
        return ResponseEntity.ok(productRepo.findAll());
    }

    @PostMapping("/products")
    public ResponseEntity<ProductDimension> createProduct(@RequestBody ProductDimension product) {
        return ResponseEntity.ok(productRepo.save(product));
    }

    @GetMapping("/stores")
    public ResponseEntity<List<StoreDimension>> getStores() {
        return ResponseEntity.ok(storeRepo.findAll());
    }

    @PostMapping("/stores")
    public ResponseEntity<StoreDimension> createStore(@RequestBody StoreDimension store) {
        return ResponseEntity.ok(storeRepo.save(store));
    }

    @GetMapping("/times")
    public ResponseEntity<List<TimeDimension>> getTimes() {
        return ResponseEntity.ok(timeRepo.findAll());
    }
}
