package com.nextbar.bar.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.nextbar.bar.model.SupplyStatus;
import com.nextbar.bar.model.dto.SupplyItemDto;
import com.nextbar.bar.model.dto.SupplyRequestDto;
import com.nextbar.bar.security.RbacService;
import com.nextbar.bar.service.SupplyRequestService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * REST controller for managing supply requests for a bar.
 * Provides endpoints to create, view, delete and update supply requests.
 */
@RestController
@RequestMapping("/bars/{barId}/supply")
@RequiredArgsConstructor
public class SupplyRequestController {

    private final SupplyRequestService requestService;
    private final RbacService rbacService;

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
        rbacService.requireBarAccess(barId);
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
        rbacService.requireBarAccess(barId);
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
    public ResponseEntity<SupplyRequestDto> getRequest(@PathVariable UUID barId, @PathVariable UUID requestId) {
        rbacService.requireBarAccess(barId);
        return ResponseEntity.ok(requestService.getRequest(requestId));
    }

    /**
     * Delete a supply request.
     *
     * @param requestId the request UUID
     * @return empty response with HTTP 204
     */
    @DeleteMapping("/{requestId}")
    public ResponseEntity<Void> deleteRequest(@PathVariable UUID barId, @PathVariable UUID requestId) {
        rbacService.requireBarAccess(barId);
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
            @PathVariable UUID barId,
            @PathVariable UUID requestId,
            @RequestParam String status) {
        rbacService.requireBarAccess(barId);
        // Parse status from string to enum
        SupplyStatus supplyStatus = SupplyStatus.valueOf(status.toUpperCase());
        requestService.updateRequestStatus(requestId, null, supplyStatus);
        SupplyRequestDto updated = requestService.getRequest(requestId);
        return ResponseEntity.ok(updated);
    }
}
