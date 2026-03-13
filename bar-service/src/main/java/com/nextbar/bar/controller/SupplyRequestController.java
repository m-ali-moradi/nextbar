package com.nextbar.bar.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nextbar.bar.model.SupplyStatus;
import com.nextbar.bar.dto.request.CreateSupplyRequestDto;
import com.nextbar.bar.dto.request.UpdateSupplyRequestStatusDto;
import com.nextbar.bar.dto.response.SupplyRequestDto;
import com.nextbar.bar.security.RbacService;
import com.nextbar.bar.service.SupplyRequestService;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * REST controller for managing supply requests for a bar.
 * Provides endpoints to create, view, delete and update supply requests.
 */
@RestController
@RequestMapping("/api/v1/bars/{barId}/supply")
@RequiredArgsConstructor
@Tag(name = "Supply", description = "Supply management")
public class SupplyRequestController {

    private final SupplyRequestService requestService;
    private final RbacService rbacService;

    /**
     * Create a new supply request.
     *
     * @param barId the bar UUID
     * @param dto   the create request payload containing items
     * @return the created supply request with HTTP 201
     */
    @PostMapping
    public ResponseEntity<SupplyRequestDto> createRequest(
            @PathVariable @NonNull UUID barId,
            @Valid @RequestBody CreateSupplyRequestDto dto) {
        rbacService.requireBarAccess(barId);
        SupplyRequestDto created = requestService.createRequest(barId, dto.items());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * View all supply requests for a bar.
     *
     * @param barId the bar UUID
     * @return list of supply requests
     */
    @GetMapping
    public ResponseEntity<List<SupplyRequestDto>> getRequests(@PathVariable @NonNull UUID barId) {
        rbacService.requireBarAccess(barId);
        return ResponseEntity.ok(requestService.getRequestsByBar(barId));
    }

    /**
     * View a single supply request.
     *
     * @param barId     the bar UUID
     * @param requestId the request UUID
     * @return the supply request details
     */
    @GetMapping("/{requestId}")
    public ResponseEntity<SupplyRequestDto> getRequest(@PathVariable @NonNull UUID barId,
            @PathVariable @NonNull UUID requestId) {
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
    public ResponseEntity<Void> deleteRequest(@PathVariable @NonNull UUID barId,
            @PathVariable @NonNull UUID requestId) {
        rbacService.requireBarAccess(barId);
        requestService.deleteRequest(requestId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Update supply request status (e.g., from PENDING to APPROVED).
     *
     * @param requestId the request UUID
     * @param dto       the status update payload
     * @return the updated supply request
     */
    @PatchMapping("/{requestId}/status")
    public ResponseEntity<SupplyRequestDto> updateRequestStatus(
            @PathVariable @NonNull UUID barId,
            @PathVariable @NonNull UUID requestId,
            @Valid @RequestBody UpdateSupplyRequestStatusDto dto) {
        rbacService.requireBarAccess(barId);
        // Parse status from string to enum
        SupplyStatus supplyStatus = SupplyStatus.valueOf(dto.status().toUpperCase());
        requestService.updateRequestStatus(requestId, null, supplyStatus);
        SupplyRequestDto updated = requestService.getRequest(requestId);
        return ResponseEntity.ok(updated);
    }
}
