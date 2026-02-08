package com.dmsa.warehouse.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.dmsa.warehouse.dto.external.SupplyRequestDto;
import com.dmsa.warehouse.dto.request.SupplyFulfillmentRequest;
import com.dmsa.warehouse.dto.response.SupplyFulfillmentResponse;
import com.dmsa.warehouse.service.SupplyRequestProcessingService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

/**
 * REST controller for managing supply requests from bars.
 * Provides endpoints to view and process supply requests.
 */
@RestController
@RequestMapping("/warehouse")
@Tag(name = "Supply Request Management", description = "Endpoints for processing bar supply requests")
public class SupplyController {

    private final SupplyRequestProcessingService supplyService;

    public SupplyController(SupplyRequestProcessingService supplyService) {
        this.supplyService = supplyService;
    }

    /**
     * Get all supply requests across all bars.
     */
    @GetMapping("/supply")
    @Operation(summary = "List all supply requests", description = "Retrieves all supply requests across all bars")
    public ResponseEntity<List<SupplyRequestDto>> listAllSupplyRequests() {
        List<SupplyRequestDto> requests = supplyService.getAllSupplyRequests();
        return ResponseEntity.ok(requests);
    }

    /**
     * Get supply requests for a specific bar.
     */
    @GetMapping("/bars/{barId}/supply")
    @Operation(summary = "List supply requests", description = "Retrieves all supply requests for a specific bar")
    public ResponseEntity<List<SupplyRequestDto>> listSupplyRequests(@PathVariable UUID barId) {
        List<SupplyRequestDto> requests = supplyService.getSupplyRequests(barId);
        return ResponseEntity.ok(requests);
    }

    /**
     * Get a specific supply request.
     */
    @GetMapping("/bars/{barId}/supply/{requestId}")
    @Operation(summary = "Get supply request", description = "Retrieves details of a specific supply request")
    public ResponseEntity<SupplyRequestDto> getSupplyRequest(
            @PathVariable UUID barId,
            @PathVariable UUID requestId) {
        SupplyRequestDto request = supplyService.getSupplyRequest(barId, requestId);
        return ResponseEntity.ok(request);
    }

    /**
     * Process a supply request (fulfill or reject).
     * 
     * This endpoint handles the warehouse's response to a supply request:
     * - From REQUESTED: Check stock and move to IN_PROGRESS or REJECTED
     * - From IN_PROGRESS: Mark as DELIVERED
     */
    @PutMapping("/bars/{barId}/supply/{requestId}/fulfill")
    @Operation(summary = "Process supply request", description = "Processes a supply request by checking stock and updating status")
    public ResponseEntity<SupplyFulfillmentResponse> processSupplyRequest(
            @PathVariable UUID barId,
            @PathVariable UUID requestId,
            @Valid @RequestBody SupplyFulfillmentRequest request) {
        SupplyFulfillmentResponse response = supplyService.processSupplyRequest(
                barId, requestId, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Reject a supply request with a reason.
     */
    @PutMapping("/bars/{barId}/supply/{requestId}/reject")
    @Operation(summary = "Reject supply request", description = "Rejects a supply request with a reason")
    public ResponseEntity<SupplyRequestDto> rejectSupplyRequest(
            @PathVariable UUID barId,
            @PathVariable UUID requestId,
            @Valid @RequestBody RejectSupplyRequest request) {
        SupplyRequestDto response = supplyService.rejectSupplyRequest(barId, requestId, request.getReason());
        return ResponseEntity.ok(response);
    }

    /**
     * Legacy endpoint for compatibility - redirects to new endpoint.
     * Kept for backward compatibility with existing frontend.
     */
    @PutMapping("/bars/replenish/{barId}/{requestId}")
    @Operation(summary = "Process supply request (legacy)", description = "Legacy endpoint for backward compatibility")
    public ResponseEntity<SupplyFulfillmentResponse> processSupplyRequestLegacy(
            @PathVariable UUID barId,
            @PathVariable UUID requestId,
            @RequestParam String currentStatus,
            @RequestBody LegacyReplenishRequest request) {

        SupplyFulfillmentRequest fulfillmentRequest = new SupplyFulfillmentRequest(
                request.getBeverageType(),
                request.getQuantity(),
                currentStatus);

        SupplyFulfillmentResponse response = supplyService.processSupplyRequest(
                barId, requestId, fulfillmentRequest);
        return ResponseEntity.ok(response);
    }

    /**
     * Legacy request format for backward compatibility.
     */
    public static class LegacyReplenishRequest {
        private String beverageType;
        private int quantity;

        public String getBeverageType() {
            return beverageType;
        }

        public void setBeverageType(String beverageType) {
            this.beverageType = beverageType;
        }

        public int getQuantity() {
            return quantity;
        }

        public void setQuantity(int quantity) {
            this.quantity = quantity;
        }
    }

    /**
     * Request payload for rejecting a supply request.
     */
    public static class RejectSupplyRequest {
        @jakarta.validation.constraints.NotBlank(message = "Rejection reason is required")
        private String reason;

        public String getReason() {
            return reason;
        }

        public void setReason(String reason) {
            this.reason = reason;
        }
    }
}
