package com.nextbar.bar.dto.response;

import java.util.UUID;

import org.springframework.lang.NonNull;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Data Transfer Object (DTO) for Bar Stock Item.
 * Represents the details of a stock item in a bar.
 */
public record BarStockItemDto(
                @NonNull UUID id,

                @NotNull(message = "Bar ID is required") UUID barId,

                @NotBlank(message = "Product name is required") String productName,

                @Min(value = 0, message = "Quantity cannot be negative") int quantity) {
}
