package de.fhdo.eventPlanner.controller;

import de.fhdo.eventPlanner.dto.request.CreateDropPointRequest;
import de.fhdo.eventPlanner.dto.request.UpdateDropPointRequest;
import de.fhdo.eventPlanner.dto.response.DropPointResponse;
import de.fhdo.eventPlanner.service.DropPointService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for DropPoint management.
 * Provides endpoints for CRUD operations and staff assignment.
 */
@RestController
@RequestMapping("/api/v1/events/drop-points")
@Tag(name = "Drop Point Management", description = "Endpoints for managing drop points within events")
public class DropPointController {

    private final DropPointService dropPointService;

    public DropPointController(DropPointService dropPointService) {
        this.dropPointService = dropPointService;
    }

    // ==================== GET Endpoints ====================

    /**
     * Get all drop points.
     */
    @GetMapping
    @Operation(summary = "Get all drop points", description = "Retrieves a list of all drop points")
    public ResponseEntity<List<DropPointResponse>> getAllDropPoints() {
        return ResponseEntity.ok(dropPointService.getAllDropPoints());
    }

    /**
     * Get drop points by event ID.
     */
    @GetMapping("/event/{eventId}")
    @Operation(summary = "Get drop points by event", description = "Retrieves all drop points for a specific event")
    public ResponseEntity<List<DropPointResponse>> getDropPointsByEventId(
            @PathVariable @Parameter(description = "Event ID") Long eventId) {
        return ResponseEntity.ok(dropPointService.getDropPointsByEventId(eventId));
    }

    /**
     * Get drop point by ID.
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get drop point by ID", description = "Retrieves a drop point by its ID")
    public ResponseEntity<DropPointResponse> getDropPointById(
            @PathVariable @Parameter(description = "Drop Point ID") Long id) {
        return ResponseEntity.ok(dropPointService.getDropPointById(id));
    }

    // ==================== POST Endpoints ====================

    /**
     * Create a new drop point.
     */
    @PostMapping
    @Operation(summary = "Create drop point", description = "Creates a new drop point for an event")
    public ResponseEntity<DropPointResponse> createDropPoint(
            @Valid @RequestBody CreateDropPointRequest request) {
        DropPointResponse response = dropPointService.createDropPoint(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // ==================== PUT Endpoints ====================

    /**
     * Update an existing drop point.
     */
    @PutMapping("/{id}")
    @Operation(summary = "Update drop point", description = "Updates an existing drop point")
    public ResponseEntity<DropPointResponse> updateDropPoint(
            @PathVariable @Parameter(description = "Drop Point ID") Long id,
            @Valid @RequestBody UpdateDropPointRequest request) {
        return ResponseEntity.ok(dropPointService.updateDropPoint(id, request));
    }

    /**
     * Assign staff to a drop point.
     */
    @PutMapping("/{id}/staff")
    @Operation(summary = "Assign staff", description = "Assigns staff to a drop point")
    public ResponseEntity<DropPointResponse> assignStaff(
            @PathVariable @Parameter(description = "Drop Point ID") Long id,
            @RequestParam @Parameter(description = "Staff IDs (comma-separated)") String staffIds) {
        return ResponseEntity.ok(dropPointService.assignStaff(id, staffIds));
    }

    // ==================== DELETE Endpoints ====================

    /**
     * Delete a drop point.
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete drop point", description = "Deletes a drop point")
    public ResponseEntity<Void> deleteDropPoint(
            @PathVariable @Parameter(description = "Drop Point ID") Long id) {
        dropPointService.deleteDropPoint(id);
        return ResponseEntity.noContent().build();
    }
}
