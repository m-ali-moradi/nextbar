package de.fhdo.eventPlanner.dto.external;

import com.fasterxml.jackson.annotation.JsonProperty;
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
    private Integer reservedQuantity;
    private Integer usedQuantity;
    private Integer minStockLevel;
    private boolean lowStock;
    private Instant updatedAt;

    /**
     * Returns id as productId for frontend compatibility.
     */
    @JsonProperty("productId")
    public Long getProductId() {
        return id;
    }

    /**
     * Returns beverageType as productName for frontend compatibility.
     */
    @JsonProperty("productName")
    public String getProductName() {
        return beverageType;
    }

    /**
     * Returns quantity as availableQuantity for frontend compatibility.
     */
    @JsonProperty("availableQuantity")
    public Integer getAvailableQuantity() {
        return quantity;
    }
}
