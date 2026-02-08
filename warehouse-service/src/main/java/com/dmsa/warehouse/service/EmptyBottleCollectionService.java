package com.dmsa.warehouse.service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dmsa.warehouse.dto.response.CollectionResponse;
import com.dmsa.warehouse.dto.response.EmptyBottleInventoryResponse;
import com.dmsa.warehouse.exception.InvalidStateTransitionException;
import com.dmsa.warehouse.exception.ResourceNotFoundException;
import com.dmsa.warehouse.model.entity.DropPointCollection;
import com.dmsa.warehouse.model.entity.EmptyBottleInventory;
import com.dmsa.warehouse.model.enums.CollectionStatus;
import com.dmsa.warehouse.repository.DropPointCollectionRepository;
import com.dmsa.warehouse.repository.EmptyBottleInventoryRepository;

/**
 * Service for managing empty bottle collection from drop points.
 * Handles the workflow of fetching notifications, accepting collections,
 * and tracking collected bottle inventory.
 */
@Service
public class EmptyBottleCollectionService {

    private static final Logger log = LoggerFactory.getLogger(EmptyBottleCollectionService.class);

    private final DropPointCollectionRepository collectionRepository;
    private final EmptyBottleInventoryRepository inventoryRepository;

    public EmptyBottleCollectionService(DropPointCollectionRepository collectionRepository,
            EmptyBottleInventoryRepository inventoryRepository) {
        this.collectionRepository = collectionRepository;
        this.inventoryRepository = inventoryRepository;
    }

    /**
     * Fetch and sync drop point notifications from drop-point service.
     * Creates or updates local collection records for notified drop points.
     */
    @Transactional
    public List<CollectionResponse> syncDropPointNotifications() {
        log.warn("Drop point sync disabled: Feign removed from warehouse-service");
        return new ArrayList<>();
    }

    /**
     * Get all pending collections (awaiting acceptance).
     */
    @Transactional(readOnly = true)
    public List<CollectionResponse> getPendingCollections() {
        log.debug("Fetching pending collections");
        return collectionRepository.findPendingCollections().stream()
                .map(CollectionResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Get all collections (all statuses).
     */
    @Transactional(readOnly = true)
    public List<CollectionResponse> getAllCollections() {
        log.debug("Fetching all collections");
        return collectionRepository.findAll().stream()
                .map(CollectionResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Get collection by ID.
     */
    @Transactional(readOnly = true)
    public CollectionResponse getCollection(Long id) {
        log.debug("Fetching collection: {}", id);
        DropPointCollection collection = findCollectionById(id);
        return CollectionResponse.fromEntity(collection);
    }

    /**
     * Accept a collection request.
     * Transitions from PENDING to ACCEPTED.
     */
    @Transactional
    public CollectionResponse acceptCollection(Long id) {
        log.info("Accepting collection: {}", id);

        DropPointCollection collection = findCollectionById(id);

        if (collection.getStatus() != CollectionStatus.PENDING) {
            throw new InvalidStateTransitionException(
                    collection.getStatus().name(),
                    CollectionStatus.ACCEPTED.name());
        }

        collection.accept();
        collection = collectionRepository.save(collection);

        // Update inventory
        updateInventory(collection);

        log.info("Accepted collection {} with {} bottles", id, collection.getBottleCount());
        return CollectionResponse.fromEntity(collection);
    }

    /**
     * Mark a collection as fully collected.
     * Transitions from ACCEPTED to COLLECTED.
     */
    @Transactional
    public CollectionResponse markCollected(Long id) {
        log.info("Marking collection as completed: {}", id);

        DropPointCollection collection = findCollectionById(id);

        if (collection.getStatus() != CollectionStatus.ACCEPTED) {
            throw new InvalidStateTransitionException(
                    collection.getStatus().name(),
                    CollectionStatus.COLLECTED.name());
        }

        collection.markCollected();
        collection = collectionRepository.save(collection);

        log.info("Collection {} marked as completed", id);
        return CollectionResponse.fromEntity(collection);
    }

    /**
     * Get empty bottle inventory summary.
     */
    @Transactional(readOnly = true)
    public List<EmptyBottleInventoryResponse> getInventorySummary() {
        log.debug("Fetching empty bottle inventory summary");
        return inventoryRepository.findAll().stream()
                .map(EmptyBottleInventoryResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Get total bottles collected across all drop points.
     */
    @Transactional(readOnly = true)
    public Long getTotalBottlesCollected() {
        return inventoryRepository.getTotalBottlesCollected();
    }

    private DropPointCollection findCollectionById(Long id) {
        return collectionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("DropPointCollection", id));
    }

    private void updateInventory(DropPointCollection collection) {
        EmptyBottleInventory inventory = inventoryRepository
                .findByDropPointId(collection.getDropPointId())
                .orElseGet(() -> new EmptyBottleInventory(
                        collection.getDropPointId(),
                        collection.getLocation()));

        inventory.recordCollection(collection.getBottleCount());
        inventoryRepository.save(inventory);

        log.info("Updated inventory for drop point {}: total {} bottles",
                collection.getDropPointId(), inventory.getTotalBottlesCollected());
    }
}
