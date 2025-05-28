package com.coditects.bar.model.dto;


import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Data Transfer Object (DTO) for Bar Stock Item.
 * Represents the details of a stock item in a bar including its ID, bar ID, product ID, name, quantity, and last updated timestamp.
 */
public record BarStockItemDto(UUID id, UUID barId, UUID productId, String name, int quantity, LocalDateTime updatedAt) {

}
