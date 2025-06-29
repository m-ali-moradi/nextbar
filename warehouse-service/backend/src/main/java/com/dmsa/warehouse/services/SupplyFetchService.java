package com.dmsa.warehouse.services;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.dmsa.warehouse.dto.SupplyRequestDto;
import com.dmsa.warehouse.feign.BarServiceClient;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;

@Service
public class SupplyFetchService {
    private final BarServiceClient barClient;

    public SupplyFetchService(BarServiceClient barClient) {
        this.barClient = barClient;
    }

    @CircuitBreaker(name = "barService", fallbackMethod = "fallbackSupplyRequests")
    public SupplyRequestDto fetchSupplyRequest(UUID barId, UUID requestId) {
        return barClient.getSupplyRequest(barId, requestId);
    }

    @SuppressWarnings("unused")
    private List<SupplyRequestDto> fallbackSupplyRequests(UUID barId, Throwable t) {
        System.err.println(">> Supply Request FETCH FAILED: " + t.getMessage());
        return List.of();
    }

    @SuppressWarnings("unused")
    private SupplyRequestDto fallbackSupplyRequest(UUID barId, UUID requestId, Throwable t) {
        System.err.println(">> Supply Request FETCH FAILED: " + t.getMessage());
        return null;
    }
}
