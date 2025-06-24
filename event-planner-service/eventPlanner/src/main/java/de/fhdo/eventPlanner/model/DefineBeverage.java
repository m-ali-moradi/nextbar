package de.fhdo.eventPlanner.model;

import jakarta.persistence.Embeddable;
import java.io.Serializable;

@Embeddable
public class DefineBeverage implements Serializable {
    private Long id;
    private String name;    // e.g. “Coke”, “Sprite”
    private Double price;   // e.g. 2.50 (per bottle)

    public DefineBeverage() { }

    public DefineBeverage(Long id, String name, Double price) {
        this.id = id;
        this.name = name;
        this.price = price;
    }

    // Getters & Setters

    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }
}
