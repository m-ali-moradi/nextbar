package com.nextbar.eventPlanner.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity representing a Bar within an Event.
 * A bar serves beverages and can have multiple stock items.
 */
@Entity
@Table(name = "bars")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Bar {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column
    private String location;

    @Column
    private Integer capacity;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @Column(name = "event_occupancy", nullable = false)
    @Builder.Default
    private Boolean eventOccupancy = false;

    @Column(name = "assigned_staff")
    private String assignedStaff;

    @OneToMany(mappedBy = "bar", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<BarStock> stocks = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Helper methods
    public void addStock(BarStock stock) {
        stocks.add(stock);
        stock.setBar(this);
    }

    public void removeStock(BarStock stock) {
        stocks.remove(stock);
        stock.setBar(null);
    }
}
