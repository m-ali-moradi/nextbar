package de.fhdo.eventPlanner.event;

import lombok.*;
import java.time.Instant;

/**
 * Event payload published when staff is assigned to a Bar or DropPoint.
 * Published to the staffAssigned-out-0 binding (topic exchange).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StaffAssignedEvent {

    public enum AssignmentType {
        BAR,
        DROP_POINT
    }

    private String staffId;
    private Long resourceId;
    private AssignmentType assignmentType;
    private String resourceName;
    private Long eventId;
    private String eventName;
    private String staffName;
    private Instant timestamp;

    public static StaffAssignedEvent forBar(String staffId, Long barId, String barName,
            Long eventId, String eventName, String staffName) {
        return StaffAssignedEvent.builder()
                .staffId(staffId)
                .resourceId(barId)
                .assignmentType(AssignmentType.BAR)
                .resourceName(barName)
                .eventId(eventId)
                .eventName(eventName)
                .staffName(staffName)
                .timestamp(Instant.now())
                .build();
    }

    public static StaffAssignedEvent forDropPoint(String staffId, Long dropPointId, String dropPointName,
            Long eventId, String eventName, String staffName) {
        return StaffAssignedEvent.builder()
                .staffId(staffId)
                .resourceId(dropPointId)
                .assignmentType(AssignmentType.DROP_POINT)
                .resourceName(dropPointName)
                .eventId(eventId)
                .eventName(eventName)
                .staffName(staffName)
                .timestamp(Instant.now())
                .build();
    }
}
