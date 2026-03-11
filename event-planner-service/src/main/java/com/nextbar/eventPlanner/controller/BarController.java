package de.fhdo.eventPlanner.controller;

import de.fhdo.eventPlanner.dto.external.UserDto;
import de.fhdo.eventPlanner.dto.request.CreateBarRequest;
import de.fhdo.eventPlanner.dto.request.UpdateBarRequest;
import de.fhdo.eventPlanner.dto.response.BarResponse;
import de.fhdo.eventPlanner.service.BarService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for Bar management.
 * Provides endpoints for CRUD operations and staff assignment.
 */
@RestController
@RequestMapping("/api/v1/events/bars")
@Tag(name = "Bar Management", description = "Endpoints for managing bars within events")
public class BarController {

    private final BarService barService;

    public BarController(BarService barService) {
        this.barService = barService;
    }

    // ==================== GET Endpoints ====================

    /**
     * Get all bars.
     */
    @GetMapping
    @Operation(summary = "Get all bars", description = "Retrieves a list of all bars")
    public ResponseEntity<List<BarResponse>> getAllBars() {
        return ResponseEntity.ok(barService.getAllBars());
    }

    /**
     * Get bars by event ID.
     */
    @GetMapping("/event/{eventId}")
    @Operation(summary = "Get bars by event", description = "Retrieves all bars for a specific event")
    public ResponseEntity<List<BarResponse>> getBarsByEventId(
            @PathVariable @Parameter(description = "Event ID") Long eventId) {
        return ResponseEntity.ok(barService.getBarsByEventId(eventId));
    }

    /**
     * Get bar by ID.
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get bar by ID", description = "Retrieves a bar by its ID")
    public ResponseEntity<BarResponse> getBarById(
            @PathVariable @Parameter(description = "Bar ID") Long id) {
        return ResponseEntity.ok(barService.getBarById(id));
    }

    /**
     * Get available staff for assignment.
     */
    @GetMapping("/staff/available")
    @Operation(summary = "Get available staff", description = "Retrieves available staff from UserService")
    public ResponseEntity<List<UserDto>> getAvailableStaff() {
        return ResponseEntity.ok(barService.getAvailableStaff());
    }

    // ==================== POST Endpoints ====================

    /**
     * Create a new bar.
     */
    @PostMapping
    @Operation(summary = "Create bar", description = "Creates a new bar for an event. Staff assignment is required.")
    public ResponseEntity<BarResponse> createBar(
            @Valid @RequestBody CreateBarRequest request) {
        BarResponse response = barService.createBar(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // ==================== PUT Endpoints ====================

    /**
     * Update an existing bar.
     */
    @PutMapping("/{id}")
    @Operation(summary = "Update bar", description = "Updates an existing bar")
    public ResponseEntity<BarResponse> updateBar(
            @PathVariable @Parameter(description = "Bar ID") Long id,
            @Valid @RequestBody UpdateBarRequest request) {
        return ResponseEntity.ok(barService.updateBar(id, request));
    }

    /**
     * Assign staff to a bar.
     */
    @PutMapping("/{id}/staff")
    @Operation(summary = "Assign staff", description = "Assigns staff to a bar")
    public ResponseEntity<BarResponse> assignStaff(
            @PathVariable @Parameter(description = "Bar ID") Long id,
            @RequestParam @Parameter(description = "Staff IDs (comma-separated)") String staffIds) {
        return ResponseEntity.ok(barService.assignStaff(id, staffIds));
    }

    // ==================== DELETE Endpoints ====================

    /**
     * Delete a bar.
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete bar", description = "Deletes a bar")
    public ResponseEntity<Void> deleteBar(
            @PathVariable @Parameter(description = "Bar ID") Long id) {
        barService.deleteBar(id);
        return ResponseEntity.noContent().build();
    }
}
