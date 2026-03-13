package com.nextbar.bar.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

/**
 * DTO for stock add/reduce operations.
 * Used for JSON body payloads instead of query parameters.
 */
public record StockOperationDto(
        @NotBlank(message = "Product name is required") String productName,

        @Min(value = 1, message = "Quantity must be at least 1") int quantity) {
}
