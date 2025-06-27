package com.dmsa.warehouse.dto;

import java.util.UUID;

public class SupplyItemDto {
    private UUID productId;
    private String productName;
    private int quantity;

    public SupplyItemDto() {
    }

    public UUID getProductId() {
        return productId;
    }

    public void setProductId(UUID id) {
        this.productId = id;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String n) {
        this.productName = n;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int q) {
        this.quantity = q;
    }
}
