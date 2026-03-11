package com.nextbar.eventPlanner.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity representing an Event in the event planning system.
 * An event can have multiple bars and drop points associated with it.
 */
@Entity
@Table(name = "events")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private LocalDate date;

    @Column(nullable = false)
    private String location;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "organizer_name", nullable = false)
    private String organizerName;

    @Column(name = "organizer_email", nullable = false)
    private String organizerEmail;

    @Column(name = "organizer_phone")
    private String organizerPhone;

    @Column(name = "attendees_count", nullable = false)
    @Builder.Default
    private Integer attendeesCount = 0;

    @Column(name = "max_attendees")
    private Integer maxAttendees;

    @Column(name = "is_public", nullable = false)
    @Builder.Default
    private Boolean isPublic = true;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private EventStatus status = EventStatus.SCHEDULED;

    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Bar> bars = new ArrayList<>();

    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<DropPoint> dropPoints = new ArrayList<>();

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
    public void addBar(Bar bar) {
        bars.add(bar);
        bar.setEvent(this);
    }

    public void removeBar(Bar bar) {
        bars.remove(bar);
        bar.setEvent(null);
    }

    public void addDropPoint(DropPoint dropPoint) {
        dropPoints.add(dropPoint);
        dropPoint.setEvent(this);
    }

    public void removeDropPoint(DropPoint dropPoint) {
        dropPoints.remove(dropPoint);
        dropPoint.setEvent(null);
    }
}
