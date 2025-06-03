package com.coditects.bar.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.coditects.bar.model.BarStockItem;

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

    /**
     * Finds a BarStockItem entity by its bar ID and product ID.
     *
     * @param barId     the ID of the bar
     * @param productId the ID of the product
     * @return an Optional containing the BarStockItem if found, or empty if not found
     */
    Optional<BarStockItem> findByBarIdAndProductId(UUID barId, UUID productId);

    boolean existsByBarIdAndProductId(UUID barId, UUID productId);

    boolean existsByBarId(UUID barId);
}
