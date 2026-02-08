package de.fhdo.eventPlanner.dto.response;

import de.fhdo.eventPlanner.model.Bar;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Response DTO for Bar entity.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BarResponse {

    private Long id;
    private String name;
    private String location;
    private Integer capacity;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long eventId;
    private String eventName;
    private Boolean eventOccupancy;
    private String assignedStaff;
    private List<BarStockResponse> stocks;

    /**
     * Factory method to create response from entity.
     */
    public static BarResponse fromEntity(Bar entity) {
        return BarResponse.builder()
                .id(entity.getId())
                .name(entity.getName())
                .location(entity.getLocation())
                .capacity(entity.getCapacity())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .eventId(entity.getEvent() != null ? entity.getEvent().getId() : null)
                .eventName(entity.getEvent() != null ? entity.getEvent().getName() : null)
                .eventOccupancy(entity.getEventOccupancy())
                .assignedStaff(entity.getAssignedStaff())
                .stocks(entity.getStocks() != null
                        ? entity.getStocks().stream()
                                .map(BarStockResponse::fromEntity)
                                .collect(Collectors.toList())
                        : List.of())
                .build();
    }

    /**
     * Factory method for summary without stocks.
     */
    public static BarResponse fromEntitySummary(Bar entity) {
        return BarResponse.builder()
                .id(entity.getId())
                .name(entity.getName())
                .location(entity.getLocation())
                .capacity(entity.getCapacity())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .eventId(entity.getEvent() != null ? entity.getEvent().getId() : null)
                .eventName(entity.getEvent() != null ? entity.getEvent().getName() : null)
                .eventOccupancy(entity.getEventOccupancy())
                .assignedStaff(entity.getAssignedStaff())
                .stocks(List.of())
                .build();
    }
}
