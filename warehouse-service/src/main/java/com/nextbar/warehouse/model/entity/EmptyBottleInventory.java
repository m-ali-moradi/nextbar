package com.nextbar.warehouse.model.entity;

import jakarta.persistence.*;
import java.time.Instant;

/**
 * Tracks total empty bottles collected from each drop point.
 * This is an aggregate view of all collections from a specific drop point.
 */
@Entity
@Table(name = "empty_bottle_inventory", indexes = {
        @Index(name = "idx_inv_drop_point_id", columnList = "drop_point_id", unique = true)
})
public class EmptyBottleInventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "drop_point_id", nullable = false, unique = true)
    private Long dropPointId;

    @Column(name = "drop_point_location")
    private String dropPointLocation;

    @Column(name = "total_bottles_collected", nullable = false)
    private Integer totalBottlesCollected = 0;

    @Column(name = "last_collection_at")
    private Instant lastCollectionAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public EmptyBottleInventory() {
    }

    public EmptyBottleInventory(Long dropPointId, String location) {
        this.dropPointId = dropPointId;
        this.dropPointLocation = location;
        this.totalBottlesCollected = 0;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }

    // --- Business Methods ---

    /**
     * Records a new collection of empty bottles.
     * 
     * @param count number of bottles collected
     */
    public void recordCollection(int count) {
        if (count <= 0) {
            throw new IllegalArgumentException("Collection count must be positive");
        }
        this.totalBottlesCollected += count;
        this.lastCollectionAt = Instant.now();
    }

    // --- Getters and Setters ---

    public Long getId() {
        return id;
    }

    public Long getDropPointId() {
        return dropPointId;
    }

    public void setDropPointId(Long dropPointId) {
        this.dropPointId = dropPointId;
    }

    public String getDropPointLocation() {
        return dropPointLocation;
    }

    public void setDropPointLocation(String dropPointLocation) {
        this.dropPointLocation = dropPointLocation;
    }

    public Integer getTotalBottlesCollected() {
        return totalBottlesCollected;
    }

    public Instant getLastCollectionAt() {
        return lastCollectionAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
