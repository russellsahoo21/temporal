package com.example.demo.repository;

import com.example.demo.model.ProductDimension;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductDimensionRepository extends JpaRepository<ProductDimension, Long> {
    Optional<ProductDimension> findByProductName(String productName);
}
