package com.dmsa.warehouse.services;

import com.dmsa.warehouse.dto.BarDto;
import com.dmsa.warehouse.feign.BarServiceClient;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

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
        BarDto bar1 = new BarDto();
        bar1.setId(UUID.fromString("11111111-1111-1111-1111-111111111111"));
        bar1.setName("Fallback Bar A");
        bar1.setLocation("Unknown");
        bar1.setMaxCapacity(100);

        BarDto bar2 = new BarDto();
        bar2.setId(UUID.fromString("22222222-2222-2222-2222-222222222222"));
        bar2.setName("Fallback Bar B");
        bar2.setLocation("Nowhere");
        bar2.setMaxCapacity(50);

        return List.of(bar1, bar2);
    }

}