package com.dmsa.warehouse.dto.response;

import com.dmsa.warehouse.model.entity.DropPointCollection;
import com.dmsa.warehouse.model.enums.CollectionStatus;

import java.time.Instant;

/**
 * Response DTO for drop point collection information.
 */
public class CollectionResponse {

    private Long id;
    private Long dropPointId;
    private String location;
    private Integer bottleCount;
    private CollectionStatus status;
    private Instant notifiedAt;
    private Instant acceptedAt;
    private Instant collectedAt;
    private String notes;

    public CollectionResponse() {
    }

    /**
     * Factory method to create response from entity.
     */
    public static CollectionResponse fromEntity(DropPointCollection entity) {
        CollectionResponse response = new CollectionResponse();
        response.setId(entity.getId());
        response.setDropPointId(entity.getDropPointId());
        response.setLocation(entity.getLocation());
        response.setBottleCount(entity.getBottleCount());
        response.setStatus(entity.getStatus());
        response.setNotifiedAt(entity.getNotifiedAt());
        response.setAcceptedAt(entity.getAcceptedAt());
        response.setCollectedAt(entity.getCollectedAt());
        response.setNotes(entity.getNotes());
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
        this.bottleCount = bottleCount;
    }

    public CollectionStatus getStatus() {
        return status;
    }

    public void setStatus(CollectionStatus status) {
        this.status = status;
    }

    public Instant getNotifiedAt() {
        return notifiedAt;
    }

    public void setNotifiedAt(Instant notifiedAt) {
        this.notifiedAt = notifiedAt;
    }

    public Instant getAcceptedAt() {
        return acceptedAt;
    }

    public void setAcceptedAt(Instant acceptedAt) {
        this.acceptedAt = acceptedAt;
    }

    public Instant getCollectedAt() {
        return collectedAt;
    }

    public void setCollectedAt(Instant collectedAt) {
        this.collectedAt = collectedAt;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
