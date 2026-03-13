package com.nextbar.warehouse.repository;

import com.nextbar.warehouse.model.entity.EmptyBottleInventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for empty bottle inventory tracking.
 */
@Repository
public interface EmptyBottleInventoryRepository extends JpaRepository<EmptyBottleInventory, Long> {

    /**
     * Find inventory record by drop point ID.
     */
    Optional<EmptyBottleInventory> findByDropPointId(Long dropPointId);

    /**
     * Get total bottles collected across all drop points.
     */
    @Query("SELECT COALESCE(SUM(e.totalBottlesCollected), 0) FROM EmptyBottleInventory e")
    Long getTotalBottlesCollected();
}
