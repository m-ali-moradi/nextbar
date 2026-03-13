package com.nextbar.warehouse.repository;

import com.nextbar.warehouse.model.entity.SupplyRequest;
import com.nextbar.warehouse.model.enums.SupplyRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repository for managing supply requests in warehouse.
 */
@Repository
public interface SupplyRequestRepository extends JpaRepository<SupplyRequest, UUID> {

    /**
     * Find all supply requests by status.
     */
    List<SupplyRequest> findByStatus(SupplyRequestStatus status);

    /**
     * Find all supply requests for a specific bar.
     */
    List<SupplyRequest> findByBarId(UUID barId);

    /**
     * Find all supply requests ordered by creation date descending.
     */
    List<SupplyRequest> findAllByOrderByCreatedAtDesc();
}
