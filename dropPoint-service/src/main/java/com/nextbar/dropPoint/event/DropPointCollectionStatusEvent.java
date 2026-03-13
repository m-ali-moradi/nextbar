package com.nextbar.dropPoint.event;

import java.time.Instant;

import com.nextbar.dropPoint.domain.DropPointStatus;

/**
 * DropPointCollectionStatusEvent represents the status of a drop point collection, including the number of bottles collected and the current status.
 */
public class DropPointCollectionStatusEvent {

    private Long dropPointId;
    private String location;
    private Integer bottleCount;
    private DropPointStatus status;
    private Instant timestamp;

    public static DropPointCollectionStatusEvent from(Long dropPointId, String location,
            Integer bottleCount, DropPointStatus status) {
        DropPointCollectionStatusEvent event = new DropPointCollectionStatusEvent();
        event.setDropPointId(dropPointId);
        event.setLocation(location);
        event.setBottleCount(bottleCount != null ? bottleCount : 0);
        event.setStatus(status);
        event.setTimestamp(Instant.now());
        return event;
    }

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

    public DropPointStatus getStatus() {
        return status;
    }

    public void setStatus(DropPointStatus status) {
        this.status = status;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }
}
