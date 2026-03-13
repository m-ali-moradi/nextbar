package com.nextbar.bar.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.nextbar.bar.model.BarStockItem;

/**
 * Repository interface for managing BarStockItem entities.
 * Provides methods to perform CRUD operations and custom queries related to bar stock items.
 */
@Repository
public interface BarStockItemRepository extends JpaRepository<BarStockItem, UUID> {
    /**
     * Finds all BarStockItem entities associated with a specific bar ID.
     *
     * @param barId the ID of the bar
     * @return a list of BarStockItem entities for the specified bar
     */
    List<BarStockItem> findByBarId(UUID barId);

    Optional<BarStockItem> findByBarIdAndProductNameIgnoreCase(UUID barId, String productName);

    boolean existsByBarIdAndProductNameIgnoreCase(UUID barId, String productName);

    boolean existsByBarId(UUID barId);

    void deleteByBarId(UUID barId);
}
