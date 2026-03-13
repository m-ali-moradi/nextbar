package com.nextbar.bar.service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.lang.NonNull;

import com.nextbar.bar.dto.response.TotalServedDto;
import com.nextbar.bar.dto.response.UsageLogDto;

/**
 * Service interface for managing usage logs in a bar.
 * Provides methods to log drink servings and retrieve usage logs for bars and
 * products.
 */
public interface UsageLogService {
    void logDrinkServed(@NonNull UUID barId, String productName, int quantity);

    List<UsageLogDto> getLogsForBar(@NonNull UUID barId);

    List<UsageLogDto> getLogsForProduct(@NonNull UUID barId, String productName);

    List<UsageLogDto> getLogsForBarByDateRange(@NonNull UUID barId, LocalDate startDate, LocalDate endDate);

    List<TotalServedDto> getTotalServed(@NonNull UUID barId);

}
