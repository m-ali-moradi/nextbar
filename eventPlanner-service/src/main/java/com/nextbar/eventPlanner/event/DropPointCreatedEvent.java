package com.nextbar.eventPlanner.event;

import com.nextbar.eventPlanner.model.ResourceMode;
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
    private ResourceMode resourceMode;
    private String existingResourceId;
    private Instant timestamp;

    public static DropPointCreatedEvent from(Long id, String name, String location,
            Long eventId, String eventName,
            ResourceMode resourceMode, String existingResourceId) {
        return DropPointCreatedEvent.builder()
                .id(id)
                .name(name)
                .location(location)
                .eventId(eventId)
                .eventName(eventName)
                .resourceMode(resourceMode)
                .existingResourceId(existingResourceId)
                .timestamp(Instant.now())
                .build();
    }
}
