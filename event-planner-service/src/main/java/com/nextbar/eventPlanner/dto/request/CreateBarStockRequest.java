package de.fhdo.eventPlanner.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

/**
 * DTO for creating a new BarStock entry.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateBarStockRequest {

    private Long barId;

    @NotBlank(message = "Item name is required")
    @Size(min = 1, max = 100, message = "Item name must be between 1 and 100 characters")
    private String itemName;

    @NotNull(message = "Quantity is required")
    @Min(value = 0, message = "Quantity cannot be negative")
    private Integer quantity;
}
