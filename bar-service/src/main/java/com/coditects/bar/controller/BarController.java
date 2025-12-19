package com.coditects.bar.controller;

import com.coditects.bar.model.dto.BarDto;
import com.coditects.bar.service.BarService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for managing bars.
 * Provides endpoints to create, view, and list bars.
 */
@RestController
@RequestMapping("/bars")
@RequiredArgsConstructor
public class BarController {

    private final BarService barService;

    /**
     * Register a new bar.
     *
     * @param dto the bar details
     * @return the created bar with HTTP 201
     */
    @PostMapping
    public ResponseEntity<BarDto> registerBar(@Valid @RequestBody BarDto dto) {
        BarDto created = barService.registerBar(
                dto.id() != null ? dto.id() : UUID.randomUUID(),
                dto.name(),
                dto.location(),
                dto.maxCapacity()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Get a specific bar by its ID.
     *
     * @param barId the bar UUID
     * @return the bar details
     */
    @GetMapping("/{barId}")
    public ResponseEntity<BarDto> getBar(@PathVariable UUID barId) {
        return ResponseEntity.ok(barService.getBar(barId));
    }

    /**
     * Get all bars.
     *
     * @return list of all bars
     */
    @GetMapping
    public ResponseEntity<List<BarDto>> getAllBars() {
        return ResponseEntity.ok(barService.getAllBars());
    }
}
