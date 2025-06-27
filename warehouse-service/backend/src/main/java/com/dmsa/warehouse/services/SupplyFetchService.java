package com.dmsa.warehouse.services;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.dmsa.warehouse.dto.SupplyRequestDto;
import com.dmsa.warehouse.feign.BarServiceClient;

@Service
public class SupplyFetchService {
    private final BarServiceClient barClient;

    public SupplyFetchService(BarServiceClient barClient) {
        this.barClient = barClient;
    }

    public SupplyRequestDto fetchSupplyRequest(UUID barId, UUID requestId) {
        return barClient.getSupplyRequest(barId, requestId);
    }
}
