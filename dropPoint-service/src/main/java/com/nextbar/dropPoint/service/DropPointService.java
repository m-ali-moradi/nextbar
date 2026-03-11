package de.fhdo.dropPointsSys.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import de.fhdo.dropPointsSys.domain.DropPoint;
import de.fhdo.dropPointsSys.domain.DropPointStatus;
import de.fhdo.dropPointsSys.event.DropPointCollectionEventPublisher;
import de.fhdo.dropPointsSys.exception.ValidationException;
import de.fhdo.dropPointsSys.repositories.DropPointRepository;

/**
 * Service class for managing DropPoint operations.
 * Handles business logic for drop point CRUD operations, status management,
 * and event publishing for collection status changes.
 */
@Service
public class DropPointService {

    private static final int DEFAULT_CAPACITY = 100;

    private final DropPointRepository dropPointRepository;
    private final DropPointCollectionEventPublisher collectionEventPublisher;

    public DropPointService(DropPointRepository dropPointRepository,
            DropPointCollectionEventPublisher collectionEventPublisher) {
        this.dropPointRepository = dropPointRepository;
        this.collectionEventPublisher = collectionEventPublisher;
    }

    /**
     * Find a drop point by ID.
     */
    public Optional<DropPoint> findById(Long id) {
        return dropPointRepository.findById(id);
    }

    /**
     * Get all drop points.
     */
    public List<DropPoint> findAll() {
        return dropPointRepository.findAll();
    }

    /**
     * Create a new drop point.
     */
    public DropPoint save(DropPoint dropPoint) {
        return dropPointRepository.save(dropPoint);
    }

    /**
     * Update an existing drop point.
     */
    public Optional<DropPoint> update(Long id, DropPoint dropPoint) {
        return dropPointRepository.findById(id)
                .map(existing -> {
                    updateDropPointFields(existing, dropPoint);
                    return dropPointRepository.save(existing);
                });
    }

    /**
     * Delete a drop point by ID.
     */
    public boolean deleteById(Long id) {
        if (dropPointRepository.existsById(id)) {
            dropPointRepository.deleteById(id);
            return true;
        }
        return false;
    }

    /**
     * Get drop point with level and status information.
     * Currently returns the same as findById, but can be extended for additional logic.
     */
    public Optional<DropPoint> getLevelAndStatus(Long id) {
        return dropPointRepository.findById(id);
    }

    /**
     * Remove all empties from a drop point and mark it as empty.
     */
    public Optional<DropPoint> removeEmpties(Long id) {
        return dropPointRepository.findById(id)
                .map(dropPoint -> {
                    dropPoint.setCurrent_empties_stock(0);
                    dropPoint.setStatus(DropPointStatus.EMPTY);
                    DropPoint saved = dropPointRepository.save(dropPoint);
                    collectionEventPublisher.publishCollectionStatusChanged(saved);
                    return saved;
                });
    }

    /**
     * Notify warehouse that drop point is full and ready for collection.
     * if the drop point is found, its status is full.if successfull, update status to notified.
     * via RabbitMQ, the warehouse will then process the drop point and update its status accordingly.
     */
    public Optional<DropPoint> notifyWarehouse(Long id) {
        return dropPointRepository.findById(id)
                .map(dropPoint -> {
                    if (dropPoint.getStatus() != DropPointStatus.FULL) {
                        throw new ValidationException("Drop point must be FULL before warehouse notification");
                    }

                    dropPoint.setStatus(DropPointStatus.NOTIFIED);
                    DropPoint saved = dropPointRepository.save(dropPoint);
                    collectionEventPublisher.publishCollectionStatusChanged(saved);
                    return saved;
                });
    }

    /**
     * Adds one empty to a drop point and marks it FULL at capacity.
     */
    public Optional<DropPoint> addEmpty(Long id) {
        return dropPointRepository.findById(id)
                .map(dropPoint -> {
                    int currentStock = getCurrentStock(dropPoint);
                    int capacity = getCapacity(dropPoint);

                    if (currentStock < capacity) {
                        int newStock = currentStock + 1;
                        dropPoint.setCurrent_empties_stock(newStock);
                        if (newStock >= capacity) {
                            dropPoint.setStatus(DropPointStatus.FULL);
                        }
                    } else {
                        dropPoint.setCurrent_empties_stock(capacity);
                        dropPoint.setStatus(DropPointStatus.FULL);
                    }

                    return dropPointRepository.save(dropPoint);
                });
    }

    /**
     * Warehouse acknowledged the collection request.
     */
    public Optional<DropPoint> markAcceptedByWarehouse(Long id) {
        return dropPointRepository.findById(id)
                .map(dropPoint -> {
                    dropPoint.setStatus(DropPointStatus.ACCEPTED);
                    return dropPointRepository.save(dropPoint);
                });
    }

    /**
     * Warehouse collected empties; drop point returns to EMPTY.
     */
    public Optional<DropPoint> markCollectedByWarehouse(Long id) {
        return dropPointRepository.findById(id)
                .map(dropPoint -> {
                    dropPoint.setCurrent_empties_stock(0);
                    dropPoint.setStatus(DropPointStatus.EMPTY);
                    return dropPointRepository.save(dropPoint);
                });
    }

    public void resetForEventCompletion(Long id) {
        dropPointRepository.findById(id).ifPresent(dropPoint -> {
            dropPoint.setCurrent_empties_stock(0);
            dropPoint.setStatus(DropPointStatus.EMPTY);
            dropPointRepository.save(dropPoint);
        });
    }

    /**
     * Create or update a drop point from bootstrap event data.
     */
    public DropPoint upsertFromBootstrapEvent(Long id, String location, Integer capacity) {
        int resolvedCapacity = (capacity != null && capacity > 0) ? capacity : DEFAULT_CAPACITY;

        return dropPointRepository.findById(id)
                .map(existing -> updateExistingDropPoint(existing, location, resolvedCapacity))
                .orElseGet(() -> createNewDropPoint(id, location, resolvedCapacity));
    }

    private void updateDropPointFields(DropPoint existing, DropPoint updated) {
        if (StringUtils.hasText(updated.getLocation())) {
            existing.setLocation(updated.getLocation());
        }
        if (updated.getCapacity() != null && updated.getCapacity() > 0) {
            existing.setCapacity(updated.getCapacity());
        }
        if (updated.getStatus() != null) {
            existing.setStatus(updated.getStatus());
        }
        if (updated.getCurrent_empties_stock() != null) {
            existing.setCurrent_empties_stock(updated.getCurrent_empties_stock());
        }
    }

    private int getCurrentStock(DropPoint dropPoint) {
        return dropPoint.getCurrent_empties_stock() != null ? dropPoint.getCurrent_empties_stock() : 0;
    }

    private int getCapacity(DropPoint dropPoint) {
        return dropPoint.getCapacity() != null && dropPoint.getCapacity() > 0 ? dropPoint.getCapacity() : DEFAULT_CAPACITY;
    }

    private DropPoint updateExistingDropPoint(DropPoint existing, String location, int capacity) {
        existing.setLocation(location);
        existing.setCapacity(capacity);
        existing.setCurrent_empties_stock(0);
        existing.setStatus(DropPointStatus.EMPTY);
        return dropPointRepository.save(existing);
    }

    private DropPoint createNewDropPoint(Long id, String location, int capacity) {
        DropPoint dropPoint = new DropPoint();
        dropPoint.setId(id);
        dropPoint.setLocation(location);
        dropPoint.setCapacity(capacity);
        dropPoint.setCurrent_empties_stock(0);
        dropPoint.setStatus(DropPointStatus.EMPTY);
        return dropPointRepository.save(dropPoint);
    }
}
