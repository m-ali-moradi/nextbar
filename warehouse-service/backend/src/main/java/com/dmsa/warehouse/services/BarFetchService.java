// src/main/java/com/dmsa/warehouse/services/BarFetchService.java
package com.dmsa.warehouse.services;

import com.dmsa.warehouse.dto.BarDto;
import com.dmsa.warehouse.feign.BarServiceClient;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BarFetchService {
    private final BarServiceClient barClient;

    public BarFetchService(BarServiceClient barClient) {
        this.barClient = barClient;
    }

    /** Retrieves all bars from the bar-service */
    public List<BarDto> fetchAllBars() {
        return barClient.getAllBars();
    }
}