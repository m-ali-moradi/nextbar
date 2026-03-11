package com.nextbar.eventPlanner.event;

import lombok.*;
import java.time.Instant;
import java.util.List;

/**
 * Event payload published when an Event is started.
 * Published to the eventStarted-out-0 binding (fanout exchange).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventStartedEvent {

    private Long eventId;
    private String eventName;
    private String eventLocation;
    private List<Long> activatedBarIds;
    private List<Long> activatedDropPointIds;
    private Instant timestamp;

    public static EventStartedEvent from(Long eventId, String eventName, String eventLocation,
            List<Long> barIds, List<Long> dropPointIds) {
        return EventStartedEvent.builder()
                .eventId(eventId)
                .eventName(eventName)
                .eventLocation(eventLocation)
                .activatedBarIds(barIds)
                .activatedDropPointIds(dropPointIds)
                .timestamp(Instant.now())
                .build();
    }
}
