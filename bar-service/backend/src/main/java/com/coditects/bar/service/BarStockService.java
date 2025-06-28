package com.coditects.bar.service;

import java.util.List;
import java.util.UUID;

import com.coditects.bar.model.dto.BarStockItemDto;

/**
 * Service interface for managing bar stock operations.
 * Provides methods to retrieve, add, reduce, and check stock availability for products in a bar.
 */
public interface BarStockService {

    // Retrieves the stock of all products in a specific bar.
    List<BarStockItemDto> getStock(UUID barId);
    // Retrieves the stock of a specific product in a specific bar.
    List<BarStockItemDto> getStockByProductAndBar(UUID productId, UUID barId);
    // Retrieves the stock of a specific product in a specific bar by product ID.
    void addStock(UUID barId, UUID productId, int quantity);
    // Reduces the stock of a specific product in a specific bar by a given quantity.
    void reduceStock(UUID barId, UUID productId, int quantity);
    // Checks if a specific product is available in stock for a given bar and quantity.
    boolean isStockAvailable(UUID barId, UUID productId, int quantity);
    // Resets the stock for a specific bar, clearing all items.
    void resetStock(UUID barId);
    // Deletes a specific stock item from a bar by product ID.
    void deleteStockItem(UUID barId, UUID productId);


}

