package com.coditects.bar.model.dto;

import com.coditects.bar.model.SupplyStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Data Transfer Object (DTO) for Supply Request.
 * Represents the details of a supply request including its ID, bar ID, items requested, status, and creation timestamp.
 */
public record SupplyRequestDto(
        UUID id,
        
        @NotNull(message = "Bar ID is required")
        UUID barId,
        
        @NotEmpty(message = "Supply request must contain at least one item")
        @Valid
        List<SupplyItemDto> items,
        
        SupplyStatus status,
        LocalDateTime createdAt
) {
}
