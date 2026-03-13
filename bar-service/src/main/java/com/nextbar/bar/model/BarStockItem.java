package com.nextbar.bar.model;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
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
 * Represents a stock item in a bar's inventory.
 * This class contains details about the stock item including its ID, bar ID, product name, quantity, and the last updated timestamp.
 */
public class BarStockItem {
    @Id
    @GeneratedValue
    private UUID id;
    private UUID barId;
    private String productName;
    private int quantity;
    private LocalDateTime updatedAt;
}
