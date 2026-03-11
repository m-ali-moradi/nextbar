package de.fhdo.eventPlanner.client;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

import de.fhdo.eventPlanner.dto.external.WarehouseStockDto;

/**
 * Feign client for communicating with the warehouse-service.
 * Used to fetch inventory items for bar stock creation.
 */
@FeignClient(name = "warehouse-service", fallback = WarehouseServiceClientFallback.class)
public interface WarehouseServiceClient {

    /**
     * Get all stock items from the warehouse-service.
     */
    @GetMapping("/api/v1/warehouse/stock")
    List<WarehouseStockDto> getAllStock();

    /**
     * Get stock by beverage type.
     */
    @GetMapping("/api/v1/warehouse/stock/type/{beverageType}")
    WarehouseStockDto getStockByType(@PathVariable("beverageType") String beverageType);

    /**
     * Get items with low stock.
     */
    @GetMapping("/api/v1/warehouse/stock/low")
    List<WarehouseStockDto> getLowStockItems();

        /**
         * Reserve stock quantity for a planned event.
         */
        @PutMapping("/api/v1/warehouse/stock/type/{beverageType}/reserve")
        WarehouseStockDto reserveStock(@PathVariable("beverageType") String beverageType,
            @RequestParam("quantity") int quantity);

        /**
         * Release previously reserved stock quantity.
         */
        @PutMapping("/api/v1/warehouse/stock/type/{beverageType}/release")
        WarehouseStockDto releaseReservedStock(@PathVariable("beverageType") String beverageType,
            @RequestParam("quantity") int quantity);

        /**
         * Consume previously reserved stock quantity.
         */
        @PutMapping("/api/v1/warehouse/stock/type/{beverageType}/consume")
        WarehouseStockDto consumeReservedStock(@PathVariable("beverageType") String beverageType,
            @RequestParam("quantity") int quantity);
}
