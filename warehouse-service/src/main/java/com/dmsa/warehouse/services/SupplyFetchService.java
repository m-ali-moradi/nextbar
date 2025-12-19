package com.dmsa.warehouse.services;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.dmsa.warehouse.dto.SupplyItemDto;
import com.dmsa.warehouse.dto.SupplyRequestDto;
import com.dmsa.warehouse.feign.BarServiceClient;

import ch.qos.logback.core.testUtil.DummyEncoder;
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
        System.err.println(">> SupplyRequests fetch failed for bar " + barId +
                ": " + t.getMessage());

        // Create one dummy SupplyRequestDto with a non-null ID
        SupplyItemDto item = new SupplyItemDto();
        item.setProductId(UUID.fromString("00000000-0000-0000-0000-000000000000"));
        item.setProductName("UNKNOWN");
        item.setQuantity(0);

        SupplyRequestDto dummy = new SupplyRequestDto();
        dummy.setId(UUID.fromString("00000000-0000-0000-0000-000000000000"));
        dummy.setBarId(barId);
        dummy.setItems(List.of(item));
        dummy.setStatus("UNAVAILABLE");
        dummy.setCreatedAt("1970-01-01T00:00:00Z"); // Use a default date

        return List.of(dummy);
    }

    @SuppressWarnings("unused")
    private SupplyRequestDto fallbackSupplyRequest(UUID barId, UUID requestId, Throwable t) {
        System.err.println(">> SupplyRequest fetch failed for bar " + barId +
                " / req " + requestId + ": " + t.getMessage());

        SupplyItemDto item = new SupplyItemDto();
        item.setProductId(UUID.fromString("00000000-0000-0000-0000-000000000000"));
        item.setProductName("UNKNOWN");
        item.setQuantity(0);

        SupplyRequestDto dummy = new SupplyRequestDto();
        dummy.setId(UUID.fromString("00000000-0000-0000-0000-000000000000"));
        dummy.setBarId(barId);
        dummy.setItems(List.of(item));
        dummy.setStatus("UNAVAILABLE");
        dummy.setCreatedAt("1970-01-01T00:00:00Z");

        return dummy;
    }
}
