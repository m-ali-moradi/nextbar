package com.nextbar.eventPlanner.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

/**
 * Event payload published when an Event is started to bootstrap Drop Points service.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DropPointBootstrapEvent {

    private Long eventId;
    private String eventName;
    private Instant timestamp;
    private List<DropPointBootstrapItem> dropPoints;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DropPointBootstrapItem {
        private Long dropPointId;
        private String location;
        private Integer capacity;
    }

    public static DropPointBootstrapEvent from(Long eventId, String eventName,
            List<DropPointBootstrapItem> dropPoints) {
        return DropPointBootstrapEvent.builder()
                .eventId(eventId)
                .eventName(eventName)
                .dropPoints(dropPoints)
                .timestamp(Instant.now())
                .build();
    }
}