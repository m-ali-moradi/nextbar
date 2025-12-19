package de.fhdo.eventPlanner.dto;

import de.fhdo.eventPlanner.model.Status;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class EventForm {
    private Long eventId;

    @NotBlank(message = "Name is required")
    private String name;

    @NotNull(message = "Date is required")
    private LocalDate date;

    @NotBlank(message = "Location is required")
    private String location;

    @NotNull(message = "Duration is required")
    @Min(value = 1, message = "Duration must be at least 1 hour")
    private Integer duration;

    @NotNull(message = "Status is required")
    private Status status;

    // ────────────────────────────────────
    // Replace List<DefineBeverage> beverages with:
    @NotEmpty(message = "You must select at least one beverage")
    private List<Long> beverageIds = new ArrayList<>();

    public List<Long> getBeverageIds() {
        return beverageIds;
    }
    public void setBeverageIds(List<Long> beverageIds) {
        this.beverageIds = beverageIds;
    }
    // ────────────────────────────────────

    @Valid
    @Size(min = 1, message = "At least one bar is required")
    private List<BarPlanForm> bars = new ArrayList<>();

    @Valid
    @Size(min = 1, message = "At least one drop point is required")
    private List<DropPointForm> dropPoints = new ArrayList<>();

    public EventForm() {
        bars.add(new BarPlanForm());
        dropPoints.add(new DropPointForm());
    }

    // … other getters/setters for eventId, name, date, location, duration, status, bars, dropPoints …

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

    public List<BarPlanForm> getBars() {
        return bars;
    }
    public void setBars(List<BarPlanForm> bars) {
        this.bars = bars;
    }

    public List<DropPointForm> getDropPoints() {
        return dropPoints;
    }
    public void setDropPoints(List<DropPointForm> dropPoints) {
        this.dropPoints = dropPoints;
    }
}
