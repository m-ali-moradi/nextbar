package com.dmsa.warehouse.dto.response;

import java.time.Instant;

import com.dmsa.warehouse.model.entity.BeverageStock;

/**
 * Response DTO for beverage stock information.
 */
public class StockResponse {

    private Long id;
    private String beverageType;
    private Integer quantity;
    private Integer reservedQuantity;
    private Integer usedQuantity;
    private Integer minStockLevel;
    private boolean lowStock;
    private Instant updatedAt;

    public StockResponse() {
    }

    /**
     * Factory method to create response from entity.
     */
    public static StockResponse fromEntity(BeverageStock entity) {
        StockResponse response = new StockResponse();
        response.setId(entity.getId());
        response.setBeverageType(entity.getBeverageType());
        response.setQuantity(entity.getQuantity());
        response.setReservedQuantity(entity.getReservedQuantity());
        response.setUsedQuantity(entity.getUsedQuantity());
        response.setMinStockLevel(entity.getMinStockLevel());
        response.setLowStock(entity.isLowStock());
        response.setUpdatedAt(entity.getUpdatedAt());
        return response;
    }

    // --- Getters and Setters ---

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getBeverageType() {
        return beverageType;
    }

    public void setBeverageType(String beverageType) {
        this.beverageType = beverageType;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Integer getMinStockLevel() {
        return minStockLevel;
    }

    public void setMinStockLevel(Integer minStockLevel) {
        this.minStockLevel = minStockLevel;
    }

    public Integer getReservedQuantity() {
        return reservedQuantity;
    }

    public void setReservedQuantity(Integer reservedQuantity) {
        this.reservedQuantity = reservedQuantity;
    }

    public Integer getUsedQuantity() {
        return usedQuantity;
    }

    public void setUsedQuantity(Integer usedQuantity) {
        this.usedQuantity = usedQuantity;
    }

    public boolean isLowStock() {
        return lowStock;
    }

    public void setLowStock(boolean lowStock) {
        this.lowStock = lowStock;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
