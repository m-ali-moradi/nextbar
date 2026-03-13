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
 * Endpoints for empties handling by drop-point operators.
 */
@RestController
@RequestMapping("/api/v1/droppoints")
public class DropPointEmptiesController {

    private final DropPointService dropPointService;
    private final RbacService rbacService;

    public DropPointEmptiesController(DropPointService dropPointService, RbacService rbacService) {
        this.dropPointService = dropPointService;
        this.rbacService = rbacService;
    }

    /**
     * Add one empty to a drop point (operator/admin).
     */
    @PutMapping("/add_empties/{id}")
    public ResponseEntity<DropPointDto> addEmptiesToDropPoint(@PathVariable Long id) {
        rbacService.requireWriteAccess();
        return dropPointService.addEmpty(id)
                .map(DropPointConverter::toDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
