package com.nextbar.eventPlanner.event;

import java.time.Instant;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Event representing the synchronization of staff resource assignments for an event.
 * This event is published whenever staff resources are assigned or cleared for a specific event.
 * It contains details about the event, the action performed (assign or clear), and the list of staff resource assignments.
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StaffResourceSyncEvent {

    private Long eventId;
    private String eventName;
    private String action;
    private List<StaffResourceAssignment> assignments;
    private Instant timestamp;

    public static StaffResourceSyncEvent assign(Long eventId, String eventName,
            List<StaffResourceAssignment> assignments) {
        return StaffResourceSyncEvent.builder()
                .eventId(eventId)
                .eventName(eventName)
                .action("ASSIGN")
                .assignments(assignments)
                .timestamp(Instant.now())
                .build();
    }

    public static StaffResourceSyncEvent clear(Long eventId, String eventName,
            List<StaffResourceAssignment> assignments) {
        return StaffResourceSyncEvent.builder()
                .eventId(eventId)
                .eventName(eventName)
                .action("CLEAR")
                .assignments(assignments)
                .timestamp(Instant.now())
                .build();
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class StaffResourceAssignment {
        private String staffId;
        private String serviceCode;
        private Long resourceId;
    }
}
