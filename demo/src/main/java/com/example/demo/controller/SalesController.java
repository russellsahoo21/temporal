package com.example.demo.controller;

import com.example.demo.dto.MultidimensionalSummaryDto;
import com.example.demo.dto.SaleRequestDto;
import com.example.demo.dto.SaleResponseDto;
import com.example.demo.service.SalesService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@RestController
@RequestMapping("/api/sales")
@CrossOrigin(origins = "*")
public class SalesController {

    private final SalesService salesService;

    public SalesController(SalesService salesService) {
        this.salesService = salesService;
    }

    @GetMapping
    public ResponseEntity<List<SaleResponseDto>> getAllSales() {
        return ResponseEntity.ok(salesService.getAllSales());
    }

    @GetMapping("/{id}")
    public ResponseEntity<SaleResponseDto> getSaleById(@PathVariable Long id) {
        return ResponseEntity.ok(salesService.getSaleById(id));
    }

    @PostMapping
    public ResponseEntity<SaleResponseDto> createSale(@RequestBody SaleRequestDto req) {
        SaleResponseDto created = salesService.createSale(req);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<SaleResponseDto> updateSale(
            @PathVariable Long id,
            @RequestBody SaleRequestDto req,
            @RequestParam(defaultValue = "false") boolean createRevision) {
        SaleResponseDto updated = salesService.updateSale(id, req, createRevision);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSale(
            @PathVariable Long id,
            @RequestParam(defaultValue = "true") boolean soft) {
        salesService.deleteSale(id, soft);
        return ResponseEntity.noContent().build();
    }

    // Temporal Point-in-time query
    @GetMapping("/temporal/as-of")
    public ResponseEntity<List<SaleResponseDto>> getSalesAsOf(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime asOf,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        LocalDateTime queryTime = asOf;
        if (queryTime == null && date != null) {
            queryTime = date.atTime(LocalTime.MAX);
        }
        if (queryTime == null) {
            queryTime = LocalDateTime.now();
        }
        return ResponseEntity.ok(salesService.getSalesAsOf(queryTime));
    }

    // Temporal Range interval query
    @GetMapping("/temporal/interval")
    public ResponseEntity<List<SaleResponseDto>> getSalesInInterval(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        LocalDateTime startTime = start.atStartOfDay();
        LocalDateTime endTime = end.atTime(LocalTime.MAX);
        return ResponseEntity.ok(salesService.getSalesInInterval(startTime, endTime));
    }

    // Multidimensional analytics summary
    @GetMapping("/summary")
    public ResponseEntity<MultidimensionalSummaryDto> getMultidimensionalSummary() {
        return ResponseEntity.ok(salesService.getMultidimensionalSummary());
    }
}
