package com.nextbar.bar.model.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 * Data Transfer Object (DTO) for Supply Item.
 * Represents the details of a supply item including its product ID, name, and
 * quantity.
 * Note: productName is optional for creation (looked up from database) but
 * included in responses.
 */
public record SupplyItemDto(
                @NotNull(message = "Product ID is required") UUID productId,

                // productName is optional for input (looked up from DB), but included in
                // responses
                String productName,

                @Min(value = 1, message = "Quantity must be at least 1") int quantity) {
}
