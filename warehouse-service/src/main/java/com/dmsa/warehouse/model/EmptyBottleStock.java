package com.dmsa.warehouse.model;

import jakarta.persistence.*;

@Entity
@Table(name = "empty_bottle_stock")
public class EmptyBottleStock {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "drop_point_id", nullable = false)
    private Long dropPointId;

    @Column(name = "drop_point_location")
    private String dropPointLocation;

    @Column(nullable = false)
    private Integer quantity;

    public Long getId() {
        return id;
    }

    public Long getDropPointId() {
        return dropPointId;
    }

    public void setDropPointId(Long dropPointId) {
        this.dropPointId = dropPointId;
    }

    public String getDropPointLocation() {
        return dropPointLocation;
    }

    public void setDropPointLocation(String dropPointLocation) {
        this.dropPointLocation = dropPointLocation;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
}