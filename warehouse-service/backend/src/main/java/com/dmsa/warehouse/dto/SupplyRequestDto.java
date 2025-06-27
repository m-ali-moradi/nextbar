package com.dmsa.warehouse.dto;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public class SupplyRequestDto {
    private UUID id;
    private UUID barId;
    private List<SupplyItemDto> items;
    private String status;
    private OffsetDateTime createdAt;

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

    public void setBarId(UUID b) {
        this.barId = b;
    }

    public List<SupplyItemDto> getItems() {
        return items;
    }

    public void setItems(List<SupplyItemDto> i) {
        this.items = i;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String s) {
        this.status = s;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime t) {
        this.createdAt = t;
    }
}
