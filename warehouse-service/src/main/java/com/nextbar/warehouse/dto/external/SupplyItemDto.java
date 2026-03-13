package com.nextbar.warehouse.dto.external;

import java.util.UUID;

/**
 * DTO for supply item within a supply request.
 */
public class SupplyItemDto {

    private UUID productId;
    private String productName;
    private int quantity;

    public SupplyItemDto() {
    }

    public SupplyItemDto(UUID productId, String productName, int quantity) {
        this.productId = productId;
        this.productName = productName;
        this.quantity = quantity;
    }

    public UUID getProductId() {
        return productId;
    }

    public void setProductId(UUID productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}
