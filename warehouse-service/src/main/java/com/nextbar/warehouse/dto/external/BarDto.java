package com.nextbar.warehouse.dto.external;

import java.util.UUID;

/**
 * DTO for bar information from bar-service.
 */
public class BarDto {

    private UUID id;
    private String name;
    private String location;
    private int maxCapacity;

    public BarDto() {
    }

    public BarDto(UUID id, String name, String location, int maxCapacity) {
        this.id = id;
        this.name = name;
        this.location = location;
        this.maxCapacity = maxCapacity;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public int getMaxCapacity() {
        return maxCapacity;
    }

    public void setMaxCapacity(int maxCapacity) {
        this.maxCapacity = maxCapacity;
    }
}
