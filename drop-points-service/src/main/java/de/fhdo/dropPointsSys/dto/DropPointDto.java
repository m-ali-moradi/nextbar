package de.fhdo.dropPointsSys.dto;

import de.fhdo.dropPointsSys.domain.DropPointStatus;

public class DropPointDto {


    public Long id;
    public String location;
    public int capacity;
    public int current_empties;
    public DropPointStatus status;

    public DropPointDto(Long id, String location, int capacity, int current_empties, DropPointStatus status) {
        this.id = id;
        this.location = location;
        this.capacity = capacity;
        this.current_empties = current_empties;
        this.status = status;
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

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public int getCurrent_empties() {
        return current_empties;
    }

    public void setCurrent_empties(int current_empties) {
        this.current_empties = current_empties;
    }

    public DropPointStatus getStatus() {
        return status;
    }

    public void setStatus(DropPointStatus status) {
        this.status = status;
    }
}
