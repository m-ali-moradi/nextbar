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
 * Represents a usage log entry for a bar's stock item.
 * This class captures the details of stock usage, including the bar ID, product ID, quantity used, and the timestamp of usage per drink.
 */
public class UsageLog {
    @Id
    @GeneratedValue
    private UUID id;
    private UUID barId;
    private String productName;
    private int quantity;
    private LocalDateTime timestamp;
}
