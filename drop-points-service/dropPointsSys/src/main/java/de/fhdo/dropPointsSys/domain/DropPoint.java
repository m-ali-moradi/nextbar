package de.fhdo.dropPointsSys.dto;

import jakarta.persistence.*;

@Entity
@Table(name = "drop_points")
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
    private Boolean status;


    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }
}
