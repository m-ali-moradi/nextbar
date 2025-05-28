package com.coditects.bar.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.coditects.bar.model.Bar;
import com.coditects.bar.model.dto.BarDto;
import com.coditects.bar.repository.BarRepository;
import com.coditects.bar.service.BarService;

import lombok.RequiredArgsConstructor;


/**
 * Implementation of the BarService interface.
 * This service provides methods to register, retrieve, and list bars.
 * It uses a BarRepository to interact with the database.
 */
@Service
@RequiredArgsConstructor
public class BarServiceImpl implements BarService {

    // injecting the BarRepository to interact with the database
    private final BarRepository barRepository;

    // registerBar method to create a new bar
    @Override
    public BarDto registerBar(String name, String location, int maxCapacity) {

        // Create a new Bar object and save it to the repository
        Bar bar = new Bar(UUID.randomUUID(), name, location, maxCapacity, new ArrayList<>());
        // Save the bar to the repository and convert it to a DTO
        return toDto(barRepository.save(bar));


    }

    // This method retrieves a bar by its ID and converts it to a DTO
    @Override
    public BarDto getBar(UUID barId) {
        return barRepository.findById(barId)
                .map(this::toDto)
                .orElseThrow(() -> new RuntimeException("Bar not found"));
    }

    // This method retrieves all bars from the repository and converts them to a list of DTOs
    @Override
    public List<BarDto> getAllBars() {
        return barRepository.findAll().stream().map(this::toDto).toList();
    }

    // Converts a Bar entity to a BarDto
    private BarDto toDto(Bar bar) {
        return new BarDto(bar.getId(), bar.getName(), bar.getLocation(), bar.getMaxCapacity());
    }
}
