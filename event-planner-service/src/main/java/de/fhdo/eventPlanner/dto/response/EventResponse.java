package de.fhdo.eventPlanner.dto.response;

import de.fhdo.eventPlanner.model.Event;
import de.fhdo.eventPlanner.model.EventStatus;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Response DTO for Event entity.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventResponse {

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
    private Integer barCount;
    private Integer dropPointCount;

    /**
     * Factory method to create response from entity (summary without nested
     * objects).
     */
    public static EventResponse fromEntity(Event entity) {
        return EventResponse.builder()
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
                .barCount(entity.getBars() != null ? entity.getBars().size() : 0)
                .dropPointCount(entity.getDropPoints() != null ? entity.getDropPoints().size() : 0)
                .build();
    }
}
