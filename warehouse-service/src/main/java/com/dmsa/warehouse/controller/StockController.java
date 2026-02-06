package com.dmsa.warehouse.controller;

import com.dmsa.warehouse.dto.request.CreateStockRequest;
import com.dmsa.warehouse.dto.response.StockResponse;
import com.dmsa.warehouse.service.StockService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for beverage stock management.
 * Provides endpoints to view, add, and manage warehouse inventory.
 */
@RestController
@RequestMapping("/warehouse/stock")
@Tag(name = "Stock Management", description = "Endpoints for managing beverage inventory")
public class StockController {

    private final StockService stockService;

    public StockController(StockService stockService) {
        this.stockService = stockService;
    }

    /**
     * Get all beverage stock items.
     */
    @GetMapping
    @Operation(summary = "Get all stock", description = "Retrieves all beverage stock items in the warehouse")
    public ResponseEntity<List<StockResponse>> getAllStock() {
        List<StockResponse> stock = stockService.getAllStock();
        return ResponseEntity.ok(stock);
    }

    /**
     * Get stock for a specific beverage type.
     */
    @GetMapping("/type/{beverageType}")
    @Operation(summary = "Get stock by type", description = "Retrieves stock information for a specific beverage type")
    public ResponseEntity<StockResponse> getStockByType(@PathVariable String beverageType) {
        StockResponse stock = stockService.getStockByType(beverageType);
        return ResponseEntity.ok(stock);
    }

    /**
     * Get all items with low stock.
     */
    @GetMapping("/low")
    @Operation(summary = "Get low stock items", description = "Retrieves all beverage items below their minimum stock level")
    public ResponseEntity<List<StockResponse>> getLowStockItems() {
        List<StockResponse> lowStock = stockService.getLowStockItems();
        return ResponseEntity.ok(lowStock);
    }

    /**
     * Create a new beverage stock entry.
     */
    @PostMapping
    @Operation(summary = "Add new stock", description = "Creates a new beverage stock entry in the warehouse")
    public ResponseEntity<StockResponse> createStock(@Valid @RequestBody CreateStockRequest request) {
        StockResponse created = stockService.createStock(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Add quantity to existing stock.
     */
    @PutMapping("/type/{beverageType}/add")
    @Operation(summary = "Add to existing stock", description = "Adds quantity to an existing beverage stock")
    public ResponseEntity<StockResponse> addStock(
            @PathVariable String beverageType,
            @RequestParam int quantity) {
        StockResponse updated = stockService.addStock(beverageType, quantity);
        return ResponseEntity.ok(updated);
    }

    /**
     * Delete a beverage stock entry.
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete stock", description = "Removes a beverage stock entry from the warehouse")
    public ResponseEntity<Void> deleteStock(@PathVariable Long id) {
        stockService.deleteStock(id);
        return ResponseEntity.noContent().build();
    }
}
