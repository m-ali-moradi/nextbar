package com.nextbar.bar.model.dto;

import java.util.UUID;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * DTO for stock add/reduce operations.
 * Used for JSON body payloads instead of query parameters.
 */
public record StockOperationDto(
        @NotNull(message = "Product ID is required") UUID productId,

        @Min(value = 1, message = "Quantity must be at least 1") int quantity) {
}
