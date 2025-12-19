package com.coditects.bar.controller;

import com.coditects.bar.model.dto.BarStockItemDto;
import com.coditects.bar.service.BarStockService;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for managing bar stock.
 * Provides endpoints to get, add, and reduce stock for a specific bar.
 */
@RestController
@RequestMapping("/bars/{barId}/stock")
@RequiredArgsConstructor
@Validated
public class BarStockController {

    private final BarStockService stockService;

    /**
     * Get stock for a specific bar.
     *
     * @param barId the bar UUID
     * @return list of stock items
     */
    @GetMapping
    public ResponseEntity<List<BarStockItemDto>> getStock(@PathVariable UUID barId) {
        return ResponseEntity.ok(stockService.getStock(barId));
    }

    /**
     * Add stock for a specific product in a bar.
     *
     * @param barId     the bar UUID
     * @param productId the product UUID
     * @param quantity  the quantity to add
     * @return empty response with HTTP 200
     */
    @PostMapping("/add")
    public ResponseEntity<Void> addStock(
            @PathVariable UUID barId,
            @RequestParam @NotNull(message = "Product ID is required") UUID productId,
            @RequestParam @Min(value = 1, message = "Quantity must be at least 1") int quantity) {
        
        stockService.addStock(barId, productId, quantity);
        return ResponseEntity.ok().build();
    }

    /**
     * Reduce stock (when serving).
     *
     * @param barId     the bar UUID
     * @param productId the product UUID
     * @param quantity  the quantity to reduce
     * @return empty response with HTTP 200
     */
    @PostMapping("/reduce")
    public ResponseEntity<Void> reduceStock(
            @PathVariable UUID barId,
            @RequestParam @NotNull(message = "Product ID is required") UUID productId,
            @RequestParam @Min(value = 1, message = "Quantity must be at least 1") int quantity) {
        
        stockService.reduceStock(barId, productId, quantity);
        return ResponseEntity.ok().build();
    }
}
