package com.example.demo.repository;

import com.example.demo.model.TimeDimension;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface TimeDimensionRepository extends JpaRepository<TimeDimension, Long> {
    Optional<TimeDimension> findByDate(LocalDate date);
}
