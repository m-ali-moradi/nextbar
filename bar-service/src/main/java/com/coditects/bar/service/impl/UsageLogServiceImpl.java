package com.coditects.bar.service.impl;

import java.time.LocalDateTime;
import java.util.AbstractMap;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.coditects.bar.exception.ValidationException;
import com.coditects.bar.model.Product;
import com.coditects.bar.model.UsageLog;
import com.coditects.bar.model.dto.UsageLogDto;
import com.coditects.bar.repository.BarRepository;
import com.coditects.bar.repository.ProductRepository;
import com.coditects.bar.repository.UsageLogRepository;
import com.coditects.bar.service.UsageLogService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

/**
 * UsageLogServiceImpl provides the implementation for managing usage logs,
 * which record the history of drinks served in bars.
 * It includes methods for logging drink servings, retrieving logs for bars and products,
 * and calculating total drinks served by product.
 * 
 */

@Service
@RequiredArgsConstructor
public class UsageLogServiceImpl implements UsageLogService {

    // Injecting repositories for usage logs, bars, and products
    private final UsageLogRepository logRepo;
    private final BarRepository barRepository;
    private final ProductRepository productRepository;

    // Logs a drink serving for a specific bar and product
    @Override
    // This method is transactional to ensure atomicity of the log entry creation
    @Transactional
    public void logDrinkServed(UUID barId, UUID productId, int quantity) {
       /*
        * data validation
        * 1. check the availability of bar and productId
        * 2. check if quantity is less than or equal to the available stock
        */

        // Verify bar exists
        if (!barRepository.existsById(barId)) {
            throw new ValidationException("Bar not found: " + barId);
        }

        // Verify product exists
        if (!productRepository.existsById(productId)) {
            throw new ValidationException("Product not found: " + productId);
        }

        // check if the product is passed validation and reduced from the stock. it should be transactional
        // this will ensure that if stock is reduced, then the log entry is created, otherwise it will throw an exception
       

        // Create and save the usage log entry
        UsageLog usageLog = new UsageLog();
        usageLog.setBarId(barId);
        usageLog.setProductId(productId);
        usageLog.setQuantity(quantity);
        usageLog.setTimestamp(LocalDateTime.now());

        logRepo.save(usageLog);
    }

    // Retrieves usage logs for a specific bar, sorted by timestamp in descending order
    @Override
    public List<UsageLogDto> getLogsForBar(UUID barId) {

        // check bar the existance
        if (!barRepository.existsById(barId)) {
            throw new ValidationException("Bar not found: " + barId);
        }
        // Check if there are any logs for the bar
//        if (logRepo.findByBarId(barId).isEmpty()) {
//            throw new ValidationException("No logs found for bar: " + barId);
//        }
        // Fetch and convert usage logs for the bar, sorted by timestamp descending
        return logRepo.findByBarId(barId).stream()
                .map(this::toDto)
                .sorted((a, b) -> b.timestamp().compareTo(a.timestamp()))
                .toList();
    }

    // Retrieves usage logs for a specific product in a bar, sorted by timestamp in descending order
    @Override
    public List<UsageLogDto> getLogsForProduct(UUID barId, UUID productId) {
        
        // check if bar exists
        if (!barRepository.existsById(barId)) {
            throw new ValidationException("Bar not found: " + barId);
        }
        // check if product exists
        if (!productRepository.existsById(productId)) {
            throw new ValidationException("Product not found: " + productId);
        }
        // Check if there are any logs for the bar and product
        if (logRepo.findByBarIdAndProductId(barId, productId).isEmpty()) {
            throw new ValidationException("No logs found for bar: " + barId + " and product: " + productId);
        }
        // Fetch and convert usage logs for the bar and product, sorted by timestamp descending
        return logRepo.findByBarIdAndProductId(barId, productId).stream()
                .map(this::toDto)
                .sorted((a, b) -> b.timestamp().compareTo(a.timestamp()))
                .toList();
    }

    // Retrieves the total number of drinks served for each product in a bar
    @Override
    public List<Map.Entry<String, Integer>> getTotalServed(UUID barId) {

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
        // Group logs by product ID and sum the quantities
        Map<UUID, Integer> totalServedById = serveLog.stream()
                .collect(Collectors.groupingBy(
                        UsageLogDto::productId,
                        Collectors.summingInt(UsageLogDto::quantity)
                ));

        // Map to hold product names corresponding to product IDs
        // Fetch product names for each product ID in the totalServedById map
        Map<UUID, String> productNameMap = new HashMap<>();
        for (UUID productId : totalServedById.keySet()) {
            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new ValidationException("Product not found: " + productId));
            productNameMap.put(productId, product.getName());
        }

        // Convert the totalServedById map to a list of entries with product names
        // Return a list of entries with product names and total quantities served
        return totalServedById.entrySet().stream()
                .map(entry -> {
                    String productName = productNameMap.getOrDefault(entry.getKey(), "Unknown Product");
                    return new AbstractMap.SimpleEntry<>(productName, entry.getValue());
                })
                .collect(Collectors.toList());
    }

    // Convert UsageLog entity to DTO, including product name
    private UsageLogDto toDto(UsageLog log) {
        Product product = productRepository.findById(log.getProductId())
                .orElseThrow(() -> new ValidationException("Product not found: " + log.getProductId()));
        return new UsageLogDto(
                log.getId(),
                log.getBarId(),
                log.getProductId(),
                product.getName(),
                log.getQuantity(),
                log.getTimestamp()
        );
    }
}