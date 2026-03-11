package com.nextbar.eventPlanner.client;

import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.nextbar.eventPlanner.dto.external.WarehouseStockDto;

/**
 * Fallback implementation for WarehouseServiceClient.
 * Provides default responses when the warehouse-service is unavailable.
 */
@Component
public class WarehouseServiceClientFallback implements WarehouseServiceClient {

    private static final Logger log = LoggerFactory.getLogger(WarehouseServiceClientFallback.class);

    @Override
    public List<WarehouseStockDto> getAllStock() {
        log.warn("WarehouseService unavailable - returning empty stock list");
        return Collections.emptyList();
    }

    @Override
    public WarehouseStockDto getStockByType(String beverageType) {
        log.warn("WarehouseService unavailable - cannot fetch stock for type: {}", beverageType);
        return null;
    }

    @Override
    public List<WarehouseStockDto> getLowStockItems() {
        log.warn("WarehouseService unavailable - returning empty low stock list");
        return Collections.emptyList();
    }

    @Override
    public WarehouseStockDto reserveStock(String beverageType, int quantity) {
        log.warn("WarehouseService unavailable - cannot reserve stock type={} quantity={}", beverageType, quantity);
        return null;
    }

    @Override
    public WarehouseStockDto releaseReservedStock(String beverageType, int quantity) {
        log.warn("WarehouseService unavailable - cannot release stock type={} quantity={}", beverageType, quantity);
        return null;
    }

    @Override
    public WarehouseStockDto consumeReservedStock(String beverageType, int quantity) {
        log.warn("WarehouseService unavailable - cannot consume stock type={} quantity={}", beverageType, quantity);
        return null;
    }
}
