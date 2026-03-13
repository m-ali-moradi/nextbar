package com.nextbar.bar.service.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.lang.NonNull;

import org.springframework.stereotype.Service;

import com.nextbar.bar.exception.ValidationException;
import com.nextbar.bar.dto.response.TotalServedDto;
import com.nextbar.bar.dto.response.UsageLogDto;
import com.nextbar.bar.model.UsageLog;
import com.nextbar.bar.repository.BarRepository;
import com.nextbar.bar.repository.UsageLogRepository;
import com.nextbar.bar.service.UsageLogService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

/**
 * UsageLogServiceImpl provides the implementation for managing usage logs,
 * which record the history of drinks served in bars.
 * It includes methods for logging drink servings, retrieving logs for bars and
 * products,
 * and calculating total drinks served by product.
 * 
 */

@Service
@RequiredArgsConstructor
public class UsageLogServiceImpl implements UsageLogService {

    // Injecting repositories for usage logs, bars, and products
    private final UsageLogRepository logRepo;
    private final BarRepository barRepository;

    // Logs a drink serving for a specific bar and product
    @Override
    // This method is transactional to ensure atomicity of the log entry creation
    @Transactional
    public void logDrinkServed(@NonNull UUID barId, String productName, int quantity) {
        /*
         * data validation
         * 1. check the availability of bar and productId
         * 2. check if quantity is less than or equal to the available stock
         */

        // Verify bar exists
        if (!barRepository.existsById(barId)) {
            throw new ValidationException("Bar not found: " + barId);
        }

        String normalizedProductName = normalizeProductName(productName);
        if (normalizedProductName == null) {
            throw new ValidationException("Product name is required");
        }

        // check if the product is passed validation and reduced from the stock. it
        // should be transactional
        // this will ensure that if stock is reduced, then the log entry is created,
        // otherwise it will throw an exception

        // Create and save the usage log entry
        UsageLog usageLog = new UsageLog();
        usageLog.setBarId(barId);
        usageLog.setProductName(normalizedProductName);
        usageLog.setQuantity(quantity);
        usageLog.setTimestamp(LocalDateTime.now());

        logRepo.save(usageLog);
    }

    // Retrieves usage logs for a specific bar, sorted by timestamp in descending
    // order
    @Override
    public List<UsageLogDto> getLogsForBar(@NonNull UUID barId) {

        // check bar the existance
        if (!barRepository.existsById(barId)) {
            throw new ValidationException("Bar not found: " + barId);
        }
        // Check if there are any logs for the bar
        // if (logRepo.findByBarId(barId).isEmpty()) {
        // throw new ValidationException("No logs found for bar: " + barId);
        // }
        // Fetch and convert usage logs for the bar, sorted by timestamp descending
        return logRepo.findByBarId(barId).stream()
                .map(this::toDto)
                .sorted((a, b) -> b.timestamp().compareTo(a.timestamp()))
                .toList();
    }

    @Override
    public List<UsageLogDto> getLogsForBarByDateRange(@NonNull UUID barId, LocalDate startDate, LocalDate endDate) {

        if (!barRepository.existsById(barId)) {
            throw new ValidationException("Bar not found: " + barId);
        }

        if (startDate == null || endDate == null) {
            throw new ValidationException("Start date and end date are required");
        }

        if (startDate.isAfter(endDate)) {
            throw new ValidationException("Start date cannot be after end date");
        }

        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);

        return logRepo.findByBarIdAndTimestampBetween(barId, startDateTime, endDateTime).stream()
                .map(this::toDto)
                .sorted((a, b) -> b.timestamp().compareTo(a.timestamp()))
                .toList();
    }

    // Retrieves usage logs for a specific product in a bar, sorted by timestamp in
    // descending order
    @Override
    public List<UsageLogDto> getLogsForProduct(@NonNull UUID barId, String productName) {

        // check if bar exists
        if (!barRepository.existsById(barId)) {
            throw new ValidationException("Bar not found: " + barId);
        }
        String normalizedProductName = normalizeProductName(productName);
        if (normalizedProductName == null) {
            throw new ValidationException("Product name is required");
        }
        // Check if there are any logs for the bar and product
        if (logRepo.findByBarIdAndProductNameIgnoreCase(barId, normalizedProductName).isEmpty()) {
            throw new ValidationException("No logs found for bar: " + barId + " and product: " + normalizedProductName);
        }
        // Fetch and convert usage logs for the bar and product, sorted by timestamp
        // descending
        return logRepo.findByBarIdAndProductNameIgnoreCase(barId, normalizedProductName).stream()
                .map(this::toDto)
                .sorted((a, b) -> b.timestamp().compareTo(a.timestamp()))
                .toList();
    }

    // Retrieves the total number of drinks served for each product in a bar
    @Override
    public List<TotalServedDto> getTotalServed(@NonNull UUID barId) {

        /*
         * Data validation
         * 1. Check if the bar exists
         * 2. Check if there are any logs for the bar
         * 3. If no logs, return an empty list
         * 4. If logs exist, calculate total served by productId
         */

        // check if bar exists
        if (!barRepository.existsById(barId)) {
            throw new ValidationException("Bar not found: " + barId);
        }
        // Fetch all logs for the bar
        List<UsageLogDto> serveLog = getLogsForBar(barId);

        // Check if there are any logs for the bar
        if (serveLog.isEmpty()) {
            return Collections.emptyList();
        }

        // Calculate total drinks served by product ID
        // Group logs by product name and sum the quantities
        Map<String, Integer> totalServedByName = serveLog.stream()
                .collect(Collectors.groupingBy(
                        UsageLogDto::productName,
                        Collectors.summingInt(UsageLogDto::quantity)));

        // Convert the totalServedById map to a list of entries with product names
        // Return a list of entries with product names and total quantities served
        return totalServedByName.entrySet().stream()
                .map(entry -> new TotalServedDto(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }

    // Convert UsageLog entity to DTO, including product name
    private UsageLogDto toDto(UsageLog log) {
        return new UsageLogDto(
                Objects.requireNonNull(log.getId()),
                Objects.requireNonNull(log.getBarId()),
                log.getProductName(),
                log.getQuantity(),
                log.getTimestamp());
    }

    private String normalizeProductName(String productName) {
        if (productName == null) {
            return null;
        }
        String normalized = productName.trim();
        return normalized.isBlank() ? null : normalized;
    }
}