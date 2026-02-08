package de.fhdo.eventPlanner.client;

import de.fhdo.eventPlanner.dto.external.WarehouseStockDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

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
}
