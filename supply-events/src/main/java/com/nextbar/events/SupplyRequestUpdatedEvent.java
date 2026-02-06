package com.nextbar.events;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Event published when warehouse updates a supply request status.
 * Consumed by bar-service to update request status.
 */
public class SupplyRequestUpdatedEvent {

    private final UUID requestId;
    private final UUID barId;
    private final SupplyStatus status;
    private final String rejectionReason;
    private final int fulfilledQuantity;
    private final LocalDateTime updatedAt;

    @JsonCreator
    public SupplyRequestUpdatedEvent(
            @JsonProperty("requestId") UUID requestId,
            @JsonProperty("barId") UUID barId,
            @JsonProperty("status") SupplyStatus status,
            @JsonProperty("rejectionReason") String rejectionReason,
            @JsonProperty("fulfilledQuantity") int fulfilledQuantity,
            @JsonProperty("updatedAt") LocalDateTime updatedAt) {
        this.requestId = requestId;
        this.barId = barId;
        this.status = status;
        this.rejectionReason = rejectionReason;
        this.fulfilledQuantity = fulfilledQuantity;
        this.updatedAt = updatedAt;
    }

    public UUID getRequestId() {
        return requestId;
    }

    public UUID getBarId() {
        return barId;
    }

    public SupplyStatus getStatus() {
        return status;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public int getFulfilledQuantity() {
        return fulfilledQuantity;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
