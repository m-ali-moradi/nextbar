package com.nextbar.bar.model.dto;

import java.util.Map;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
// Represents a Data Transfer Object (DTO) for a bars and stocks coming from the event planner service.
public class EventPlannerBarDto {
    private int barId;
    private String barName;
    private String location;
    private int totalCapacity;
    private Map<String, Integer> beverageStock;
}
