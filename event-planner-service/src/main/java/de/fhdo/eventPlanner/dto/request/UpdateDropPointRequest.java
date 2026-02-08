package de.fhdo.eventPlanner.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

/**
 * DTO for updating an existing DropPoint.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateDropPointRequest {

    @Size(min = 2, max = 100, message = "Drop point name must be between 2 and 100 characters")
    private String name;

    @Size(max = 200, message = "Location must not exceed 200 characters")
    private String location;

    @Size(max = 500, message = "Assigned staff must not exceed 500 characters")
    private String assignedStaff;
}
