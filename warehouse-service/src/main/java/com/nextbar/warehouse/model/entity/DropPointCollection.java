package com.dmsa.warehouse.model.entity;

import com.dmsa.warehouse.model.enums.CollectionStatus;
import jakarta.persistence.*;
import java.time.Instant;

/**
 * Represents a record of empty bottle collection from a drop point.
 * Tracks the collection workflow from notification to completion.
 */
@Entity
@Table(name = "drop_point_collection", indexes = {
        @Index(name = "idx_drop_point_id", columnList = "drop_point_id"),
        @Index(name = "idx_status", columnList = "status")
})
public class DropPointCollection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "drop_point_id", nullable = false)
    private Long dropPointId;

    @Column(name = "location")
    private String location;

    @Column(name = "bottle_count", nullable = false)
    private Integer bottleCount = 0;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private CollectionStatus status = CollectionStatus.PENDING;

    @Column(name = "notified_at", nullable = false, updatable = false)
    private Instant notifiedAt;

    @Column(name = "accepted_at")
    private Instant acceptedAt;

    @Column(name = "collected_at")
    private Instant collectedAt;

    @Column(name = "notes")
    private String notes;

    public DropPointCollection() {
    }

    public DropPointCollection(Long dropPointId, String location, Integer bottleCount) {
        this.dropPointId = dropPointId;
        this.location = location;
        this.bottleCount = bottleCount != null ? bottleCount : 0;
        this.status = CollectionStatus.PENDING;
    }

    @PrePersist
    protected void onCreate() {
        this.notifiedAt = Instant.now();
    }

    // --- Business Methods ---

    /**
     * Transitions the collection to ACCEPTED status.
     * 
     * @throws IllegalStateException if transition is not allowed
     */
    public void accept() {
        validateTransition(CollectionStatus.ACCEPTED);
        this.status = CollectionStatus.ACCEPTED;
        this.acceptedAt = Instant.now();
    }

    /**
     * Marks the collection as completed.
     * 
     * @throws IllegalStateException if transition is not allowed
     */
    public void markCollected() {
        validateTransition(CollectionStatus.COLLECTED);
        this.status = CollectionStatus.COLLECTED;
        this.collectedAt = Instant.now();
    }

    /**
     * Resets the collection status (e.g., if drop point was emptied externally).
     */
    public void reset() {
        validateTransition(CollectionStatus.RESET);
        this.status = CollectionStatus.RESET;
        this.bottleCount = 0;
    }

    private void validateTransition(CollectionStatus target) {
        if (!this.status.canTransitionTo(target)) {
            throw new IllegalStateException(
                    String.format("Cannot transition from %s to %s", this.status, target));
        }
    }

    /**
     * Checks if collection is still pending acceptance.
     */
    public boolean isPending() {
        return this.status == CollectionStatus.PENDING;
    }

    /**
     * Checks if collection has been accepted but not yet completed.
     */
    public boolean isInProgress() {
        return this.status == CollectionStatus.ACCEPTED;
    }

    // --- Getters and Setters ---

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getDropPointId() {
        return dropPointId;
    }

    public void setDropPointId(Long dropPointId) {
        this.dropPointId = dropPointId;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Integer getBottleCount() {
        return bottleCount;
    }

    public void setBottleCount(Integer bottleCount) {
        this.bottleCount = bottleCount != null ? bottleCount : 0;
    }

    public CollectionStatus getStatus() {
        return status;
    }

    public Instant getNotifiedAt() {
        return notifiedAt;
    }

    public Instant getAcceptedAt() {
        return acceptedAt;
    }

    public Instant getCollectedAt() {
        return collectedAt;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
