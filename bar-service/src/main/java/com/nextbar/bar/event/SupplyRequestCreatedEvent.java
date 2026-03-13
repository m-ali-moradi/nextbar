package com.nextbar.bar.event;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Event published when a new supply request is created by a bar.
 * This event is sent to the warehouse-service to notify them of the new request.
 */
public class SupplyRequestCreatedEvent {

    private final UUID requestId;
    private final UUID barId;
    private final String barName;
    private final List<SupplyItem> items;
    private final LocalDateTime createdAt;

    @JsonCreator
    public SupplyRequestCreatedEvent(
            @JsonProperty("requestId") UUID requestId,
            @JsonProperty("barId") UUID barId,
            @JsonProperty("barName") String barName,
            @JsonProperty("items") List<SupplyItem> items,
            @JsonProperty("createdAt") LocalDateTime createdAt) {
        this.requestId = requestId;
        this.barId = barId;
        this.barName = barName;
        this.items = items;
        this.createdAt = createdAt;
    }

    public UUID getRequestId() {
        return requestId;
    }

    public UUID getBarId() {
        return barId;
    }

    public String getBarName() {
        return barName;
    }

    public List<SupplyItem> getItems() {
        return items;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public static class SupplyItem {
        private final UUID productId;
        private final String productName;
        private final int quantity;

        @JsonCreator
        public SupplyItem(
                @JsonProperty("productId") UUID productId,
                @JsonProperty("productName") String productName,
                @JsonProperty("quantity") int quantity) {
            this.productId = productId;
            this.productName = productName;
            this.quantity = quantity;
        }

        public UUID getProductId() {
            return productId;
        }

        public String getProductName() {
            return productName;
        }

        public int getQuantity() {
            return quantity;
        }
    }
}
