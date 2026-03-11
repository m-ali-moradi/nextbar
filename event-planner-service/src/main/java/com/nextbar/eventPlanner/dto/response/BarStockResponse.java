package de.fhdo.eventPlanner.dto.response;

import de.fhdo.eventPlanner.model.BarStock;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Response DTO for BarStock entity.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BarStockResponse {

    private Long id;
    private Long barId;
    private String barName;
    private String itemName;
    private Integer quantity;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Factory method to create response from entity.
     */
    public static BarStockResponse fromEntity(BarStock entity) {
        return BarStockResponse.builder()
                .id(entity.getId())
                .barId(entity.getBar() != null ? entity.getBar().getId() : null)
                .barName(entity.getBar() != null ? entity.getBar().getName() : null)
                .itemName(entity.getItemName())
                .quantity(entity.getQuantity())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
