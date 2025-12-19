package com.dmsa.warehouse.feign;

import java.util.List;
import java.util.UUID;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.dmsa.warehouse.dto.BarDto;
import com.dmsa.warehouse.dto.SupplyRequestDto;

@FeignClient(name = "bar-service")
public interface BarServiceClient {

        @PutMapping("/bars/{barId}/supply/{requestId}/status")
        ResponseEntity<Void> updateSupplyStatus(
                        @PathVariable("barId") UUID barId,
                        @PathVariable("requestId") UUID requestId,
                        @RequestParam("quantity") int quantity,
                        @RequestParam("status") String status);

        @GetMapping("/bars")
        List<BarDto> getAllBars();

        @GetMapping("/bars/{barId}/supply")
        List<SupplyRequestDto> getSupplyRequests(@PathVariable("barId") UUID barId);

        @GetMapping("/bars/{barId}/supply/{requestId}")
        SupplyRequestDto getSupplyRequest(
                        @PathVariable("barId") UUID barId,
                        @PathVariable("requestId") UUID requestId);
}
