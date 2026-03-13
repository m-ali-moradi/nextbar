package com.nextbar.dropPoint.event;

import java.time.Instant;
import java.util.List;

/**
 * DropPointBootstrapEvent represents an event that is published when the drop-points-service
 * is bootstrapped with initial data. It contains information about the event and a list of
 * drop points that were initialized.
 */
public class DropPointBootstrapEvent {

    private Long eventId;
    private String eventName;
    private Instant timestamp;
    private List<DropPointBootstrapItem> dropPoints;

    public Long getEventId() {
        return eventId;
    }

    public void setEventId(Long eventId) {
        this.eventId = eventId;
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public List<DropPointBootstrapItem> getDropPoints() {
        return dropPoints;
    }

    public void setDropPoints(List<DropPointBootstrapItem> dropPoints) {
        this.dropPoints = dropPoints;
    }

    public static class DropPointBootstrapItem {
        private Long dropPointId;
        private String location;
        private Integer capacity;

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
}