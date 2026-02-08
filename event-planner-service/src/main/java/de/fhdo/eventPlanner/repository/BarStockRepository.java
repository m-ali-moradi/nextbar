package de.fhdo.eventPlanner.repository;

import de.fhdo.eventPlanner.model.BarStock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

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
     * Find stock entry by bar ID and item name.
     */
    java.util.Optional<BarStock> findByBarIdAndItemName(Long barId, String itemName);

    /**
     * Check if an item exists in a bar's stock.
     */
    boolean existsByBarIdAndItemName(Long barId, String itemName);

    /**
     * Delete all stock entries for a bar.
     */
    void deleteByBarId(Long barId);
}
