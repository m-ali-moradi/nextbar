package de.fhdo.eventPlanner.client;

import de.fhdo.eventPlanner.dto.external.WarehouseStockDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

/**
 * Feign client for communicating with the warehouse-service.
 * Used to fetch inventory items for bar stock creation.
 */
@FeignClient(name = "warehouse-service", fallback = WarehouseServiceClientFallback.class)
public interface WarehouseServiceClient {

    /**
     * Get all stock items from the warehouse-service.
     */
    @GetMapping("/warehouse/stock")
    List<WarehouseStockDto> getAllStock();

    /**
     * Get stock by beverage type.
     */
    @GetMapping("/warehouse/stock/type/{beverageType}")
    WarehouseStockDto getStockByType(@PathVariable("beverageType") String beverageType);

    /**
     * Get items with low stock.
     */
    @GetMapping("/warehouse/stock/low")
    List<WarehouseStockDto> getLowStockItems();
}
