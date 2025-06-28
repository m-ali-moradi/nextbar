package com.dmsa.warehouse.controller;

import com.dmsa.warehouse.dto.BarDto;
import com.dmsa.warehouse.dto.ReplenishRequest;
import com.dmsa.warehouse.dto.ReplenishResponse;
import com.dmsa.warehouse.dto.SupplyRequestDto;
import com.dmsa.warehouse.feign.BarServiceClient;
import com.dmsa.warehouse.model.BeverageStock;
import com.dmsa.warehouse.model.DropPointRecord;
import com.dmsa.warehouse.model.EmptyBottleStock;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

import com.dmsa.warehouse.services.BarFetchService;
import com.dmsa.warehouse.services.DropPointIntegrationService;
import com.dmsa.warehouse.services.EmptyBottleStockService;
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

    private final DropPointIntegrationService dropPointIntegrationService;

    private final EmptyBottleStockService emptyBottleStockService;

    public WarehouseController(SupplyUpdateService supplyUpdateService, BarFetchService barFetchService,
            BarServiceClient barServiceClient, SupplyFetchService supplyFetchService,
            DropPointIntegrationService dropPointIntegrationService, EmptyBottleStockService emptyBottleStockService) {
        this.barFetchService = barFetchService;
        this.supplyUpdateService = supplyUpdateService;
        this.barServiceClient = barServiceClient;
        this.supplyFetchService = supplyFetchService;
        this.dropPointIntegrationService = dropPointIntegrationService;
        this.emptyBottleStockService = emptyBottleStockService;
    }

    @GetMapping("/stock")
    public List<BeverageStock> getStock() {
        return warehouseService.getAllBeverages();
    }

    @PutMapping("/bars/replenish/{barId}/{requestId}")
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

    @PostMapping("/droppoints/fetch-notified")
    public ResponseEntity<List<DropPointRecord>> fetchNotified() {
        return ResponseEntity.ok(dropPointIntegrationService.fetchNotified() == null ? List.of()
                : dropPointIntegrationService.fetchNotified());
    }

    @GetMapping("/droppoints/notified")
    public ResponseEntity<List<DropPointRecord>> listNotified() {
        return ResponseEntity.ok(dropPointIntegrationService.listNotified());
    }

    @PutMapping("/droppoints/{id}/accept")
    public ResponseEntity<DropPointRecord> accept(@PathVariable Long id) {
        DropPointRecord updated = dropPointIntegrationService.accept(id);
        return ResponseEntity.ok(updated);
    }

    @GetMapping("/droppoints/{id}/status")
    public ResponseEntity<DropPointRecord> status(@PathVariable Long id) {
        DropPointRecord rec = dropPointIntegrationService.getStatus(id);
        return ResponseEntity.ok(rec);
    }

    @GetMapping("/droppoints/all")
    public ResponseEntity<List<DropPointRecord>> listAll() {
        return ResponseEntity.ok(dropPointIntegrationService.listNotified());
    }

    @GetMapping("/empties")
    public ResponseEntity<List<EmptyBottleStock>> listEmpties() {
        return ResponseEntity.ok(emptyBottleStockService.listAll());
    }

    @PostMapping("/stock")
    public ResponseEntity<BeverageStock> createBeverage(@RequestBody BeverageStock incoming) {
        BeverageStock created = warehouseService
                .addBeverage(incoming.getBeverageType(), incoming.getQuantity());
        return ResponseEntity
                .status(201)
                .body(created);
    }

    @DeleteMapping("/stock/{id}")
    public ResponseEntity<Void> deleteBeverage(@PathVariable Long id) {
        warehouseService.deleteBeverage(id);
        return ResponseEntity.noContent().build();
    }

}