package com.dmsa.warehouse.controller;

import com.dmsa.warehouse.dto.EmptyReceiveRequest;
import com.dmsa.warehouse.dto.ReplenishRequest;
import com.dmsa.warehouse.dto.ReplenishResponse;
import com.dmsa.warehouse.model.BeverageStock;
import com.dmsa.warehouse.model.EmptyBottleStock;
import com.dmsa.warehouse.repository.BeverageStockRepository;
import com.dmsa.warehouse.repository.EmptyBottleStockRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

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

    public WarehouseController(SupplyUpdateService supplyUpdateService) {
        this.supplyUpdateService = supplyUpdateService;
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

//     @PutMapping("/replenish/{barId}/{requestId}")
// public ResponseEntity<ReplenishResponse> replenish(@PathVariable UUID barId,
//                                                    @PathVariable UUID requestId,
//                                                    @RequestBody int quantity,
//                                                    @RequestBody ReplenishRequest request) {
//     supplyUpdateService.sendSupplyUpdate(barId, requestId, quantity, request);
//     ReplenishResponse response = new ReplenishResponse(barId, requestId, quantity, "Supply status updated.");
//     return ResponseEntity.ok(response);
// }

    @PutMapping("/replenish/{barId}/{requestId}")
        public ResponseEntity<ReplenishResponse> replenish(
                @PathVariable    UUID              barId,
                @PathVariable    UUID              requestId,
                @RequestParam    String            currentStatus,
                @RequestBody     ReplenishRequest  request
        ) {
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

}