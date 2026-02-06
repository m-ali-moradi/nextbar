package com.nextbar.bar.controller;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nextbar.bar.model.dto.BarDto;
import com.nextbar.bar.security.RbacService;
import com.nextbar.bar.service.BarService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * REST controller for managing bars.
 * Provides endpoints to create, view, and list bars.
 */
@RestController
@RequestMapping("/bars")
@RequiredArgsConstructor
public class BarController {

    private final BarService barService;
    private final RbacService rbacService;

    /**
     * Register a new bar.
     *
     * @param dto the bar details
     * @return the created bar with HTTP 201
     */
    @PostMapping
    public ResponseEntity<BarDto> registerBar(@Valid @RequestBody BarDto dto) {
        rbacService.requireEventManagerOrAdmin();
        BarDto created = barService.registerBar(
                dto.id() != null ? dto.id() : UUID.randomUUID(),
                dto.name(),
                dto.location(),
                dto.maxCapacity());
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
        rbacService.requireBarAccess(barId);
        return ResponseEntity.ok(barService.getBar(barId));
    }

    /**
     * Get all bars.
     *
     * @return list of all bars
     */
    @GetMapping
    public ResponseEntity<List<BarDto>> getAllBars() {
        if (rbacService.isAdmin() || rbacService.isBarManager() || rbacService.isWarehouseStaff()) {
            return ResponseEntity.ok(barService.getAllBars());
        }

        Set<UUID> barIds = rbacService.getOperatorBarIds();
        if (barIds.isEmpty()) {
            throw new AccessDeniedException("No bar assignments");
        }

        List<BarDto> bars = barIds.stream().map(barService::getBar).toList();
        return ResponseEntity.ok(bars);
    }
}
