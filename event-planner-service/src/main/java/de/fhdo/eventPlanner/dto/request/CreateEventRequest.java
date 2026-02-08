package de.fhdo.eventPlanner.dto.request;

import de.fhdo.eventPlanner.model.EventStatus;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;

/**
 * DTO for creating a new Event.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateEventRequest {

    @NotBlank(message = "Event name is required")
    @Size(min = 2, max = 100, message = "Event name must be between 2 and 100 characters")
    private String name;

    @NotNull(message = "Event date is required")
    @FutureOrPresent(message = "Event date must be today or in the future")
    private LocalDate date;

    @NotBlank(message = "Event location is required")
    @Size(max = 200, message = "Location must not exceed 200 characters")
    private String location;

    @Size(max = 2000, message = "Description must not exceed 2000 characters")
    private String description;

    @NotBlank(message = "Organizer name is required")
    @Size(max = 100, message = "Organizer name must not exceed 100 characters")
    private String organizerName;

    @NotBlank(message = "Organizer email is required")
    @Email(message = "Invalid email format")
    private String organizerEmail;

    @Pattern(regexp = "^\\+?[0-9\\-\\s]{7,20}$", message = "Invalid phone number format")
    private String organizerPhone;

    @Min(value = 0, message = "Attendees count cannot be negative")
    private Integer attendeesCount = 0;

    @Min(value = 1, message = "Max attendees must be at least 1")
    private Integer maxAttendees;

    private Boolean isPublic = true;
}
