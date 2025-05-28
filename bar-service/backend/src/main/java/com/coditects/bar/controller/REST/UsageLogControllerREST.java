package com.coditects.bar.controller.REST;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.coditects.bar.model.dto.UsageLogDto;
import com.coditects.bar.service.UsageLogService;

import lombok.RequiredArgsConstructor;

/**
 * REST controller for managing usage logs for a bar.
 * Provides endpoints to log drink servings and retrieve usage statistics.
 */
@RestController
// Maps requests to "/api/bars/{barId}/usage" and allows cross-origin requests from "http://localhost:5173". 
@RequestMapping("/api/bars/{barId}/usage")
@CrossOrigin(origins = "http://localhost:5173")
// RequiredArgsConstructor generates a constructor with required arguments for dependency injection
@RequiredArgsConstructor
public class UsageLogControllerREST {

    // Injects the UsageLogService to handle business logic for usage logs
    private final UsageLogService usageLogService;

    // Log a drink served
    @PostMapping
    public ResponseEntity<Void> logUsage(@PathVariable UUID barId,
            @RequestParam UUID productId,
            @RequestParam int quantity) {
        // Validate inputs.
        if (barId == null || productId == null || quantity <= 0) {
            return ResponseEntity.badRequest().build(); // Return 400 Bad Request if inputs are invalid
        }
        // Log the drink served
        usageLogService.logDrinkServed(barId, productId, quantity);
        // Return 200 OK if the log was successful
        return ResponseEntity.ok().build();
    }

    // Get all usage logs for a bar
    @GetMapping
    public ResponseEntity<List<UsageLogDto>> getLogs(@PathVariable UUID barId) {
        // Validate barId
        if (barId == null) {
            return ResponseEntity.badRequest().build(); // Return 400 Bad Request if barId is null
        }
        // Retrieve and return all usage logs for the bar
        return ResponseEntity.ok(usageLogService.getLogsForBar(barId));
    }

    // Get logs for a product
    @GetMapping("/product/{productId}")
    public ResponseEntity<List<UsageLogDto>> getLogsForProduct(@PathVariable UUID barId,
            @PathVariable UUID productId) {
        // Validate inputs
        if (barId == null || productId == null) {
            return ResponseEntity.badRequest().build(); // Return 400 Bad Request if inputs are invalid
        }
        // Retrieve and return usage logs for the specified product in the bar
        return ResponseEntity.ok(usageLogService.getLogsForProduct(barId, productId));
    }

    // Get total drinks served for a bar
    @GetMapping("/total-served")
    public ResponseEntity<List<Map.Entry<String, Integer>>> getTotalServed(@PathVariable UUID barId) {
        // Validate barId
        if (barId == null) {
            return ResponseEntity.badRequest().build(); // Return 400 Bad Request if barId is null
        }
        // Retrieve and return total drinks served for the bar
        return ResponseEntity.ok(usageLogService.getTotalServed(barId));
    }
}
