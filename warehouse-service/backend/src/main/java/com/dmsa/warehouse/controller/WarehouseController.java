package com.dmsa.warehouse.controller;

import com.dmsa.warehouse.dto.EmptyReceiveRequest;
import com.dmsa.warehouse.dto.ReplenishRequest;
import com.dmsa.warehouse.model.BeverageStock;
import com.dmsa.warehouse.model.EmptyBottleStock;
import com.dmsa.warehouse.repository.BeverageStockRepository;
import com.dmsa.warehouse.repository.EmptyBottleStockRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import com.dmsa.warehouse.services.WarehouseService;

import java.util.List;

@RestController
@RequestMapping("/warehouse")
public class WarehouseController {

    @Autowired
    private WarehouseService warehouseService;

    @GetMapping("/stock")
    public List<BeverageStock> getStock() {
        return warehouseService.getAllBeverages();
    }

    @PostMapping("/replenish")
    public ResponseEntity<String> replenish(@RequestBody ReplenishRequest request) {
        warehouseService.replenish(request.getBeverageType(), request.getQuantity());
        return ResponseEntity.ok("Replenished successfully");
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
}