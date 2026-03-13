package com.nextbar.warehouse.controller;

import com.nextbar.warehouse.dto.response.CollectionResponse;
import com.nextbar.warehouse.dto.response.EmptyBottleInventoryResponse;
import com.nextbar.warehouse.EmptyBottleCollectionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST controller for empty bottle collection from drop points.
 * Provides endpoints to sync, view, and manage bottle collections.
 */
@RestController
@RequestMapping("/api/v1/warehouse/collections")
@Tag(name = "Empty Bottle Collection", description = "Endpoints for managing empty bottle collection from drop points")
public class CollectionController {

    private final EmptyBottleCollectionService collectionService;

    public CollectionController(EmptyBottleCollectionService collectionService) {
        this.collectionService = collectionService;
    }

    /**
     * Sync drop point notifications from drop-point service.
     * Creates local collection records for notified drop points.
     */
    @PostMapping("/sync")
    @Operation(summary = "Sync drop point notifications", description = "Fetches notifications from drop-point service and creates collection records")
    public ResponseEntity<List<CollectionResponse>> syncNotifications() {
        List<CollectionResponse> synced = collectionService.syncDropPointNotifications();
        return ResponseEntity.ok(synced);
    }

    /**
     * Get all pending collections.
     */
    @GetMapping("/pending")
    @Operation(summary = "Get pending collections", description = "Retrieves all collections awaiting acceptance")
    public ResponseEntity<List<CollectionResponse>> getPendingCollections() {
        List<CollectionResponse> pending = collectionService.getPendingCollections();
        return ResponseEntity.ok(pending);
    }

    /**
     * Get all collections.
     */
    @GetMapping
    @Operation(summary = "Get all collections", description = "Retrieves all collection records regardless of status")
    public ResponseEntity<List<CollectionResponse>> getAllCollections() {
        List<CollectionResponse> all = collectionService.getAllCollections();
        return ResponseEntity.ok(all);
    }

    /**
     * Get a specific collection.
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get collection", description = "Retrieves details of a specific collection")
    public ResponseEntity<CollectionResponse> getCollection(@PathVariable Long id) {
        CollectionResponse collection = collectionService.getCollection(id);
        return ResponseEntity.ok(collection);
    }

    /**
     * Accept a collection request.
     * Moves status from PENDING to ACCEPTED and updates inventory.
     */
    @PutMapping("/{id}/accept")
    @Operation(summary = "Accept collection", description = "Accepts a pending collection and adds bottles to inventory")
    public ResponseEntity<CollectionResponse> acceptCollection(@PathVariable Long id) {
        CollectionResponse accepted = collectionService.acceptCollection(id);
        return ResponseEntity.ok(accepted);
    }

    /**
     * Mark a collection as completed.
     * Moves status from ACCEPTED to COLLECTED.
     */
    @PutMapping("/{id}/complete")
    @Operation(summary = "Mark collection complete", description = "Marks an accepted collection as fully completed")
    public ResponseEntity<CollectionResponse> completeCollection(@PathVariable Long id) {
        CollectionResponse completed = collectionService.markCollected(id);
        return ResponseEntity.ok(completed);
    }

    /**
     * Get empty bottle inventory summary.
     */
    @GetMapping("/inventory")
    @Operation(summary = "Get inventory summary", description = "Retrieves total bottles collected per drop point")
    public ResponseEntity<List<EmptyBottleInventoryResponse>> getInventorySummary() {
        List<EmptyBottleInventoryResponse> inventory = collectionService.getInventorySummary();
        return ResponseEntity.ok(inventory);
    }

    /**
     * Get total bottles collected.
     */
    @GetMapping("/inventory/total")
    @Operation(summary = "Get total bottles", description = "Retrieves the total number of bottles collected across all drop points")
    public ResponseEntity<Map<String, Long>> getTotalBottles() {
        Long total = collectionService.getTotalBottlesCollected();
        return ResponseEntity.ok(Map.of("totalBottlesCollected", total));
    }

    // === Legacy endpoints for backward compatibility ===

    @PostMapping("/droppoints/fetch-notified")
    @Operation(summary = "Fetch notified (legacy)", description = "Legacy endpoint - use /sync instead")
    public ResponseEntity<List<CollectionResponse>> fetchNotifiedLegacy() {
        return syncNotifications();
    }

    @GetMapping("/droppoints/notified")
    @Operation(summary = "List notified (legacy)", description = "Legacy endpoint - use /pending instead")
    public ResponseEntity<List<CollectionResponse>> listNotifiedLegacy() {
        return getAllCollections();
    }

    @PutMapping("/droppoints/{id}/accept")
    @Operation(summary = "Accept (legacy)", description = "Legacy endpoint - use /{id}/accept instead")
    public ResponseEntity<CollectionResponse> acceptLegacy(@PathVariable Long id) {
        return acceptCollection(id);
    }

    @GetMapping("/droppoints/{id}/status")
    @Operation(summary = "Get status (legacy)", description = "Legacy endpoint - use /{id} instead")
    public ResponseEntity<CollectionResponse> statusLegacy(@PathVariable Long id) {
        return getCollection(id);
    }

    @GetMapping("/droppoints/all")
    @Operation(summary = "List all (legacy)", description = "Legacy endpoint - use / instead")
    public ResponseEntity<List<CollectionResponse>> listAllLegacy() {
        return getAllCollections();
    }

    @GetMapping("/empties")
    @Operation(summary = "List empties (legacy)", description = "Legacy endpoint - use /inventory instead")
    public ResponseEntity<List<EmptyBottleInventoryResponse>> listEmptiesLegacy() {
        return getInventorySummary();
    }
}
