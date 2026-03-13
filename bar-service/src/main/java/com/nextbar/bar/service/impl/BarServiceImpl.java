package com.nextbar.bar.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nextbar.bar.exception.ResourceNotFoundException;
import com.nextbar.bar.mapper.BarMapper;
import com.nextbar.bar.model.Bar;
import com.nextbar.bar.model.EventBarAssociation;
import com.nextbar.bar.dto.response.BarDto;
import com.nextbar.bar.repository.BarRepository;
import com.nextbar.bar.repository.EventBarAssociationRepository;
import com.nextbar.bar.service.BarService;

import lombok.RequiredArgsConstructor;


/**
 * Implementation of the BarService interface.
 * This service provides methods to register, retrieve, and list bars.
 * It uses a BarRepository to interact with the database.
 */
@Service
@RequiredArgsConstructor
public class BarServiceImpl implements BarService {

    private final BarRepository barRepository;
    private final EventBarAssociationRepository eventBarAssociationRepository;
    private final BarMapper barMapper;

    @Override
    @Transactional
    public BarDto registerBar(UUID id, String name, String location, int maxCapacity) {
        Bar bar = new Bar(id, name, location, maxCapacity, new ArrayList<>());
        return toBarDtoWithAssociation(barRepository.save(bar));
    }

    @Override
    @Transactional
    public BarDto createLocalBar(String name, String location, int maxCapacity) {
        UUID id = UUID.randomUUID();
        Bar bar = new Bar(id, name, location, maxCapacity, new ArrayList<>());
        return toBarDtoWithAssociation(barRepository.save(bar));
    }

    @Override
    @Transactional(readOnly = true)
    public BarDto getBar(UUID barId) {
        UUID requiredBarId = Objects.requireNonNull(barId, "barId must not be null");
        return barRepository.findById(requiredBarId)
                .map(this::toBarDtoWithAssociation)
            .orElseThrow(() -> new ResourceNotFoundException("Bar", requiredBarId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<BarDto> getAllBars() {
        return barRepository.findAll().stream()
                .map(this::toBarDtoWithAssociation)
                .toList();
    }

    private BarDto toBarDtoWithAssociation(Bar bar) {
        BarDto base = barMapper.toDto(bar);
        EventBarAssociation association = eventBarAssociationRepository
                .findTopByBarIdOrderByCreatedAtDesc(bar.getId())
                .orElse(null);

        if (association == null) {
            return base;
        }

        String eventName = association.getEventName();
        String eventStatus = association.getEventStatus();
        if (eventStatus != null) {
            eventStatus = eventStatus.trim().toUpperCase(Locale.ROOT);
        }

        return new BarDto(
                base.id(),
                base.name(),
                base.location(),
                base.maxCapacity(),
                eventName,
                eventStatus);
    }
}
