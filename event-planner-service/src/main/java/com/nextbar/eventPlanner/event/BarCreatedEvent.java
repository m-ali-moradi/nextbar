package de.fhdo.eventPlanner.event;

import de.fhdo.eventPlanner.model.ResourceMode;
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
    private ResourceMode resourceMode;
    private String existingResourceId;
    private Instant timestamp;

    public static BarCreatedEvent from(Long id, String name, String location,
            Integer capacity, Long eventId, String eventName,
            ResourceMode resourceMode, String existingResourceId) {
        return BarCreatedEvent.builder()
                .id(id)
                .name(name)
                .location(location)
                .capacity(capacity)
                .eventId(eventId)
                .eventName(eventName)
                .resourceMode(resourceMode)
                .existingResourceId(existingResourceId)
                .timestamp(Instant.now())
                .build();
    }
}
