package com.coditects.bar.controller.REST;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.coditects.bar.model.dto.BarDto;
import com.coditects.bar.service.BarService;

import lombok.RequiredArgsConstructor;

/**
 * REST controller for managing bars.
 * Provides endpoints to create, view, and update bars.
 */
@RestController
// Maps requests to "/api/bars" and allows cross-origin requests from "http://localhost:5173". it it my frontend application :)
// Other origins can be added as needed for CORS support e.g Event Planner service
@RequestMapping("/api/bars")
@CrossOrigin(origins = "http://localhost:5173")
@RequiredArgsConstructor

public class BarControllerREST {

    // Injects the BarService to handle business logic for bar operations
    private final BarService barService;

    // Register a new bar
    @PostMapping
    public ResponseEntity<BarDto> registerBar(@RequestParam String name, @RequestParam String location, @RequestParam int maxCapacity) {

        // Validate input parameters
        if (name == null || name.isEmpty() || location == null || location.isEmpty() || maxCapacity <= 0) {
            return ResponseEntity.badRequest().build(); // Return 400 Bad Request if parameters are invalid
        }
        // Returns a 201 Created response with the registered bar details
        return ResponseEntity.status(HttpStatus.CREATED).body(barService.registerBar(name, location, maxCapacity));
    }

    // Get a specific bar by its ID
    @GetMapping("/{barId}")
    public ResponseEntity<BarDto> getBar(@PathVariable UUID barId) {

        // Validate barId
        if (barId == null) {
            return ResponseEntity.badRequest().build(); // Return 400 Bad Request if barId is null
        }
        // Returns a 200 OK response with the bar details
        return ResponseEntity.ok(barService.getBar(barId));
    }

    // Get all bars
    @GetMapping
    public ResponseEntity<List<BarDto>> getAllBars() {

        // Returns a 200 OK response with the list of all bars
        return ResponseEntity.ok(barService.getAllBars());
    }
}
