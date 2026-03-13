package com.nextbar.warehouse;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nextbar.warehouse.dto.response.CollectionResponse;
import com.nextbar.warehouse.event.DropPointCollectionStatusEvent;
import com.nextbar.warehouse.event.WarehouseCollectionLifecyclePublisher;
import com.nextbar.warehouse.dto.response.EmptyBottleInventoryResponse;
import com.nextbar.warehouse.exception.InvalidStateTransitionException;
import com.nextbar.warehouse.exception.ResourceNotFoundException;
import com.nextbar.warehouse.model.entity.DropPointCollection;
import com.nextbar.warehouse.model.entity.EmptyBottleInventory;
import com.nextbar.warehouse.model.enums.CollectionStatus;
import com.nextbar.warehouse.repository.DropPointCollectionRepository;
import com.nextbar.warehouse.repository.EmptyBottleInventoryRepository;

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
    private final WarehouseCollectionLifecyclePublisher collectionLifecyclePublisher;

    public EmptyBottleCollectionService(DropPointCollectionRepository collectionRepository,
            EmptyBottleInventoryRepository inventoryRepository,
            WarehouseCollectionLifecyclePublisher collectionLifecyclePublisher) {
        this.collectionRepository = collectionRepository;
        this.inventoryRepository = inventoryRepository;
        this.collectionLifecyclePublisher = collectionLifecyclePublisher;
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
     * Get a specific collection by ID.
     */
    @Transactional(readOnly = true)
    public CollectionResponse getCollection(Long id) {
        log.debug("Fetching collection with ID: {}", id);
        return CollectionResponse.fromEntity(findCollectionById(id));
    }

    /**
     * Get empty-bottle inventory summary per drop point.
     */
    @Transactional(readOnly = true)
    public List<EmptyBottleInventoryResponse> getInventorySummary() {
        log.debug("Fetching empty bottle inventory summary");
        return inventoryRepository.findAll().stream()
                .map(EmptyBottleInventoryResponse::fromEntity)
                .collect(Collectors.toList());
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
        collectionLifecyclePublisher.publishAccepted(collection.getDropPointId());

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
        collectionLifecyclePublisher.publishCollected(collection.getDropPointId());

        log.info("Collection {} marked as completed", id);
        return CollectionResponse.fromEntity(collection);
    }


    /**
     * Get total bottles collected across all drop points.
     */
    @Transactional(readOnly = true)
    public Long getTotalBottlesCollected() {
        return inventoryRepository.getTotalBottlesCollected();
    }

    /**
     * Handles async drop-point status events.
     * NOTIFIED (or FULL for compatibility) -> create/update pending collection.
     * EMPTY -> transition active collection to COLLECTED and update inventory.
     */
    @Transactional
    public void handleDropPointStatusEvent(DropPointCollectionStatusEvent event) {
        String status = event.getStatus();
        if ("NOTIFIED".equalsIgnoreCase(status) || "FULL".equalsIgnoreCase(status)) {
            upsertPendingCollection(event);
            return;
        }

        if ("EMPTY".equalsIgnoreCase(status)) {
            completeActiveCollection(event);
            return;
        }

        log.debug("Ignoring unsupported drop-point status event status={} dropPointId={}",
                status, event.getDropPointId());
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

    private void upsertPendingCollection(DropPointCollectionStatusEvent event) {
        Integer rawBottleCount = event.getBottleCount();
        Integer bottleCount = rawBottleCount != null ? rawBottleCount : 0;
        if (bottleCount <= 0) {
            log.debug("Skip pending collection upsert for dropPointId={} because bottleCount={}",
                    event.getDropPointId(), bottleCount);
            return;
        }

        List<CollectionStatus> activeStatuses = List.of(CollectionStatus.PENDING, CollectionStatus.ACCEPTED);
        DropPointCollection collection = collectionRepository.findActiveByDropPointId(event.getDropPointId(),
                activeStatuses)
                .map(existing -> {
                    existing.setLocation(event.getLocation());
                    existing.setBottleCount(bottleCount);
                    return existing;
                })
                .orElseGet(() -> new DropPointCollection(event.getDropPointId(), event.getLocation(), bottleCount));

        collectionRepository.save(collection);
        log.info("Upserted pending collection for dropPointId={} bottles={} status={}",
                event.getDropPointId(), bottleCount, collection.getStatus());
    }

    private void completeActiveCollection(DropPointCollectionStatusEvent event) {
        List<CollectionStatus> activeStatuses = List.of(CollectionStatus.PENDING, CollectionStatus.ACCEPTED);
        DropPointCollection collection = collectionRepository.findActiveByDropPointId(event.getDropPointId(),
                activeStatuses)
                .orElse(null);

        if (collection == null) {
            log.debug("No active collection found for emptied dropPointId={}, skip completion", event.getDropPointId());
            return;
        }

        if (collection.getStatus() == CollectionStatus.PENDING) {
            collection.accept();
        }
        if (collection.getStatus() == CollectionStatus.ACCEPTED) {
            collection.markCollected();
        }

        collection = collectionRepository.save(collection);
        updateInventory(collection);
        log.info("Completed collection from async EMPTY event for dropPointId={} bottles={}",
                event.getDropPointId(), collection.getBottleCount());
    }
}
