package com.coditects.bar.model;

import java.util.UUID;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Entity

/**
 * Represents a product in the system.
 * This class contains details about the product including its ID, name, and unit type.
 */
public class Product {
    @Id
    private UUID id;
    private String name;
    private String unitType;

    // Default unit type is "bottle" if not specified
    public Product(String name) {
        this.id = UUID.randomUUID();
        this.name = name;
        this.unitType = "bottle"; // Default unit type
    }

}
