package com.nextbar.bar.service;

import java.util.List;
import java.util.UUID;

import com.nextbar.bar.model.dto.BarDto;


// This interface defines the operations for managing bars in the system.
public interface BarService {

    // Registers a new bar with the given name, location, and maximum capacity.
    BarDto registerBar(UUID id, String name, String location , int maxCapacity);

    // Retrieves a bar by its ID.
    BarDto getBar(UUID barId);

    // Retrieves a list of all bars in the system.
    List<BarDto> getAllBars();

}
