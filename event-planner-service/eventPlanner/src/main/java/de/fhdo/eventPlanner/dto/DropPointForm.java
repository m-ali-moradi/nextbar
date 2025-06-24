package de.fhdo.eventPlanner.dto;

import jakarta.validation.constraints.*;

public class DropPointForm {
    private Long dropPointId;

    @NotBlank(message = "Location is required")
    private String location;

    @NotNull(message = "Capacity is required")
    @Min(value = 1, message = "Capacity must be at least 1")
    private Integer capacity;

    public DropPointForm() { }

    /* ====== Getters & Setters ====== */
    public Long getDropPointId() {
        return dropPointId;
    }
    public void setDropPointId(Long dropPointId) {
        this.dropPointId = dropPointId;
    }

    public String getLocation() {
        return location;
    }
    public void setLocation(String location) {
        this.location = location;
    }

    public Integer getCapacity() {
        return capacity;
    }
    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }
}
