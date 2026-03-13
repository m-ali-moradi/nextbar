package com.nextbar.bar.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

/**
 * DTO for creating a new local bar.
 */
public record CreateLocalBarRequest(
        @NotBlank(message = "Bar name is required") String name,
        @NotBlank(message = "Bar location is required") String location,
        @Min(value = 1, message = "Max capacity must be at least 1") int maxCapacity) {
}
