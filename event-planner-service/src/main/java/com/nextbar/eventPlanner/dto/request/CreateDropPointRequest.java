package de.fhdo.eventPlanner.dto.request;

import de.fhdo.eventPlanner.model.ResourceMode;
import jakarta.validation.constraints.*;
import lombok.*;

/**
 * DTO for creating a new DropPoint assigned to an Event.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateDropPointRequest {

    @NotNull(message = "Event ID is required")
    private Long eventId;

    @Size(min = 2, max = 100, message = "Drop point name must be between 2 and 100 characters")
    private String name;

    @Size(max = 200, message = "Location must not exceed 200 characters")
    private String location;

    @Size(max = 500, message = "Assigned staff must not exceed 500 characters")
    private String assignedStaff;

    @Min(value = 1, message = "Capacity must be at least 1")
    @Max(value = 100000, message = "Capacity must not exceed 100000")
    private Integer capacity;

    private ResourceMode resourceMode;

    @Size(max = 100, message = "Existing resource ID must not exceed 100 characters")
    private String existingResourceId;

    @AssertTrue(message = "For NEW mode drop point name and capacity are required; for EXISTING mode existingResourceId is required")
    public boolean isValidResourceModeCombination() {
        ResourceMode effectiveMode = resourceMode != null ? resourceMode : ResourceMode.NEW;

        if (effectiveMode == ResourceMode.EXISTING) {
            return existingResourceId != null && !existingResourceId.isBlank();
        }

        return name != null && !name.isBlank() && capacity != null;
    }
}
