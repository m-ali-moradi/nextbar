package com.dmsa.warehouse.dto;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;

public class DropPointDto implements Serializable{
    private Long id;
    private String location;
    private Integer capacity;
    @JsonProperty("current_empties")
    private Integer currentEmpties;
    private String status;

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
}
