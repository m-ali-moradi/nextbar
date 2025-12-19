package com.coditects.bar.model;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
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
 * This class represents a Bar entity.
 * It includes fields for the bar's ID, name, location, maximum capacity, and a list of stock items.
 */
public class Bar {
    @Id
    private UUID id;
    private String name;
    private String location;
    private int maxCapacity;

    // One Bar can have many stock items
    // Each stock item is associated with a specific product
    @OneToMany(mappedBy = "barId", cascade = CascadeType.ALL)
    private List<BarStockItem> stockItems = new ArrayList<>();


}
