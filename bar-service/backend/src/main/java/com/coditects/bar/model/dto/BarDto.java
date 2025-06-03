package com.coditects.bar.model.dto;

import java.util.UUID;

import com.coditects.bar.exception.ValidationException;

/**
 * Data Transfer Object (DTO) for Bar entity.
 * Represents the details of a bar including its ID, name, location, and maximum capacity.
 */
public record BarDto(UUID id, String name, String location, int maxCapacity) {
    
    /**
     * Constructor for BarDto that validates the provided details.
     *
     * @param id          Unique identifier for the bar.
     * @param name        Name of the bar.
     * @param location    Location of the bar.
     * @param maxCapacity Maximum capacity of the bar.
     * @throws ValidationException if any of the provided details are invalid.
     */
    public BarDto {
        if (id == null || name == null || location == null || maxCapacity <= 0) {
            throw new ValidationException("Invalid bar details provided");
        }
    }
}