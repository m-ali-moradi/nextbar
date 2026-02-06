package com.nextbar.bar.model.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 * Data Transfer Object (DTO) for Bar Stock Item.
 * Represents the details of a stock item in a bar.
 */
public record BarStockItemDto(
        UUID id,
        
        @NotNull(message = "Bar ID is required")
        UUID barId,
        
        @NotNull(message = "Product ID is required")
        UUID productId,
        
        @NotBlank(message = "Product name is required")
        String productName,
        
        @Min(value = 0, message = "Quantity cannot be negative")
        int quantity
) {
}
