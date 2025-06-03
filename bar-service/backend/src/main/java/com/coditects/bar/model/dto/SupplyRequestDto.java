package com.coditects.bar.model.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.coditects.bar.model.SupplyStatus;

/**
 * Data Transfer Object (DTO) for Supply Request.
 * Represents the details of a supply request including its ID, bar ID, items requested, status, and creation timestamp.
 */
public record SupplyRequestDto(
        UUID id,
        UUID barId,
        List<SupplyItemDto> items,
        SupplyStatus status,
        LocalDateTime createdAt
) {
}
