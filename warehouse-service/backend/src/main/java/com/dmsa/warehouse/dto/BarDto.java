// src/main/java/com/dmsa/warehouse/dto/BarDto.java
package com.dmsa.warehouse.dto;

import java.util.UUID;

public class BarDto {
    private UUID id;
    private String name;
    private String location;
    private int maxCapacity;

    public BarDto() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public int getMaxCapacity() { return maxCapacity; }
    public void setMaxCapacity(int maxCapacity) { this.maxCapacity = maxCapacity; }
}