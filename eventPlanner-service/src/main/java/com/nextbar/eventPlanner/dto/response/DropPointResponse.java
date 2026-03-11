package com.nextbar.eventPlanner.dto.response;

import com.nextbar.eventPlanner.model.DropPoint;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Response DTO for DropPoint entity.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DropPointResponse {

    private Long id;
    private String name;
    private String location;
    private Long eventId;
    private String eventName;
    private Boolean eventOccupancy;
    private String assignedStaff;
    private Integer capacity;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Factory method to create response from entity.
     */
    public static DropPointResponse fromEntity(DropPoint entity) {
        return DropPointResponse.builder()
                .id(entity.getId())
                .name(entity.getName())
                .location(entity.getLocation())
                .eventId(entity.getEvent() != null ? entity.getEvent().getId() : null)
                .eventName(entity.getEvent() != null ? entity.getEvent().getName() : null)
                .eventOccupancy(entity.getEventOccupancy())
                .assignedStaff(entity.getAssignedStaff())
                .capacity(entity.getCapacity())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
