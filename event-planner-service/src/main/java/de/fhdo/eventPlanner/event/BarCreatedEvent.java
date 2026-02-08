package de.fhdo.eventPlanner.event;

import lombok.*;
import java.time.Instant;

/**
 * Event payload published when a Bar is created.
 * Published to the barCreated-out-0 binding (fanout exchange).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BarCreatedEvent {

    private Long id;
    private String name;
    private String location;
    private Integer capacity;
    private Long eventId;
    private String eventName;
    private Instant timestamp;

    public static BarCreatedEvent from(Long id, String name, String location,
            Integer capacity, Long eventId, String eventName) {
        return BarCreatedEvent.builder()
                .id(id)
                .name(name)
                .location(location)
                .capacity(capacity)
                .eventId(eventId)
                .eventName(eventName)
                .timestamp(Instant.now())
                .build();
    }
}
