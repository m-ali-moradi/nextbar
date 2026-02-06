package com.nextbar.events;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Event published when a bar creates a new supply request.
 * Consumed by warehouse-service to add to pending requests.
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

    /**
     * Represents an item in the supply request.
     */
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
