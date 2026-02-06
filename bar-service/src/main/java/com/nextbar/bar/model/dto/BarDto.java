package com.nextbar.bar.model.dto;

import java.util.UUID;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

/**
 * Data Transfer Object (DTO) for Bar entity.
 * Represents the details of a bar including its ID, name, location, and maximum capacity.
 */
public record BarDto(
        UUID id,
        
        @NotBlank(message = "Bar name is required")
        String name,
        
        @NotBlank(message = "Bar location is required")
        String location,
        
        @Min(value = 1, message = "Max capacity must be at least 1")
        int maxCapacity
) {
}