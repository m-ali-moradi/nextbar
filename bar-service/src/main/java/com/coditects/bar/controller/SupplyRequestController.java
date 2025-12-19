package com.coditects.bar.controller;

import com.coditects.bar.model.SupplyStatus;
import com.coditects.bar.model.dto.SupplyItemDto;
import com.coditects.bar.model.dto.SupplyRequestDto;
import com.coditects.bar.service.SupplyRequestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for managing supply requests for a bar.
 * Provides endpoints to create, view, delete and update supply requests.
 */
@RestController
@RequestMapping("/bars/{barId}/supply")
@RequiredArgsConstructor
public class SupplyRequestController {

    private final SupplyRequestService requestService;

    /**
     * Create a new supply request.
     *
     * @param barId the bar UUID
     * @param items list of items to request
     * @return the created supply request with HTTP 201
     */
    @PostMapping
    public ResponseEntity<SupplyRequestDto> createRequest(
            @PathVariable UUID barId,
            @Valid @RequestBody List<SupplyItemDto> items) {
        
        SupplyRequestDto created = requestService.createRequest(barId, items);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * View all supply requests for a bar.
     * This endpoint can be shared with warehouse personnel to view all requests for a bar.
     *
     * @param barId the bar UUID
     * @return list of supply requests
     */
    @GetMapping
    public ResponseEntity<List<SupplyRequestDto>> getRequests(@PathVariable UUID barId) {
        return ResponseEntity.ok(requestService.getRequestsByBar(barId));
    }

    /**
     * View a single supply request.
     * This endpoint can be shared with warehouse personnel to view one request.
     *
     * @param requestId the request UUID
     * @return the supply request details
     */
    @GetMapping("/{requestId}")
    public ResponseEntity<SupplyRequestDto> getRequest(@PathVariable UUID requestId) {
        return ResponseEntity.ok(requestService.getRequest(requestId));
    }

    /**
     * Delete a supply request.
     *
     * @param requestId the request UUID
     * @return empty response with HTTP 204
     */
    @DeleteMapping("/{requestId}")
    public ResponseEntity<Void> deleteRequest(@PathVariable UUID requestId) {
        requestService.deleteRequest(requestId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Update supply request status (e.g., from PENDING to APPROVED).
     * This endpoint can be used by warehouse personnel.
     *
     * @param requestId the request UUID
     * @param status    the new status
     * @return the updated supply request
     */
    @PatchMapping("/{requestId}/status")
    public ResponseEntity<SupplyRequestDto> updateRequestStatus(
            @PathVariable UUID requestId,
            @RequestParam String status) {
        
        // Parse status from string to enum
        SupplyStatus supplyStatus = SupplyStatus.valueOf(status.toUpperCase());
        requestService.updateRequestStatus(requestId, null, supplyStatus);
        SupplyRequestDto updated = requestService.getRequest(requestId);
        return ResponseEntity.ok(updated);
    }
}
