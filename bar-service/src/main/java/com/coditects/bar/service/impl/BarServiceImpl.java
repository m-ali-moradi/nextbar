package com.coditects.bar.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.coditects.bar.exception.ResourceNotFoundException;
import com.coditects.bar.mapper.BarMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.coditects.bar.model.Bar;
import com.coditects.bar.model.dto.BarDto;
import com.coditects.bar.repository.BarRepository;
import com.coditects.bar.service.BarService;

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
    private final BarMapper barMapper;

    @Override
    @Transactional
    public BarDto registerBar(UUID id, String name, String location, int maxCapacity) {
        Bar bar = new Bar(id, name, location, maxCapacity, new ArrayList<>());
        return barMapper.toDto(barRepository.save(bar));
    }

    @Override
    @Transactional(readOnly = true)
    public BarDto getBar(UUID barId) {
        return barRepository.findById(barId)
                .map(barMapper::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("Bar", barId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<BarDto> getAllBars() {
        return barRepository.findAll().stream()
                .map(barMapper::toDto)
                .toList();
    }
}
