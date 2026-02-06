package com.dmsa.warehouse.dto.response;

import com.dmsa.warehouse.model.entity.EmptyBottleInventory;

import java.time.Instant;

/**
 * Response DTO for empty bottle inventory.
 */
public class EmptyBottleInventoryResponse {

    private Long id;
    private Long dropPointId;
    private String dropPointLocation;
    private Integer totalBottlesCollected;
    private Instant lastCollectionAt;

    public EmptyBottleInventoryResponse() {
    }

    /**
     * Factory method to create response from entity.
     */
    public static EmptyBottleInventoryResponse fromEntity(EmptyBottleInventory entity) {
        EmptyBottleInventoryResponse response = new EmptyBottleInventoryResponse();
        response.setId(entity.getId());
        response.setDropPointId(entity.getDropPointId());
        response.setDropPointLocation(entity.getDropPointLocation());
        response.setTotalBottlesCollected(entity.getTotalBottlesCollected());
        response.setLastCollectionAt(entity.getLastCollectionAt());
        return response;
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

    public String getDropPointLocation() {
        return dropPointLocation;
    }

    public void setDropPointLocation(String dropPointLocation) {
        this.dropPointLocation = dropPointLocation;
    }

    public Integer getTotalBottlesCollected() {
        return totalBottlesCollected;
    }

    public void setTotalBottlesCollected(Integer totalBottlesCollected) {
        this.totalBottlesCollected = totalBottlesCollected;
    }

    public Instant getLastCollectionAt() {
        return lastCollectionAt;
    }

    public void setLastCollectionAt(Instant lastCollectionAt) {
        this.lastCollectionAt = lastCollectionAt;
    }
}
