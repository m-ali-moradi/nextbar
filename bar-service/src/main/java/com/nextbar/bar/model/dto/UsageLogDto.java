package com.nextbar.bar.model.dto;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Data Transfer Object (DTO) for Usage Log.
 * Represents the details of a usage log entry including its ID, bar ID, product ID, product name, quantity used, and timestamp.
 */
public record UsageLogDto(
        UUID id,
        UUID barId,
        UUID productId,
        String productName,
        int quantity,
        LocalDateTime timestamp
) {
    /**
     * Constructs a new UsageLogDto with the specified parameters.
     *
     * @param id          the unique identifier of the usage log entry
     * @param barId       the unique identifier of the bar where the usage occurred
     * @param productId   the unique identifier of the product used
     * @param productName the name of the product used
     * @param quantity    the quantity of the product used
     * @param timestamp   the timestamp when the usage occurred
     */
    public UsageLogDto(UUID id, UUID barId, UUID productId, String productName, int quantity, LocalDateTime timestamp) {
        this.id = id;
        this.barId = barId;
        this.productId = productId;
        this.productName = productName;
        this.quantity = quantity;
        this.timestamp = timestamp;
    }
}
