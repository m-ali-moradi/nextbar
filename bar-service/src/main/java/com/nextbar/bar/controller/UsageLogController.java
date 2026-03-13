package com.nextbar.bar.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.lang.NonNull;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.nextbar.bar.dto.request.StockOperationDto;
import com.nextbar.bar.dto.response.TotalServedDto;
import com.nextbar.bar.dto.response.UsageLogDto;
import com.nextbar.bar.security.RbacService;
import com.nextbar.bar.service.UsageLogService;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * REST controller for managing usage logs.
 * Provides endpoints to record and view usage logs for a bar.
 */
@RestController
@RequestMapping("/api/v1/bars/{barId}/usage")
@RequiredArgsConstructor
@Validated
@Tag(name = "Usage", description = "Usage management")
public class UsageLogController {

    private final UsageLogService usageLogService;
    private final RbacService rbacService;

    /**
     * Record usage (consumption) of a product.
     *
     * @param barId the bar UUID
     * @param dto   the stock operation details (productId, quantity)
     * @return empty response with HTTP 200
     */
    @PostMapping
    public ResponseEntity<Void> recordUsage(
            @PathVariable @NonNull UUID barId,
            @Valid @RequestBody StockOperationDto dto) {
        rbacService.requireBarAccess(barId);
        usageLogService.logDrinkServed(barId, dto.productName(), dto.quantity());
        return ResponseEntity.ok().build();
    }

    /**
     * Get usage logs for a bar.
     *
     * @param barId the bar UUID
     * @return list of usage logs
     */
    @GetMapping
    public ResponseEntity<List<UsageLogDto>> getUsageLogs(@PathVariable @NonNull UUID barId) {
        rbacService.requireBarAccess(barId);
        return ResponseEntity.ok(usageLogService.getLogsForBar(barId));
    }

    /**
     * Get total quantity served per product for a bar.
     *
     * @param barId the bar UUID
     * @return list of totals (name + total)
     */
    @GetMapping("/total-served")
    public ResponseEntity<List<TotalServedDto>> getTotalServed(@PathVariable @NonNull UUID barId) {
        rbacService.requireBarAccess(barId);
        return ResponseEntity.ok(usageLogService.getTotalServed(barId));
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
            @PathVariable @NonNull UUID barId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        rbacService.requireBarAccess(barId);
        return ResponseEntity.ok(usageLogService.getLogsForBarByDateRange(barId, startDate, endDate));
    }
}
