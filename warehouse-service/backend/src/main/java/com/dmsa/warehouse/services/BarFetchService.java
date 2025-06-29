// src/main/java/com/dmsa/warehouse/services/BarFetchService.java
package com.dmsa.warehouse.services;

import com.dmsa.warehouse.dto.BarDto;
import com.dmsa.warehouse.feign.BarServiceClient;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BarFetchService {
    private final BarServiceClient barClient;

    public BarFetchService(BarServiceClient barClient) {
        this.barClient = barClient;
    }

    @CircuitBreaker(name = "barService", fallbackMethod = "fallbackAllBars")
    public List<BarDto> fetchAllBars() {
        return barClient.getAllBars();
    }

    @SuppressWarnings("unused")
    private List<BarDto> fallbackAllBars(Throwable t) {
        System.err.println(">> BARs FETCH FAILED: " + t.getMessage());
        return List.of();
    }

}