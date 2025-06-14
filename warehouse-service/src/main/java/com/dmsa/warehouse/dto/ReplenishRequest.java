package com.dmsa.warehouse.dto;

public class ReplenishRequest {
    private String beverageType;
    private int quantity;

    public String getBeverageType() { return beverageType; }
    public void setBeverageType(String beverageType) { this.beverageType = beverageType; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
}
