package com.nextbar.warehouse.model.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import com.nextbar.warehouse.model.enums.SupplyRequestStatus;

/**
 * Entity representing a supply request received from bar-service.
 * This stores the supply requests locally in warehouse for processing.
 */
@Entity
@Table(name = "supply_requests")
public class SupplyRequest {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "bar_id", nullable = false)
    private UUID barId;

    @Column(name = "bar_name")
    private String barName;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private SupplyRequestStatus status;

    @Column(name = "rejection_reason")
    private String rejectionReason;

    @Column(name = "requested_quantity", nullable = false)
    private int requestedQuantity = 0;

    @Column(name = "fulfilled_quantity", nullable = false)
    private int fulfilledQuantity = 0;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "supplyRequest", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SupplyRequestItem> items = new ArrayList<>();

    // Default constructor
    public SupplyRequest() {
    }

    // Getters and Setters
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

    public SupplyRequestStatus getStatus() {
        return status;
    }

    public void setStatus(SupplyRequestStatus status) {
        this.status = status;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }

    public int getRequestedQuantity() {
        return requestedQuantity;
    }

    public void setRequestedQuantity(int requestedQuantity) {
        this.requestedQuantity = requestedQuantity;
    }

    public int getFulfilledQuantity() {
        return fulfilledQuantity;
    }

    public void setFulfilledQuantity(int fulfilledQuantity) {
        this.fulfilledQuantity = fulfilledQuantity;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public List<SupplyRequestItem> getItems() {
        return items;
    }

    public void setItems(List<SupplyRequestItem> items) {
        this.items = items;
    }

    public void addItem(SupplyRequestItem item) {
        items.add(item);
        item.setSupplyRequest(this);
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
