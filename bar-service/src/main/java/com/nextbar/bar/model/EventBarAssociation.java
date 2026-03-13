package com.nextbar.bar.model;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * Entity representing the association between an event and a bar.
 * This is used to track which bars are associated with which events.
 */
@Setter
@Getter
@Entity
@Table(name = "event_bar_association")
public class EventBarAssociation {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false)
    private Long eventId;

    @Column(nullable = false)
    private UUID barId;

    @Column(nullable = false)
    private String eventStatus;

    @Column(nullable = false)
    private String eventName;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

}
