package com.nextbar.bar.dto.request;

import java.util.List;

import com.nextbar.bar.dto.response.SupplyItemDto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

/**
 * DTO for creating a supply request.
 * Wraps the list of items for JSON body payload.
 */
public record CreateSupplyRequestDto(
        @NotEmpty(message = "At least one item is required") @Valid List<SupplyItemDto> items) {
}
