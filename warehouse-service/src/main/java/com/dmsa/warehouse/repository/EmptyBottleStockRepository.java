package com.dmsa.warehouse.repository;

import com.dmsa.warehouse.model.EmptyBottleStock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmptyBottleStockRepository extends JpaRepository<EmptyBottleStock, Long> { }
