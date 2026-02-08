package de.fhdo.eventPlanner.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * Entity representing a Drop Point within an Event.
 * Drop points are locations where empty bottles can be collected.
 */
@Entity
@Table(name = "drop_points")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DropPoint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String location;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @Column(name = "event_occupancy", nullable = false)
    @Builder.Default
    private Boolean eventOccupancy = false;

    @Column(name = "assigned_staff")
    private String assignedStaff;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
