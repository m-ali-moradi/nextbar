package com.dmsa.warehouse.model;

import jakarta.persistence.*;

@Entity
@Table(name = "beverage_stock")
public class BeverageStock {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "beverage_type")
    private String beverageType;

    @Column(name = "quantity")
    private int quantity;

    public BeverageStock() {}

    public BeverageStock(String beverageType, int quantity) {
        this.beverageType = beverageType;
        this.quantity = quantity;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getBeverageType() { return beverageType; }
    public void setBeverageType(String beverageType) { this.beverageType = beverageType; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
}