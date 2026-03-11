package de.fhdo.eventPlanner.dto.request;

import de.fhdo.eventPlanner.model.ResourceMode;
import jakarta.validation.constraints.*;
import lombok.*;
import java.util.List;

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

    @Size(min = 2, max = 100, message = "Bar name must be between 2 and 100 characters")
    private String name;

    @Size(max = 200, message = "Location must not exceed 200 characters")
    private String location;

    @Min(value = 1, message = "Capacity must be at least 1")
    private Integer capacity;

    @Size(max = 50, message = "Assigned staff list must not exceed 50 users")
    private List<@NotBlank(message = "Assigned staff user ID must not be blank") String> assignedStaff;

    private ResourceMode resourceMode;

    @Size(max = 100, message = "Existing resource ID must not exceed 100 characters")
    private String existingResourceId;

    @AssertTrue(message = "For NEW mode bar name is required; for EXISTING mode existingResourceId is required")
    public boolean isValidResourceModeCombination() {
        ResourceMode effectiveMode = resourceMode != null ? resourceMode : ResourceMode.NEW;

        if (effectiveMode == ResourceMode.EXISTING) {
            return existingResourceId != null && !existingResourceId.isBlank();
        }

        return name != null && !name.isBlank();
    }
}
