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
 * This class contains details about the stock item including its ID, bar ID, product ID, quantity, and the last updated timestamp.
 */
public class BarStockItem {
    @Id
    @GeneratedValue
    private UUID id;
    private UUID barId;
    private UUID productId;
    private int quantity;
    private LocalDateTime updatedAt;

    public BarStockItem(UUID id, UUID uuid, UUID uuid1, String name, int quantity, LocalDateTime localDateTime) {
    }
}
