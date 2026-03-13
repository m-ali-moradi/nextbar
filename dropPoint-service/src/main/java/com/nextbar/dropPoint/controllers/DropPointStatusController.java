package com.nextbar.dropPoint.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nextbar.dropPoint.dto.DropPointDto;
import com.nextbar.dropPoint.mapper.DropPointConverter;
import com.nextbar.dropPoint.security.RbacService;
import com.nextbar.dropPoint.service.DropPointService;

/**
 * REST controller for DropPoint status operations.
 */
@RestController
@RequestMapping("/api/v1/droppoints")
public class DropPointStatusController {

    private final DropPointService dropPointService;
    private final RbacService rbacService;

    public DropPointStatusController(DropPointService dropPointService, RbacService rbacService) {
        this.dropPointService = dropPointService;
        this.rbacService = rbacService;
    }

    /**
     * Get Levels and Status for a DropPoint.
     */
    @GetMapping("/level_and_status/{id}")
    public ResponseEntity<DropPointDto> getLevelAndStatus(@PathVariable Long id) {
        rbacService.requireReadAccess();
        return dropPointService.getLevelAndStatus(id)
                .map(DropPointConverter::toDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}