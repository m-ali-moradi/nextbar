package com.nextbar.warehouse.dto.response;

import com.nextbar.warehouse.model.enums.SupplyRequestStatus;

import java.util.UUID;

/**
 * Response DTO for supply fulfillment operations.
 */
public class SupplyFulfillmentResponse {

    private UUID barId;
    private UUID requestId;
    private int quantityFulfilled;
    private int quantityRequested;
    private SupplyRequestStatus newStatus;
    private String message;
    private boolean fullyFulfilled;

    public SupplyFulfillmentResponse() {
    }

    public SupplyFulfillmentResponse(UUID barId, UUID requestId, int quantityFulfilled,
            int quantityRequested, SupplyRequestStatus newStatus,
            String message) {
        this.barId = barId;
        this.requestId = requestId;
        this.quantityFulfilled = quantityFulfilled;
        this.quantityRequested = quantityRequested;
        this.newStatus = newStatus;
        this.message = message;
        this.fullyFulfilled = quantityFulfilled >= quantityRequested;
    }

    // Builder pattern for easier construction
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private UUID barId;
        private UUID requestId;
        private int quantityFulfilled;
        private int quantityRequested;
        private SupplyRequestStatus newStatus;
        private String message;

        public Builder barId(UUID barId) {
            this.barId = barId;
            return this;
        }

        public Builder requestId(UUID requestId) {
            this.requestId = requestId;
            return this;
        }

        public Builder quantityFulfilled(int quantityFulfilled) {
            this.quantityFulfilled = quantityFulfilled;
            return this;
        }

        public Builder quantityRequested(int quantityRequested) {
            this.quantityRequested = quantityRequested;
            return this;
        }

        public Builder newStatus(SupplyRequestStatus newStatus) {
            this.newStatus = newStatus;
            return this;
        }

        public Builder message(String message) {
            this.message = message;
            return this;
        }

        public SupplyFulfillmentResponse build() {
            return new SupplyFulfillmentResponse(barId, requestId, quantityFulfilled,
                    quantityRequested, newStatus, message);
        }
    }

    // --- Getters and Setters ---

    public UUID getBarId() {
        return barId;
    }

    public void setBarId(UUID barId) {
        this.barId = barId;
    }

    public UUID getRequestId() {
        return requestId;
    }

    public void setRequestId(UUID requestId) {
        this.requestId = requestId;
    }

    public int getQuantityFulfilled() {
        return quantityFulfilled;
    }

    public void setQuantityFulfilled(int quantityFulfilled) {
        this.quantityFulfilled = quantityFulfilled;
    }

    public int getQuantityRequested() {
        return quantityRequested;
    }

    public void setQuantityRequested(int quantityRequested) {
        this.quantityRequested = quantityRequested;
    }

    public SupplyRequestStatus getNewStatus() {
        return newStatus;
    }

    public void setNewStatus(SupplyRequestStatus newStatus) {
        this.newStatus = newStatus;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isFullyFulfilled() {
        return fullyFulfilled;
    }

    public void setFullyFulfilled(boolean fullyFulfilled) {
        this.fullyFulfilled = fullyFulfilled;
    }
}
