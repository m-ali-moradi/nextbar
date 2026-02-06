package com.dmsa.warehouse.client;

import com.dmsa.warehouse.dto.external.BarDto;
import com.dmsa.warehouse.dto.external.SupplyRequestDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Feign client for communicating with the bar-service.
 */
@FeignClient(name = "bar-service", fallbackFactory = BarServiceClientFallbackFactory.class)
public interface BarServiceClient {

    /**
     * Get all bars.
     */
    @GetMapping("/bars")
    List<BarDto> getAllBars();

    /**
     * Get supply requests for a specific bar.
     */
    @GetMapping("/bars/{barId}/supply")
    List<SupplyRequestDto> getSupplyRequests(@PathVariable("barId") UUID barId);

    /**
     * Get a specific supply request.
     */
    @GetMapping("/bars/{barId}/supply/{requestId}")
    SupplyRequestDto getSupplyRequest(
            @PathVariable("barId") UUID barId,
            @PathVariable("requestId") UUID requestId);

    /**
     * Update the status of a supply request.
     */
    @PutMapping("/bars/{barId}/supply/{requestId}/status")
    ResponseEntity<Void> updateSupplyStatus(
            @PathVariable("barId") UUID barId,
            @PathVariable("requestId") UUID requestId,
            @RequestParam("quantity") int quantity,
            @RequestParam("status") String status);
}
