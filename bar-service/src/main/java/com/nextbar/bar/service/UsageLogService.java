package com.nextbar.bar.service;

import java.util.List;
import java.util.UUID;

import com.nextbar.bar.model.dto.TotalServedDto;
import com.nextbar.bar.model.dto.UsageLogDto;

/**
 * Service interface for managing usage logs in a bar.
 * Provides methods to log drink servings and retrieve usage logs for bars and products.
 */
public interface UsageLogService {
    void logDrinkServed(UUID barId, UUID productId, int quantity);

    List<UsageLogDto> getLogsForBar(UUID barId);

    List<UsageLogDto> getLogsForProduct(UUID barId, UUID productId);

    List<TotalServedDto> getTotalServed(UUID barId);

}
