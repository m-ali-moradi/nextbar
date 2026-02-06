package com.dmsa.warehouse.model.entity;

import jakarta.persistence.*;
import java.time.Instant;

/**
 * Represents beverage stock in the warehouse inventory.
 * Each record tracks a specific beverage type and its available quantity.
 */
@Entity
@Table(name = "beverage_stock", indexes = {
        @Index(name = "idx_beverage_type", columnList = "beverage_type")
})
public class BeverageStock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "beverage_type", nullable = false, unique = true)
    private String beverageType;

    @Column(name = "quantity", nullable = false)
    private Integer quantity = 0;

    @Column(name = "min_stock_level")
    private Integer minStockLevel = 10;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Version
    private Long version; // Optimistic locking for concurrent stock updates

    public BeverageStock() {
    }

    public BeverageStock(String beverageType, Integer quantity) {
        this.beverageType = beverageType;
        this.quantity = quantity != null ? quantity : 0;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }

    // --- Business Methods ---

    /**
     * Checks if there is sufficient stock available.
     * 
     * @param requestedQuantity the quantity needed
     * @return true if stock is sufficient
     */
    public boolean hasSufficientStock(int requestedQuantity) {
        return this.quantity >= requestedQuantity;
    }

    /**
     * Deducts quantity from stock. Throws if insufficient.
     * 
     * @param amount the amount to deduct
     * @throws IllegalStateException if insufficient stock
     */
    public void deductStock(int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Deduction amount must be positive");
        }
        if (this.quantity < amount) {
            throw new IllegalStateException(
                    String.format("Insufficient stock for %s: available=%d, requested=%d",
                            beverageType, quantity, amount));
        }
        this.quantity -= amount;
    }

    /**
     * Adds quantity to stock.
     * 
     * @param amount the amount to add
     */
    public void addStock(int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Addition amount must be positive");
        }
        this.quantity += amount;
    }

    /**
     * Checks if stock level is below minimum threshold.
     * 
     * @return true if restocking is recommended
     */
    public boolean isLowStock() {
        return this.quantity <= this.minStockLevel;
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
        this.quantity = quantity != null ? quantity : 0;
    }

    public Integer getMinStockLevel() {
        return minStockLevel;
    }

    public void setMinStockLevel(Integer minStockLevel) {
        this.minStockLevel = minStockLevel;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public Long getVersion() {
        return version;
    }
}
