package com.coditects.bar.controller.REST;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.coditects.bar.model.dto.BarStockItemDto;
import com.coditects.bar.service.BarStockService;

import lombok.RequiredArgsConstructor;

/**
 * REST controller for managing bar stock.
 * Provides endpoints to get, add, and reduce stock for a specific bar.
 */
@RestController
// Maps requests to "/api/bars/{barId}/stock" and allows cross-origin requests from "http://localhost:5173"
@RequestMapping("/bars/{barId}/stock")
//@CrossOrigin(origins = "http://localhost:5173")
@RequiredArgsConstructor
public class BarStockControllerREST {

    // Injects the BarStockService to handle business logic for stock operations
    private final BarStockService stockService;

    // Get stock for a specific bar
    @GetMapping
    public ResponseEntity<List<BarStockItemDto>> getStock(@PathVariable UUID barId) {

        if (barId == null) {
            return ResponseEntity.badRequest().build(); // Return 400 Bad Request if barId is null
        }

        // Returns a 200 OK response with the stock details for the specified bar
        return ResponseEntity.ok(stockService.getStock(barId));
    }

    // Add stock for a specific product in a bar
    @PostMapping("/add")
    public ResponseEntity<Void> addStock(@PathVariable UUID barId,
                                         @RequestParam UUID productId,
                                         @RequestParam int quantity) {
        if (barId == null) {
            return ResponseEntity.badRequest().build(); // Return 400 Bad Request if barId is null
        }
        if (productId == null || quantity <= 0) {
            return ResponseEntity.badRequest().build(); // Return 400 Bad Request if productId is null or quantity is invalid
        }
        stockService.addStock(barId, productId, quantity);

        // Returns a 200 OK response after adding stock
        return ResponseEntity.ok().build();
    }

    // Reduce stock (when serving)
    @PostMapping("/reduce")
    public ResponseEntity<Void> reduceStock(@PathVariable UUID barId,
                                            @RequestParam UUID productId,
                                            @RequestParam int quantity) {

        if (barId == null) {
            return ResponseEntity.badRequest().build(); // Return 400 Bad Request if barId is null
        }
        if (productId == null || quantity <= 0) {
            return ResponseEntity.badRequest().build(); // Return 400 Bad Request if productId is null or quantity is invalid
        }

        // Check if stock is available before reducing
        // if (!stockService.isStockAvailable(barId, productId, quantity)) {
        //     return ResponseEntity.status(409).build(); // Return 409 Conflict if stock is not available
        // }
        // Calls the service to reduce stock for the specified product in the bar
        stockService.reduceStock(barId, productId, quantity);

        // Returns a 200 OK response after reducing stock
        return ResponseEntity.ok().build();
    }

    // Reset stock for a specific bar
//    @PostMapping("/reset")
//    public ResponseEntity<Void> resetStock(@PathVariable UUID barId) {

//     // Validate barId
//          if (barId == null) {
//               return ResponseEntity.badRequest().build(); // Return 400 Bad Request if barId is null
//          }
//     // Calls the service to reset stock for the specified bar
//        stockService.resetStock(barId);
//     // Returns a 200 OK response after resetting stock
//        return ResponseEntity.ok().build();
//    }
}
