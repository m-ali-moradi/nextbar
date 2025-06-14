package com.dmsa.warehouse.repository;

import com.dmsa.warehouse.model.BeverageStock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BeverageStockRepository extends JpaRepository<BeverageStock, Long> {
    Optional<BeverageStock> findByBeverageType(String beverageType);
}

