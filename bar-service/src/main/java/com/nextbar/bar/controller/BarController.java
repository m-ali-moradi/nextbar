package com.nextbar.bar.controller;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nextbar.bar.dto.request.CreateLocalBarRequest;
import com.nextbar.bar.dto.response.BarDto;
import com.nextbar.bar.exception.ResourceNotFoundException;
import com.nextbar.bar.security.RbacService;
import com.nextbar.bar.service.BarService;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * REST controller for managing bars.
 * Provides endpoints to create, view, and list bars.
 */
@RestController
@RequestMapping("/api/v1/bars")
@RequiredArgsConstructor
@Tag(name = "Bars", description = "Bar management")
public class BarController {

    private final BarService barService;
    private final RbacService rbacService;

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
        if (rbacService.canListAllBars()) {
            return ResponseEntity.ok(barService.getAllBars());
        }

        Set<UUID> barIds = rbacService.requireStaffBarIdsForListing();
        if (barIds.isEmpty()) {
            return ResponseEntity.ok(List.of());
        }
        // For bar staff, only return bars they have access to
        List<BarDto> bars = barIds.stream()
                .map(barId -> {
                    try {
                        return barService.getBar(barId);
                    } catch (ResourceNotFoundException ex) {
                        return null;
                    }
                })
                .filter(java.util.Objects::nonNull)
                .toList();
        return ResponseEntity.ok(bars);
    }

    /**
     * Create a new local bar.
     *
     * @param request the request containing bar details
     * @return the created bar details
     */
    @PostMapping("/local")
    public ResponseEntity<BarDto> createLocalBar(@Valid @RequestBody CreateLocalBarRequest request) {
        rbacService.requireBarStaffOrAdmin();
        BarDto created = barService.createLocalBar(request.name(), request.location(), request.maxCapacity());
        return ResponseEntity.ok(created);
    }
}
