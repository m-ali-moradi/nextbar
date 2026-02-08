package com.nextbar.bar.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nextbar.bar.model.dto.BarStockItemDto;
import com.nextbar.bar.model.dto.StockOperationDto;
import com.nextbar.bar.security.RbacService;
import com.nextbar.bar.service.BarStockService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

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
    private final RbacService rbacService;

    /**
     * Get stock for a specific bar.
     *
     * @param barId the bar UUID
     * @return list of stock items
     */
    @GetMapping
    public ResponseEntity<List<BarStockItemDto>> getStock(@PathVariable UUID barId) {
        rbacService.requireBarAccess(barId);
        return ResponseEntity.ok(stockService.getStock(barId));
    }

    /**
     * Add stock for a specific product in a bar.
     *
     * @param barId the bar UUID
     * @param dto   the stock operation details (productId, quantity)
     * @return empty response with HTTP 200
     */
    @PostMapping("/add")
    public ResponseEntity<Void> addStock(
            @PathVariable UUID barId,
            @Valid @RequestBody StockOperationDto dto) {
        rbacService.requireBarAccess(barId);
        stockService.addStock(barId, dto.productId(), dto.quantity());
        return ResponseEntity.ok().build();
    }

    /**
     * Reduce stock (when serving).
     *
     * @param barId the bar UUID
     * @param dto   the stock operation details (productId, quantity)
     * @return empty response with HTTP 200
     */
    @PostMapping("/reduce")
    public ResponseEntity<Void> reduceStock(
            @PathVariable UUID barId,
            @Valid @RequestBody StockOperationDto dto) {
        rbacService.requireBarAccess(barId);
        stockService.reduceStock(barId, dto.productId(), dto.quantity());
        return ResponseEntity.ok().build();
    }
}
