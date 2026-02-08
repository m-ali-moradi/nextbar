package de.fhdo.eventPlanner.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

/**
 * DTO for creating a new Bar assigned to an Event.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateBarRequest {

    @NotNull(message = "Event ID is required")
    private Long eventId;

    @NotBlank(message = "Bar name is required")
    @Size(min = 2, max = 100, message = "Bar name must be between 2 and 100 characters")
    private String name;

    @NotBlank(message = "Bar location is required")
    @Size(max = 200, message = "Location must not exceed 200 characters")
    private String location;

    @NotNull(message = "Capacity is required")
    @Min(value = 1, message = "Capacity must be at least 1")
    private Integer capacity;

    @NotBlank(message = "Staff assignment is required")
    @Size(max = 500, message = "Assigned staff must not exceed 500 characters")
    private String assignedStaff;
}
