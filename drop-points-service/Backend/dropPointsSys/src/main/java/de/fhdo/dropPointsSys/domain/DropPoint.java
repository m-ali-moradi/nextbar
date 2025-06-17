package de.fhdo.dropPointsSys.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "DROP_POINTS")
public class DropPoint {
    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "LOCATION")
    private String location;

    @Column(name = "CAPACITY")
    private Integer capacity;

    @Column(name = "CURRENT_EMPTIES_STOCK")
    private Integer current_empties_stock;

    @Column(name = "STATUS")
    @Enumerated(EnumType.STRING)
    private DropPointStatus status;

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

    public Integer getCurrent_empties_stock() {
        return current_empties_stock;
    }

    public void setCurrent_empties_stock(Integer current_empties_stock) {
        this.current_empties_stock = current_empties_stock;
    }

    public DropPointStatus getStatus() {
        return status;
    }

    public void setStatus(DropPointStatus status) {
        this.status = status;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }
}
