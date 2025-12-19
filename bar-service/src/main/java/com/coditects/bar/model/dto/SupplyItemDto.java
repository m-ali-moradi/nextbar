package com.coditects.bar.model.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 * Data Transfer Object (DTO) for Supply Item.
 * Represents the details of a supply item including its product ID, name, and quantity.
 */
public record SupplyItemDto(
        @NotNull(message = "Product ID is required")
        UUID productId,
        
        @NotBlank(message = "Product name is required")
        String productName,
        
        @Min(value = 1, message = "Quantity must be at least 1")
        int quantity
) {
}
