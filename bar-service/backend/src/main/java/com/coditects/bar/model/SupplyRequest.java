package com.coditects.bar.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
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
 * Represents a supply request.
 * This class contains details about the supply request including its ID, bar ID, status, creation timestamp, and a list of supply items.
 */
public class SupplyRequest {

    @Id
    private UUID id;

    private UUID barId;

   
    @Enumerated(EnumType.STRING)
    private SupplyStatus status;

    private LocalDateTime createdAt;

    @ElementCollection
    // list of supply items associated with the supply request.
    @CollectionTable(name = "supply_items", joinColumns = @JoinColumn(name = "request_id"))
    private List<SupplyItem> items = new ArrayList<>();


}

