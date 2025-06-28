package com.dmsa.warehouse.repository;

import com.dmsa.warehouse.model.EmptyBottleStock;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface EmptyBottleStockRepository extends JpaRepository<EmptyBottleStock, Long> {
  Optional<EmptyBottleStock> findByDropPointId(Long dropPointId);
}
