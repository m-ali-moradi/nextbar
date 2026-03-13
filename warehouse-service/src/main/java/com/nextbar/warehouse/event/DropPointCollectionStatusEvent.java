package com.nextbar.warehouse.event;

import java.time.Instant;

public class DropPointCollectionStatusEvent {

    private Long dropPointId;
    private String location;
    private Integer bottleCount;
    private String status;
    private Instant timestamp;

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

    public Integer getBottleCount() {
        return bottleCount;
    }

    public void setBottleCount(Integer bottleCount) {
        this.bottleCount = bottleCount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }
}
