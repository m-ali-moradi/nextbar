package com.nextbar.bar.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.nextbar.bar.model.UsageLog;

/**
 * Repository interface for managing UsageLog entities.
 * This interface extends JpaRepository to provide CRUD operations for UsageLog entities.
 */
@Repository
public interface UsageLogRepository extends JpaRepository<UsageLog, UUID> {
    /**
     * Finds all UsageLog entities associated with a specific bar ID.
     *
     * @param barId the ID of the bar
     * @return a list of UsageLog entities for the specified bar
     */
    List<UsageLog> findByBarId(UUID barId);

    /**
     * Finds all UsageLog entities associated with a specific bar ID and product ID.
     *
     * @param barId     the ID of the bar
     * @param productId the ID of the product
     * @return a list of UsageLog entities for the specified bar and product
     */
    List<UsageLog> findByBarIdAndProductId(UUID barId, UUID productId);

}
