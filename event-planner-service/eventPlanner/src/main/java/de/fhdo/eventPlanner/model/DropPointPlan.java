package de.fhdo.eventPlanner.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

@Entity
@Table(name = "drop_point_plans")
public class DropPointPlan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long dropPointId;

    @NotBlank(message = "Drop point location is required")
    private String location;

    @NotNull(message = "Capacity is required")
    @Min(value = 1, message = "Capacity must be at least 1")
    private Integer capacity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id")
    private Event event;

    public DropPointPlan() { }

    /* ====== Getters & Setters ====== */

    public Long getDropPointId() {
        return dropPointId;
    }
    public void setDropPointId(Long dropPointId) {
        this.dropPointId = dropPointId;
    }

    public String getLocation() {
        return location;
    }
    public void setLocation(String location) {
        this.location = location;
    }

    public Integer getCapacity() {
        return capacity;
    }
    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }

    public Event getEvent() {
        return event;
    }
    public void setEvent(Event event) {
        this.event = event;
    }
}
