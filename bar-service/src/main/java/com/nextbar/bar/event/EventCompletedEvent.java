package com.nextbar.bar.event;

import java.time.Instant;
/**
 * Event published when an event is completed. so that bar can be deassociated with the event and become available for new events.
 */
public class EventCompletedEvent {

    private Long eventId;
    private String eventName;
    private Instant completedAt;

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
}
