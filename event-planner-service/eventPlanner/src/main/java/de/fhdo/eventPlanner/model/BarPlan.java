package de.fhdo.eventPlanner.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonBackReference;

@Entity
@Table(name = "bar_plans")
public class BarPlan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long barId;

    @NotBlank(message = "Bar name is required")
    private String barName;

    @NotBlank(message = "Bar location is required")
    private String location;

    @NotNull(message = "Total capacity is required")
    @Min(value = 1, message = "Capacity must be at least 1")
    private Integer totalCapacity;

    /**
     * We need to store “per drink stock.” We will map a beverage name → assigned quantity.
     * Internally we store this as a JSON via a @ElementCollection of a Map.
     */
    @ElementCollection
    @CollectionTable(name = "bar_beverage_stock",
            joinColumns = @JoinColumn(name = "bar_id"))
    @MapKeyColumn(name = "beverage_name")       // e.g. "Coke"
    @Column(name = "quantity")
    private Map<String, Integer> beverageStock = new HashMap<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id")
    @JsonBackReference
    private Event event;

    public BarPlan() { }

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

    public Event getEvent() {
        return event;
    }
    public void setEvent(Event event) {
        this.event = event;
    }

    /**
     * Convenience: sum up all assigned beverage quantities to validate ≤ totalCapacity
     */
    @Transient
    public int getTotalAssignedDrinkQuantity() {
        return beverageStock.values().stream().mapToInt(Integer::intValue).sum();
    }
}
