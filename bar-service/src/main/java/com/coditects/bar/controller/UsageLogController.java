package com.coditects.bar.controller;

import com.coditects.bar.model.dto.UsageLogDto;
import com.coditects.bar.service.UsageLogService;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * REST controller for managing usage logs.
 * Provides endpoints to record and view usage logs for a bar.
 */
@RestController
@RequestMapping("/bars/{barId}/usage")
@RequiredArgsConstructor
@Validated
public class UsageLogController {

    private final UsageLogService usageLogService;

    /**
     * Record usage (consumption) of a product.
     *
     * @param barId     the bar UUID
     * @param productId the product UUID
     * @param quantity  the quantity consumed
     * @return empty response with HTTP 200
     */
    @PostMapping
    public ResponseEntity<Void> recordUsage(
            @PathVariable UUID barId,
            @RequestParam @NotNull(message = "Product ID is required") UUID productId,
            @RequestParam @Min(value = 1, message = "Quantity must be at least 1") int quantity) {
        
        usageLogService.logDrinkServed(barId, productId, quantity);
        return ResponseEntity.ok().build();
    }

    /**
     * Get usage logs for a bar.
     *
     * @param barId the bar UUID
     * @return list of usage logs
     */
    @GetMapping
    public ResponseEntity<List<UsageLogDto>> getUsageLogs(@PathVariable UUID barId) {
        return ResponseEntity.ok(usageLogService.getLogsForBar(barId));
    }

    /**
     * Get usage logs for a bar within a date range.
     *
     * @param barId     the bar UUID
     * @param startDate start date (inclusive)
     * @param endDate   end date (inclusive)
     * @return list of usage logs
     */
    @GetMapping("/range")
    public ResponseEntity<List<UsageLogDto>> getUsageLogsByDateRange(
            @PathVariable UUID barId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        // Note: Date range filtering not implemented in service interface yet
        // Returning all logs for the bar
        return ResponseEntity.ok(usageLogService.getLogsForBar(barId));
    }
}
