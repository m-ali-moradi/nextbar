package de.fhdo.eventPlanner.dto.response;

import de.fhdo.eventPlanner.model.Event;
import de.fhdo.eventPlanner.model.EventStatus;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Detailed response DTO for Event entity including nested bars and drop points.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventDetailResponse {

    private Long id;
    private String name;
    private LocalDate date;
    private String location;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String organizerName;
    private String organizerEmail;
    private String organizerPhone;
    private Integer attendeesCount;
    private Integer maxAttendees;
    private Boolean isPublic;
    private EventStatus status;
    private List<BarResponse> bars;
    private List<DropPointResponse> dropPoints;

    /**
     * Factory method to create detailed response from entity with nested objects.
     */
    public static EventDetailResponse fromEntity(Event entity) {
        return EventDetailResponse.builder()
                .id(entity.getId())
                .name(entity.getName())
                .date(entity.getDate())
                .location(entity.getLocation())
                .description(entity.getDescription())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .organizerName(entity.getOrganizerName())
                .organizerEmail(entity.getOrganizerEmail())
                .organizerPhone(entity.getOrganizerPhone())
                .attendeesCount(entity.getAttendeesCount())
                .maxAttendees(entity.getMaxAttendees())
                .isPublic(entity.getIsPublic())
                .status(entity.getStatus())
                .bars(entity.getBars() != null
                        ? entity.getBars().stream()
                                .map(BarResponse::fromEntity)
                                .collect(Collectors.toList())
                        : List.of())
                .dropPoints(entity.getDropPoints() != null
                        ? entity.getDropPoints().stream()
                                .map(DropPointResponse::fromEntity)
                                .collect(Collectors.toList())
                        : List.of())
                .build();
    }
}
