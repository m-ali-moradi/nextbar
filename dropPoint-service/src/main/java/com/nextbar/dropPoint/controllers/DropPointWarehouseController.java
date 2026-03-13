package com.nextbar.dropPoint.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nextbar.dropPoint.dto.DropPointDto;
import com.nextbar.dropPoint.mapper.DropPointConverter;
import com.nextbar.dropPoint.security.RbacService;
import com.nextbar.dropPoint.service.DropPointService;

/**
 * REST controller for DropPoint warehouse interaction operations.
 * Notifies the warehouse when a drop point is full and verifies transferred
 * empties.
 * the notification message is sent via RabbitMQ to the warehouse, which then
 * processes the drop point and updates its status.
 */
@RestController
@RequestMapping("/api/v1/droppoints")
public class DropPointWarehouseController {

    private final DropPointService dropPointService;
    private final RbacService rbacService;

    public DropPointWarehouseController(DropPointService dropPointService, RbacService rbacService) {
        this.dropPointService = dropPointService;
        this.rbacService = rbacService;
    }

    /**
     * Notify warehouse for a DropPoint.
     */
    @PutMapping("/notify_warehouse/{id}")
    public ResponseEntity<DropPointDto> notifyWarehouse(@PathVariable Long id) {
        rbacService.requireWriteAccess();
        return dropPointService.notifyWarehouse(id)
                .map(DropPointConverter::toDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}