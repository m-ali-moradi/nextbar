package com.nextbar.eventPlanner.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.nextbar.eventPlanner.model.BarStock;

/**
 * Repository for BarStock entity operations.
 */
@Repository
public interface BarStockRepository extends JpaRepository<BarStock, Long> {

    /**
     * Find all stock entries for a specific bar.
     */
    List<BarStock> findByBarId(Long barId);

    /**
     * Find all stock entries for all bars of an event.
     */
    List<BarStock> findByBar_Event_Id(Long eventId);

    /**
     * Check if an item exists in a bar's stock.
     */
    boolean existsByBarIdAndItemName(Long barId, String itemName);
}
