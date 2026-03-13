package com.nextbar.bar.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.lang.NonNull;

/**
 * Data Transfer Object (DTO) for Usage Log.
 */
public record UsageLogDto(
                @NonNull UUID id,
                @NonNull UUID barId,
                String productName,
                int quantity,
                LocalDateTime timestamp) {
}
