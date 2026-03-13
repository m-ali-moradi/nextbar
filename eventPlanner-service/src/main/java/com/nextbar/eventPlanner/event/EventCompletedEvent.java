package com.nextbar.eventPlanner.event;

import java.time.Instant;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
/**
 * Event representing the completion of an event, containing details such as event ID, name, completion time, and associated drop point IDs.
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventCompletedEvent {

    private Long eventId;
    private String eventName;
    private Instant completedAt;
    private List<Long> dropPointIds;

    public static EventCompletedEvent from(Long eventId, String eventName) {
        return EventCompletedEvent.builder()
                .eventId(eventId)
                .eventName(eventName)
                .completedAt(Instant.now())
                .build();
    }

    public static EventCompletedEvent from(Long eventId, String eventName, List<Long> dropPointIds) {
        return EventCompletedEvent.builder()
                .eventId(eventId)
                .eventName(eventName)
                .completedAt(Instant.now())
                .dropPointIds(dropPointIds)
                .build();
    }
}
