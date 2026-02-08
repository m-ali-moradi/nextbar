package de.fhdo.eventPlanner.dto.external;

import lombok.*;
import java.time.Instant;

/**
 * DTO representing stock/inventory item from the warehouse-service.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WarehouseStockDto {

    private Long id;
    private String beverageType;
    private Integer quantity;
    private Integer minStockLevel;
    private boolean lowStock;
    private Instant updatedAt;
}
