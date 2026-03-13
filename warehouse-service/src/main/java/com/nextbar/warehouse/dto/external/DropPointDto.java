package com.nextbar.warehouse.dto.external;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO for drop point information from drop-point service.
 */
public class DropPointDto {

    private Long id;
    private String location;
    private Integer capacity;

    @JsonProperty("current_empties")
    private Integer currentEmpties;

    private String status;

    public DropPointDto() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public Integer getCurrentEmpties() {
        return currentEmpties;
    }

    public void setCurrentEmpties(Integer currentEmpties) {
        this.currentEmpties = currentEmpties;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * Check if this drop point is full and has notified the warehouse.
     */
    public boolean isNotifiedToWarehouse() {
        return "NOTIFIED".equals(status);
    }

    /**
     * Check if this drop point is empty.
     */
    public boolean isEmpty() {
        return "EMPTY".equals(status);
    }
}
