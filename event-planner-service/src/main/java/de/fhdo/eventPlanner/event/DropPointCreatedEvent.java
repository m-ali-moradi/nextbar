package de.fhdo.eventPlanner.event;

import lombok.*;
import java.time.Instant;

/**
 * Event payload published when a DropPoint is created.
 * Published to the dropPointCreated-out-0 binding (fanout exchange).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DropPointCreatedEvent {

    private Long id;
    private String name;
    private String location;
    private Long eventId;
    private String eventName;
    private Instant timestamp;

    public static DropPointCreatedEvent from(Long id, String name, String location,
            Long eventId, String eventName) {
        return DropPointCreatedEvent.builder()
                .id(id)
                .name(name)
                .location(location)
                .eventId(eventId)
                .eventName(eventName)
                .timestamp(Instant.now())
                .build();
    }
}
