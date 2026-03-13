package com.nextbar.warehouse.dto.external;

import java.util.List;
import java.util.UUID;

/**
 * DTO for supply request from bar-service.
 */
public class SupplyRequestDto {

    private UUID id;
    private UUID barId;
    private String barName;
    private List<SupplyItemDto> items;
    private String status;
    private String rejectionReason;
    private String createdAt;

    public SupplyRequestDto() {
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getBarId() {
        return barId;
    }

    public void setBarId(UUID barId) {
        this.barId = barId;
    }

    public String getBarName() {
        return barName;
    }

    public void setBarName(String barName) {
        this.barName = barName;
    }

    public List<SupplyItemDto> getItems() {
        return items;
    }

    public void setItems(List<SupplyItemDto> items) {
        this.items = items;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}
