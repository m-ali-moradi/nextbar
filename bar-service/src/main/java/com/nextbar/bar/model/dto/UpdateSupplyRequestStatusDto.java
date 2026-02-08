package com.nextbar.bar.model.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO for updating supply request status.
 * Used for JSON body payloads instead of query parameters.
 */
public record UpdateSupplyRequestStatusDto(
        @NotBlank(message = "Status is required") String status) {
}
