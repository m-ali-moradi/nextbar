package com.dmsa.warehouse.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

/**
 * Request DTO for fulfilling a supply request from a bar.
 */
public class SupplyFulfillmentRequest {

    @NotBlank(message = "Beverage type is required")
    private String beverageType;

    @Min(value = 1, message = "Quantity must be at least 1")
    private int quantity;

    @NotBlank(message = "Current status is required")
    private String currentStatus;

    public SupplyFulfillmentRequest() {
    }

    public SupplyFulfillmentRequest(String beverageType, int quantity, String currentStatus) {
        this.beverageType = beverageType;
        this.quantity = quantity;
        this.currentStatus = currentStatus;
    }

    public String getBeverageType() {
        return beverageType;
    }

    public void setBeverageType(String beverageType) {
        this.beverageType = beverageType;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getCurrentStatus() {
        return currentStatus;
    }

    public void setCurrentStatus(String currentStatus) {
        this.currentStatus = currentStatus;
    }
}
