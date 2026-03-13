package com.nextbar.bar.model;

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
    private String productName;
    private int quantity;

    /**
     * Constructs a SupplyItem with the specified product ID and quantity.
     *
     * @param productName the name of the product
     * @param quantity  the quantity of the product
     */
    public SupplyItem(String productName, int quantity) {
        this.productName = productName;
        this.quantity = quantity;
    }
}
