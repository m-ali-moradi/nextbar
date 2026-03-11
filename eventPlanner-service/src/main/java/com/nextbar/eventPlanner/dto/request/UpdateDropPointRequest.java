package com.nextbar.eventPlanner.dto.request;

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

    @Min(value = 1, message = "Capacity must be at least 1")
    @Max(value = 100000, message = "Capacity must not exceed 100000")
    private Integer capacity;
}
