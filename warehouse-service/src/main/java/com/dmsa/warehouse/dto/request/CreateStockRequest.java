package com.dmsa.warehouse.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

/**
 * Request DTO for adding new beverage stock.
 */
public class CreateStockRequest {

    @NotBlank(message = "Beverage type is required")
    private String beverageType;

    @Min(value = 0, message = "Quantity must be non-negative")
    private int quantity;

    @Min(value = 0, message = "Minimum stock level must be non-negative")
    private Integer minStockLevel;

    public CreateStockRequest() {
    }

    public CreateStockRequest(String beverageType, int quantity) {
        this.beverageType = beverageType;
        this.quantity = quantity;
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

    public Integer getMinStockLevel() {
        return minStockLevel;
    }

    public void setMinStockLevel(Integer minStockLevel) {
        this.minStockLevel = minStockLevel;
    }
}
