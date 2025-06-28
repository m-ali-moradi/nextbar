package de.fhdo.eventPlanner.model;

//import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.Embeddable;
import java.io.Serializable;

@Embeddable
public class DefineBeverage implements Serializable {
    private Long id;
    // Map the external JSON "beverageType" field to this property
    //@JsonProperty("beverageType")
    private String name;    // e.g. “Coke”, “Sprite”
    // Map the external JSON "quantity" field to this property
    //@JsonProperty("quantity")
    private Long price;   // e.g. 2.50 (per bottle)

    public DefineBeverage() { }

    public DefineBeverage(Long id, String name, Long price) {
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

    public Long getPrice() {
        return price;
    }

    public void setPrice(Long price) {
        this.price = price;
    }
}
