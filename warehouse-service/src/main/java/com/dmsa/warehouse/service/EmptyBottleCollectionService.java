package com.dmsa.warehouse.service;

import com.dmsa.warehouse.client.DropPointServiceClient;
import com.dmsa.warehouse.dto.external.DropPointDto;
import com.dmsa.warehouse.dto.response.CollectionResponse;
import com.dmsa.warehouse.dto.response.EmptyBottleInventoryResponse;
import com.dmsa.warehouse.exception.InvalidStateTransitionException;
import com.dmsa.warehouse.exception.ResourceNotFoundException;
import com.dmsa.warehouse.model.entity.DropPointCollection;
import com.dmsa.warehouse.model.entity.EmptyBottleInventory;
import com.dmsa.warehouse.model.enums.CollectionStatus;
import com.dmsa.warehouse.repository.DropPointCollectionRepository;
import com.dmsa.warehouse.repository.EmptyBottleInventoryRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for managing empty bottle collection from drop points.
 * Handles the workflow of fetching notifications, accepting collections,
 * and tracking collected bottle inventory.
 */
@Service
public class EmptyBottleCollectionService {

    private static final Logger log = LoggerFactory.getLogger(EmptyBottleCollectionService.class);

    private final DropPointServiceClient dropPointClient;
    private final DropPointCollectionRepository collectionRepository;
    private final EmptyBottleInventoryRepository inventoryRepository;

    public EmptyBottleCollectionService(DropPointServiceClient dropPointClient,
            DropPointCollectionRepository collectionRepository,
            EmptyBottleInventoryRepository inventoryRepository) {
        this.dropPointClient = dropPointClient;
        this.collectionRepository = collectionRepository;
        this.inventoryRepository = inventoryRepository;
    }

    /**
     * Fetch and sync drop point notifications from drop-point service.
     * Creates or updates local collection records for notified drop points.
     */
    @Transactional
    @CircuitBreaker(name = "dropPointService")
    public List<CollectionResponse> syncDropPointNotifications() {
        log.info("Syncing drop point notifications");

        List<DropPointDto> allDropPoints = dropPointClient.getAllDropPoints();
        List<CollectionResponse> result = new ArrayList<>();

        for (DropPointDto dropPoint : allDropPoints) {
            try {
                CollectionResponse response = processDropPoint(dropPoint);
                if (response != null) {
                    result.add(response);
                }
            } catch (Exception e) {
                log.error("Error processing drop point {}: {}", dropPoint.getId(), e.getMessage());
            }
        }

        log.info("Synced {} drop point notifications", result.size());
        return result;
    }

    private CollectionResponse processDropPoint(DropPointDto dropPoint) {
        if (dropPoint.isNotifiedToWarehouse()) {
            // Check if we already have a collection record
            if (collectionRepository.hasActiveCollection(dropPoint.getId())) {
                log.debug("Active collection already exists for drop point {}", dropPoint.getId());
                return null;
            }

            // Create new collection record
            DropPointCollection collection = new DropPointCollection(
                    dropPoint.getId(),
                    dropPoint.getLocation(),
                    dropPoint.getCurrentEmpties());
            collection = collectionRepository.save(collection);
            log.info("Created collection record for drop point {}", dropPoint.getId());
            return CollectionResponse.fromEntity(collection);

        } else if (dropPoint.isEmpty()) {
            // Handle reset scenario - mark any ACCEPTED collections as needs reset
            List<CollectionStatus> activeStatuses = List.of(
                    CollectionStatus.PENDING,
                    CollectionStatus.ACCEPTED);
            collectionRepository.findActiveByDropPointId(dropPoint.getId(), activeStatuses)
                    .ifPresent(collection -> {
                        if (collection.getStatus() == CollectionStatus.ACCEPTED) {
                            log.info("Drop point {} was emptied externally, resetting collection",
                                    dropPoint.getId());
                            collection.reset();
                            collectionRepository.save(collection);
                        }
                    });
        }

        return null;
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
