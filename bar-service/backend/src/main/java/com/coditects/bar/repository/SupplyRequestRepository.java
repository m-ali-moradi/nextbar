package com.coditects.bar.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.coditects.bar.model.SupplyRequest;
import com.coditects.bar.model.SupplyStatus;

/**
 * Repository interface for managing SupplyRequest entities.
 * This interface extends JpaRepository to provide CRUD operations for SupplyRequest entities.
 */
@Repository
public interface SupplyRequestRepository extends JpaRepository<SupplyRequest, UUID> {
    /**
     * Finds all supply requests associated with a specific bar ID.
     *
     * @param barId the ID of the bar for which to find supply requests
     * @return a list of SupplyRequest entities associated with the specified bar ID
     */
    List<SupplyRequest> findByBarId(UUID barId);
    // Checks if a supply request exists for a specific bar ID and a list of product IDs.
    boolean existsByBarIdAndStatusIn(UUID barId, List<SupplyStatus> statuses);
}
