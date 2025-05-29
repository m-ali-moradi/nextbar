package com.coditects.bar.controller.REST;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.coditects.bar.model.SupplyStatus;
import com.coditects.bar.model.dto.SupplyItemDto;
import com.coditects.bar.model.dto.SupplyRequestDto;
import com.coditects.bar.service.SupplyRequestService;

import lombok.RequiredArgsConstructor;

/**
 * REST controller for managing supply requests for a bar.
 * Provides endpoints to create, view, and update supply requests.
 */
@RestController

// Maps requests to "/api/bars/{barId}/supply" and allows cross-origin requests from "http://localhost:5173"
@RequestMapping("/bars/{barId}/supply")
@CrossOrigin(origins = "http://localhost:5173")
// RequiredArgsConstructor generates a constructor with required arguments for dependency injection
@RequiredArgsConstructor
public class SupplyRequestControllerREST {

    // Injects the SupplyRequestService to handle business logic for supply requests
    private final SupplyRequestService requestService;

    // Create a new supply request
    @PostMapping
    public ResponseEntity<SupplyRequestDto> createRequest(
            @PathVariable UUID barId,
            @RequestBody List<SupplyItemDto> items) {

    // Validate inputs
        if (barId == null || items == null) {
            return ResponseEntity.badRequest().build(); // Return 400 Bad Request if barId is null or items are empty
        }
        // Create and return the supply request
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(requestService.createRequest(barId, items));
    }

    // View all supply requests for a bar
    // This end point be shared with warehouse person to view all requests for a bar. endpoint for warehouse person :)
    @GetMapping
    public ResponseEntity<List<SupplyRequestDto>> getRequests(@PathVariable UUID barId) {
        // Validate barId
        if (barId == null) {
            return ResponseEntity.badRequest().build(); // Return 400 Bad Request if barId is null
        }
        // Returns a 200 OK response with the list of supply requests for the specified bar
        return ResponseEntity.ok(requestService.getRequestsByBar(barId));
    }

    // View single request
    // This end point be shared with warehouse person to view one requests. endpoint for warehouse people :)
    @GetMapping("/{requestId}")
    public ResponseEntity<SupplyRequestDto> getRequest(@PathVariable UUID requestId) {
        // Validate requestId
        if (requestId == null) {
            return ResponseEntity.badRequest().build(); // Return 400 Bad Request if requestId is null
        }
        // Fetch and return the supply request details
        return ResponseEntity.ok(requestService.getRequest(requestId));
    }

    // Update supply request status (used by Warehouse)
    @PutMapping("/{requestId}/status")
    public ResponseEntity<Void> updateStatus(@PathVariable UUID requestId,
                                             @RequestParam SupplyStatus status) {
    // Validate requestId and status
        if (requestId == null || status == null) {
            return ResponseEntity.badRequest().build(); // Return 400 Bad Request if requestId or status is null
        }
        // Update the request status
        requestService.updateRequestStatus(requestId, status);
        // Returns a 200 OK response after updating the status
        return ResponseEntity.ok().build();
    }
}
