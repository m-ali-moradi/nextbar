package com.nextbar.bar.service;

import java.util.List;
import java.util.UUID;

import org.springframework.lang.NonNull;

import com.nextbar.bar.dto.response.BarStockItemDto;

/**
 * Service interface for managing bar stock operations.
 * Provides methods to retrieve, add, reduce, and check stock availability for products in a bar.
 */
public interface BarStockService {


    List<BarStockItemDto> getStock(@NonNull UUID barId);

    List<BarStockItemDto> getStockByProductAndBar(@NonNull UUID barId, String productName);

    void addStock(@NonNull UUID barId, String productName, int quantity);

    void upsertStock(@NonNull UUID barId, String productName, int quantity);

    void reduceStock(@NonNull UUID barId, String productName, int quantity);

    boolean isStockAvailable(@NonNull UUID barId, String productName, int quantity);

    void resetStock(@NonNull UUID barId);

    void deleteStockItem(@NonNull UUID barId, String productName);

    void clearStock(@NonNull UUID barId);


}

