package com.coditects.bar.model;

import java.util.UUID;

import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@Embeddable
/**
 * Represents a supply item (value object).
 * This class contains details about the supply item including its product ID and quantity.
 */
public class SupplyItem {
    private UUID productId;
    private int quantity;

    /**
     * Constructs a SupplyItem with the specified product ID and quantity.
     *
     * @param productId the ID of the product
     * @param quantity  the quantity of the product
     */
    public SupplyItem(UUID productId, int quantity) {
        this.productId = productId;
        this.quantity = quantity;
    }
}
