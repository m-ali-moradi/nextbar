package de.fhdo.eventPlanner.dto;

import jakarta.validation.constraints.*;
import java.util.HashMap;
import java.util.Map;

public class BarPlanForm {
    private Long barId;

    @NotBlank(message = "Bar name cannot be blank")
    private String barName;

    @NotBlank(message = "Location is required")
    private String location;

    @NotNull(message = "Total capacity is required")
    @Min(value = 1, message = "Capacity must be at least 1")
    private Integer totalCapacity;

    /**
     * Map<beverageName, quantity> from the selected beverages.
     * We ensure in the service layer that sum ≤ totalCapacity.
     */
    private Map<String, Integer> beverageStock = new HashMap<>();

    public BarPlanForm() { }

    /* ====== Getters & Setters ====== */
    public Long getBarId() {
        return barId;
    }
    public void setBarId(Long barId) {
        this.barId = barId;
    }

    public String getBarName() {
        return barName;
    }
    public void setBarName(String barName) {
        this.barName = barName;
    }

    public String getLocation() {
        return location;
    }
    public void setLocation(String location) {
        this.location = location;
    }

    public Integer getTotalCapacity() {
        return totalCapacity;
    }
    public void setTotalCapacity(Integer totalCapacity) {
        this.totalCapacity = totalCapacity;
    }

    public Map<String, Integer> getBeverageStock() {
        return beverageStock;
    }
    public void setBeverageStock(Map<String, Integer> beverageStock) {
        this.beverageStock = beverageStock;
    }
}
