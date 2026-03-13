package com.nextbar.dropPoint.controllers;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nextbar.dropPoint.dto.DropPointDto;
import com.nextbar.dropPoint.mapper.DropPointConverter;
import com.nextbar.dropPoint.repositories.EventDroppointAssociationRepository;
import com.nextbar.dropPoint.security.RbacService;
import com.nextbar.dropPoint.service.DropPointService;

/**
 * REST controller for basic DropPoint operations.
 */
@RestController
@RequestMapping("/api/v1/droppoints")
public class DropPointController {

    private final DropPointService dropPointService;
    private final RbacService rbacService;
    private final EventDroppointAssociationRepository associationRepository;

    public DropPointController(DropPointService dropPointService, RbacService rbacService,
            EventDroppointAssociationRepository associationRepository) {
        this.dropPointService = dropPointService;
        this.rbacService = rbacService;
        this.associationRepository = associationRepository;
    }

    /**
     * Get all DropPoints.
     */
    @GetMapping
    public ResponseEntity<List<DropPointDto>> getAllDropPoints() {
        rbacService.requireReadAccess();
        List<DropPointDto> dropPointDtos = dropPointService.findAll()
                .stream()
                .map(dropPoint -> toDtoWithAssociation(dropPoint.getId(), DropPointConverter.toDto(dropPoint)))
                .collect(Collectors.toList());

        return ResponseEntity.ok(dropPointDtos);
    }

    /**
     * Get DropPoint by ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<DropPointDto> getDropPointById(@PathVariable Long id) {
        rbacService.requireReadAccess();

        return dropPointService.findById(id)
                .map(dropPoint -> toDtoWithAssociation(dropPoint.getId(), DropPointConverter.toDto(dropPoint)))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    private DropPointDto toDtoWithAssociation(Long dropPointId, DropPointDto dto) {
        String eventStatus = associationRepository.findTopByDropPointIdOrderByCreatedAtDesc(dropPointId)
                .map(association -> association.getEventStatus())
                .orElse(null);
        dto.setEventStatus(eventStatus);
        return dto;
    }

    /**
     * Create drop point (admin only).
     */
    @PostMapping
    public ResponseEntity<DropPointDto> createDropPoint(@RequestBody DropPointDto dto) {
        rbacService.requireAdminAccess();
        return ResponseEntity.ok(DropPointConverter.toDto(dropPointService.save(DropPointConverter.toEntity(dto))));
    }

    /**
     * Update drop point (admin only).
     */
    @PutMapping("/{id}")
    public ResponseEntity<DropPointDto> updateDropPoint(@PathVariable Long id, @RequestBody DropPointDto dto) {
        rbacService.requireAdminAccess();
        return dropPointService.update(id, DropPointConverter.toEntity(dto))
                .map(DropPointConverter::toDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Delete drop point (admin only).
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDropPoint(@PathVariable Long id) {
        rbacService.requireAdminAccess();
        return dropPointService.deleteById(id)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }
}