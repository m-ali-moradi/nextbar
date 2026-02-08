package de.fhdo.eventPlanner.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

/**
 * DTO for updating an existing Bar.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateBarRequest {

    @Size(min = 2, max = 100, message = "Bar name must be between 2 and 100 characters")
    private String name;

    @Size(max = 200, message = "Location must not exceed 200 characters")
    private String location;

    @Min(value = 1, message = "Capacity must be at least 1")
    private Integer capacity;

    @Size(max = 500, message = "Assigned staff must not exceed 500 characters")
    private String assignedStaff;
}
