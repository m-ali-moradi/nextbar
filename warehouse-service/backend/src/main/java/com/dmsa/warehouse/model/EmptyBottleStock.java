package com.dmsa.warehouse.model;


import jakarta.persistence.*;

@Entity
@Table(name = "empty_bottle_stock")
public class EmptyBottleStock {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "quantity")
    private int quantity;

    public EmptyBottleStock() {}
    public EmptyBottleStock(int quantity) {
        this.quantity = quantity;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

}
