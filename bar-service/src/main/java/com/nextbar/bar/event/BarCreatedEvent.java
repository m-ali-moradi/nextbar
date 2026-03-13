package com.nextbar.bar.event;

import java.time.Instant;
/**
 * Event representing the creation of a new bar from eventplanner-service.
 * This event is published by the eventplanner-service when a new bar is created as part of an event planning process.
*/
public class BarCreatedEvent {

    private Long id;
    private String name;
    private String location;
    private Integer capacity;
    private Long eventId;
    private String eventName;
    private String resourceMode;
    private String existingResourceId;
    private Instant timestamp;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
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

    public Integer getCapacity() {
        return capacity;
    }

    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }

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

    public String getResourceMode() {
        return resourceMode;
    }

    public void setResourceMode(String resourceMode) {
        this.resourceMode = resourceMode;
    }

    public String getExistingResourceId() {
        return existingResourceId;
    }

    public void setExistingResourceId(String existingResourceId) {
        this.existingResourceId = existingResourceId;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }
}
