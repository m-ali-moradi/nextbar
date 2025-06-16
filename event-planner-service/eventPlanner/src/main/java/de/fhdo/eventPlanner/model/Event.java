package de.fhdo.eventPlanner.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "events")
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long eventId;

    @NotBlank(message = "Event name is required")
    private String name;

    @NotNull(message = "Date is required")
    private LocalDate date;

    @NotBlank(message = "Location is required")
    private String location; // simple string for now

    @NotNull(message = "Duration is required")
    @Min(value = 1, message = "Duration must be at least 1 hour")
    private Integer duration; // in hours

    @Enumerated(EnumType.STRING)
    private Status status;

    /**
     * The subset of the “Warehouse catalog” that this event will carry.
     * We store it as an @ElementCollection of DefineBeverage.
     */
    @ElementCollection
    @CollectionTable(name = "event_beverages", joinColumns = @JoinColumn(name = "event_id"))
    private List<DefineBeverage> beverages = new ArrayList<>();

    /**
     * One‐to‐Many relationship: an Event has multiple BarPlans.
     * Cascade so that when you save Event, its BarPlans are saved too.
     */
    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BarPlan> bars = new ArrayList<>();

    /**
     * One‐to‐Many relationship: an Event has multiple DropPointPlans.
     */
    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DropPointPlan> dropPoints = new ArrayList<>();

    public Event() { }

    // Google “Lombok” if you want to reduce boilerplate. Otherwise, generate getters/setters.

    /* ====== Getters & Setters ====== */

    public Long getEventId() {
        return eventId;
    }
    public void setEventId(Long eventId) {
        this.eventId = eventId;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public LocalDate getDate() {
        return date;
    }
    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getLocation() {
        return location;
    }
    public void setLocation(String location) {
        this.location = location;
    }

    public Integer getDuration() {
        return duration;
    }
    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    public Status getStatus() {
        return status;
    }
    public void setStatus(Status status) {
        this.status = status;
    }

    public List<DefineBeverage> getBeverages() {
        return beverages;
    }
    public void setBeverages(List<DefineBeverage> beverages) {
        this.beverages = beverages;
    }

    public List<BarPlan> getBars() {
        return bars;
    }
    public void setBars(List<BarPlan> bars) {
        this.bars = bars;
    }

    public List<DropPointPlan> getDropPoints() {
        return dropPoints;
    }
    public void setDropPoints(List<DropPointPlan> dropPoints) {
        this.dropPoints = dropPoints;
    }

    /* ====== Helper methods to keep both sides of the relationship in sync ====== */
    public void addBar(BarPlan bar) {
        bars.add(bar);
        bar.setEvent(this);
    }
    public void removeBar(BarPlan bar) {
        bars.remove(bar);
        bar.setEvent(null);
    }
    public void addDropPoint(DropPointPlan dp) {
        dropPoints.add(dp);
        dp.setEvent(this);
    }
    public void removeDropPoint(DropPointPlan dp) {
        dropPoints.remove(dp);
        dp.setEvent(null);
    }
}
