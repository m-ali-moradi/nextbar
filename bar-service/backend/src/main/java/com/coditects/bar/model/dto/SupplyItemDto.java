package com.coditects.bar.model.dto;

import java.util.UUID;


/**
 * Data Transfer Object (DTO) for Supply Item.
 * Represents the details of a supply item including its product ID, name, and quantity.
 */
public record SupplyItemDto(UUID productId, String productName, int quantity) {
}
