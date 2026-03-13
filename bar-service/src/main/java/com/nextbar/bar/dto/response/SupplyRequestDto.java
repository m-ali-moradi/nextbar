package com.nextbar.bar.dto.response;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.nextbar.bar.model.SupplyStatus;

import org.springframework.lang.NonNull;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

/**
 * Data Transfer Object (DTO) for Supply Request.
 * Represents the details of a supply request including its ID, bar ID, items
 * requested, status, and creation timestamp.
 */
public record SupplyRequestDto(
                @NonNull UUID id,

                @NotNull(message = "Bar ID is required") @NonNull UUID barId,

                @NotEmpty(message = "Supply request must contain at least one item") @Valid List<SupplyItemDto> items,

                SupplyStatus status,
                String rejectionReason,
                LocalDateTime createdAt) {
}
