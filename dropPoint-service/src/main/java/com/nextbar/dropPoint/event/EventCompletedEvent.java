package com.nextbar.dropPoint.event;

import java.time.Instant;
import java.util.List;

/**
 * EventCompletedEvent represents the event that is published when an event is completed.
 * It contains information about the completed event, including its ID, name, completion time,
 * and optionally the IDs of associated drop points.
 */

public class EventCompletedEvent {

    private Long eventId;
    private String eventName;
    private Instant completedAt;
    private List<Long> dropPointIds;

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

    public Instant getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(Instant completedAt) {
        this.completedAt = completedAt;
    }

    public List<Long> getDropPointIds() {
        return dropPointIds;
    }

    public void setDropPointIds(List<Long> dropPointIds) {
        this.dropPointIds = dropPointIds;
    }
}
