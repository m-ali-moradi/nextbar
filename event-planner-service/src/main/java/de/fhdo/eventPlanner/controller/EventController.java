package de.fhdo.eventPlanner.controller;

import de.fhdo.eventPlanner.dto.request.CreateEventRequest;
import de.fhdo.eventPlanner.dto.request.UpdateEventRequest;
import de.fhdo.eventPlanner.dto.response.EventDetailResponse;
import de.fhdo.eventPlanner.dto.response.EventResponse;
import de.fhdo.eventPlanner.model.EventStatus;
import de.fhdo.eventPlanner.service.EventService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for Event management.
 * Provides endpoints for CRUD operations and event lifecycle management.
 */
@RestController
@RequestMapping("/api/events")
@Tag(name = "Event Management", description = "Endpoints for managing events and their lifecycle")
public class EventController {

    private final EventService eventService;

    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    // ==================== GET Endpoints ====================

    /**
     * Get all events.
     */
    @GetMapping
    @Operation(summary = "Get all events", description = "Retrieves a list of all events")
    public ResponseEntity<List<EventResponse>> getAllEvents() {
        return ResponseEntity.ok(eventService.getAllEvents());
    }

    /**
     * Get events by status.
     */
    @GetMapping("/status/{status}")
    @Operation(summary = "Get events by status", description = "Retrieves events filtered by status")
    public ResponseEntity<List<EventResponse>> getEventsByStatus(
            @PathVariable @Parameter(description = "Event status") EventStatus status) {
        return ResponseEntity.ok(eventService.getEventsByStatus(status));
    }

    /**
     * Get event by ID (summary).
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get event by ID", description = "Retrieves an event by its ID")
    public ResponseEntity<EventResponse> getEventById(
            @PathVariable @Parameter(description = "Event ID") Long id) {
        return ResponseEntity.ok(eventService.getEventById(id));
    }

    /**
     * Get event by ID with full details.
     */
    @GetMapping("/{id}/details")
    @Operation(summary = "Get event details", description = "Retrieves full event details including bars and drop points")
    public ResponseEntity<EventDetailResponse> getEventDetails(
            @PathVariable @Parameter(description = "Event ID") Long id) {
        return ResponseEntity.ok(eventService.getEventDetailById(id));
    }

    // ==================== POST Endpoints ====================

    /**
     * Create a new event.
     */
    @PostMapping
    @Operation(summary = "Create event", description = "Creates a new event")
    public ResponseEntity<EventResponse> createEvent(
            @Valid @RequestBody CreateEventRequest request) {
        EventResponse response = eventService.createEvent(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // ==================== PUT Endpoints ====================

    /**
     * Update an existing event.
     */
    @PutMapping("/{id}")
    @Operation(summary = "Update event", description = "Updates an existing event")
    public ResponseEntity<EventResponse> updateEvent(
            @PathVariable @Parameter(description = "Event ID") Long id,
            @Valid @RequestBody UpdateEventRequest request) {
        return ResponseEntity.ok(eventService.updateEvent(id, request));
    }

    // ==================== DELETE Endpoints ====================

    /**
     * Delete an event.
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete event", description = "Deletes an event")
    public ResponseEntity<Void> deleteEvent(
            @PathVariable @Parameter(description = "Event ID") Long id) {
        eventService.deleteEvent(id);
        return ResponseEntity.noContent().build();
    }

    // ==================== Lifecycle Endpoints ====================

    /**
     * Start an event.
     * Updates status to RUNNING and activates all bars and drop points.
     */
    @PostMapping("/{id}/start")
    @Operation(summary = "Start event", description = "Starts an event, updating status to RUNNING and activating all bars and drop points")
    public ResponseEntity<EventDetailResponse> startEvent(
            @PathVariable @Parameter(description = "Event ID") Long id) {
        return ResponseEntity.ok(eventService.startEvent(id));
    }

    /**
     * Complete an event.
     */
    @PostMapping("/{id}/complete")
    @Operation(summary = "Complete event", description = "Marks an event as completed")
    public ResponseEntity<EventResponse> completeEvent(
            @PathVariable @Parameter(description = "Event ID") Long id) {
        return ResponseEntity.ok(eventService.completeEvent(id));
    }

    /**
     * Cancel an event.
     */
    @PostMapping("/{id}/cancel")
    @Operation(summary = "Cancel event", description = "Cancels an event")
    public ResponseEntity<EventResponse> cancelEvent(
            @PathVariable @Parameter(description = "Event ID") Long id) {
        return ResponseEntity.ok(eventService.cancelEvent(id));
    }
}
