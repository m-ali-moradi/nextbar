package com.nextbar.bar.model.dto;

import java.util.UUID;

/**
 * Data Transfer Object (DTO) for Product entity.
 * Represents the details of a product including its ID, name, and unit type.
 */
public record ProductDto(UUID id, String name, String unitType) {
}
