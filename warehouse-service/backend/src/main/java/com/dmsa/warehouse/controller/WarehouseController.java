package com.dmsa.warehouse.controller;

import com.dmsa.warehouse.dto.BarDto;
import com.dmsa.warehouse.dto.EmptyReceiveRequest;
import com.dmsa.warehouse.dto.ReplenishRequest;
import com.dmsa.warehouse.dto.ReplenishResponse;
import com.dmsa.warehouse.dto.SupplyRequestDto;
import com.dmsa.warehouse.feign.BarServiceClient;
import com.dmsa.warehouse.model.BeverageStock;
import com.dmsa.warehouse.model.EmptyBottleStock;
import com.dmsa.warehouse.repository.BeverageStockRepository;
import com.dmsa.warehouse.repository.EmptyBottleStockRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

import com.dmsa.warehouse.services.BarFetchService;
import com.dmsa.warehouse.services.SupplyFetchService;
import com.dmsa.warehouse.services.SupplyUpdateService;
import com.dmsa.warehouse.services.WarehouseService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/warehouse")
public class WarehouseController {

    @Autowired
    private WarehouseService warehouseService;

    private final SupplyUpdateService supplyUpdateService;

    private final BarFetchService barFetchService;

    private final BarServiceClient barServiceClient;

    private final SupplyFetchService supplyFetchService;

    public WarehouseController(SupplyUpdateService supplyUpdateService, BarFetchService barFetchService,
            BarServiceClient barServiceClient, SupplyFetchService supplyFetchService) {
        this.barFetchService = barFetchService;
        this.supplyUpdateService = supplyUpdateService;
        this.barServiceClient = barServiceClient;
        this.supplyFetchService = supplyFetchService;
    }

    @GetMapping("/stock")
    public List<BeverageStock> getStock() {
        return warehouseService.getAllBeverages();
    }

    @PostMapping("/receive-empty")
    public ResponseEntity<String> receiveEmpty(@RequestBody EmptyReceiveRequest request) {
        warehouseService.acceptEmpties(request.getQuantity());
        return ResponseEntity.ok("Empties received");
    }

    @GetMapping("/empty-stock")
    public ResponseEntity<Integer> getEmptyStock() {
        return ResponseEntity.ok(warehouseService.getEmptyBottleStock());
    }

    // @PutMapping("/replenish/{barId}/{requestId}")
    // public ResponseEntity<ReplenishResponse> replenish(@PathVariable UUID barId,
    // @PathVariable UUID requestId,
    // @RequestBody int quantity,
    // @RequestBody ReplenishRequest request) {
    // supplyUpdateService.sendSupplyUpdate(barId, requestId, quantity, request);
    // ReplenishResponse response = new ReplenishResponse(barId, requestId,
    // quantity, "Supply status updated.");
    // return ResponseEntity.ok(response);
    // }

    @PutMapping("/replenish/{barId}/{requestId}")
    public ResponseEntity<ReplenishResponse> replenish(
            @PathVariable UUID barId,
            @PathVariable UUID requestId,
            @RequestParam String currentStatus,
            @RequestBody ReplenishRequest request) {
        try {
            ReplenishResponse resp = supplyUpdateService.processSupplyRequest(barId, requestId, request, currentStatus);
            return ResponseEntity.ok(resp);

        } catch (IllegalStateException ex) {
            // invalid transition
            return ResponseEntity
                    .badRequest()
                    .body(new ReplenishResponse(barId, requestId, 0, ex.getMessage()));
        }
    }

    @GetMapping("/bars")
    public ResponseEntity<List<BarDto>> listBars() {
        List<BarDto> bars = barFetchService.fetchAllBars();
        return ResponseEntity.ok(bars);
    }

    @GetMapping("/bars/{barId}/supply")
    public List<SupplyRequestDto> listSupply(@PathVariable UUID barId) {
        return barServiceClient.getSupplyRequests(barId);
    }

    @GetMapping("/bars/{barId}/supply/{requestId}")
    public ResponseEntity<SupplyRequestDto> getSupplyRequest(
            @PathVariable UUID barId,
            @PathVariable UUID requestId) {
        SupplyRequestDto dto = supplyFetchService.fetchSupplyRequest(barId, requestId);
        return ResponseEntity.ok(dto);
    }
}