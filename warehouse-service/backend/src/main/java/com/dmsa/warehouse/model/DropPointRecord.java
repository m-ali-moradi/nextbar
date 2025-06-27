package com.dmsa.warehouse.model;

import jakarta.persistence.*;

@Entity
@Table(name = "drop_point_records")
public class DropPointRecord {

    @Id
    private Long id;

    private String location;
    private Integer currentEmpties;

    @Enumerated(EnumType.STRING)
    private Status status;

    public enum Status {
        NOTIFIED, ACCEPTED
    }

    public DropPointRecord() {
    }

    public DropPointRecord(Long id) {
        this.id = id;
        this.status = Status.NOTIFIED;
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

    public Integer getCurrentEmpties() {
        return currentEmpties;
    }

    public void setCurrentEmpties(Integer currentEmpties) {
        this.currentEmpties = currentEmpties;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }
}
