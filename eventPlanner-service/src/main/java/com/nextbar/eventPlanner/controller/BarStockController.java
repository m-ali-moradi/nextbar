package com.nextbar.eventPlanner.controller;

import com.nextbar.eventPlanner.dto.external.WarehouseStockDto;
import com.nextbar.eventPlanner.dto.request.CreateBarStockRequest;
import com.nextbar.eventPlanner.dto.response.BarStockResponse;
import com.nextbar.eventPlanner.service.BarStockService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for BarStock management.
 * Provides endpoints for managing stock entries and warehouse inventory.
 */
@RestController
@RequestMapping("/api/v1/events/bars/{barId}/stocks")
@Tag(name = "Bar Stock Management", description = "Endpoints for managing stock items at bars")
public class BarStockController {

    private final BarStockService barStockService;

    public BarStockController(BarStockService barStockService) {
        this.barStockService = barStockService;
    }

    // ==================== GET Endpoints ====================

    /**
     * Get all stock entries for a bar.
     */
    @GetMapping
    @Operation(summary = "Get bar stocks", description = "Retrieves all stock entries for a bar")
    public ResponseEntity<List<BarStockResponse>> getStocksByBarId(
            @PathVariable @Parameter(description = "Bar ID") Long barId) {
        return ResponseEntity.ok(barStockService.getStocksByBarId(barId));
    }

    /**
     * Get stock entry by ID.
     */
    @GetMapping("/{stockId}")
    @Operation(summary = "Get stock by ID", description = "Retrieves a specific stock entry")
    public ResponseEntity<BarStockResponse> getStockById(
            @PathVariable Long barId,
            @PathVariable @Parameter(description = "Stock ID") Long stockId) {
        return ResponseEntity.ok(barStockService.getStockById(stockId));
    }

    // ==================== POST Endpoints ====================

    /**
     * Create a new stock entry.
     */
    @PostMapping
    @Operation(summary = "Create stock entry", description = "Creates a new stock entry for a bar")
    public ResponseEntity<BarStockResponse> createStock(
            @PathVariable Long barId,
            @Valid @RequestBody CreateBarStockRequest request) {
        // Override barId from path
        request.setBarId(barId);
        BarStockResponse response = barStockService.createStock(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // ==================== PUT Endpoints ====================

    /**
     * Update stock quantity.
     */
    @PutMapping("/{stockId}")
    @Operation(summary = "Update stock quantity", description = "Updates the quantity of a stock entry")
    public ResponseEntity<BarStockResponse> updateStockQuantity(
            @PathVariable Long barId,
            @PathVariable @Parameter(description = "Stock ID") Long stockId,
            @RequestParam @Parameter(description = "New quantity") Integer quantity) {
        return ResponseEntity.ok(barStockService.updateStockQuantity(stockId, quantity));
    }

    // ==================== DELETE Endpoints ====================

    /**
     * Delete a stock entry.
     */
    @DeleteMapping("/{stockId}")
    @Operation(summary = "Delete stock entry", description = "Deletes a stock entry")
    public ResponseEntity<Void> deleteStock(
            @PathVariable Long barId,
            @PathVariable @Parameter(description = "Stock ID") Long stockId) {
        barStockService.deleteStock(stockId);
        return ResponseEntity.noContent().build();
    }
}

/**
 * Additional controller for warehouse inventory endpoints.
 */
@RestController
@RequestMapping("/api/v1/events/warehouse")
@Tag(name = "Warehouse Integration", description = "Endpoints for warehouse inventory integration")
class WarehouseIntegrationController {

    private final BarStockService barStockService;

    WarehouseIntegrationController(BarStockService barStockService) {
        this.barStockService = barStockService;
    }

    /**
     * Get available inventory from warehouse.
     */
    @GetMapping("/inventory")
    @Operation(summary = "Get warehouse inventory", description = "Retrieves available inventory from WarehouseService")
    public ResponseEntity<List<WarehouseStockDto>> getWarehouseInventory() {
        return ResponseEntity.ok(barStockService.getWarehouseInventory());
    }

    /**
     * Get warehouse stock by beverage type.
     */
    @GetMapping("/inventory/{beverageType}")
    @Operation(summary = "Get stock by type", description = "Retrieves warehouse stock for a specific beverage type")
    public ResponseEntity<WarehouseStockDto> getStockByType(
            @PathVariable @Parameter(description = "Beverage type") String beverageType) {
        return ResponseEntity.ok(barStockService.getWarehouseStockByType(beverageType));
    }
}
