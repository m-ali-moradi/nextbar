package com.nextbar.bar.dto.response;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

/**
 * Data Transfer Object (DTO) for Supply Item.
 * Represents the details of a supply item including product name and quantity.
 */
public record SupplyItemDto(
                @NotBlank(message = "Product name is required") String productName,

                @Min(value = 1, message = "Quantity must be at least 1") int quantity) {
}
