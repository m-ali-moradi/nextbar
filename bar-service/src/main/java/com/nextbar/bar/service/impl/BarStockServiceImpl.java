package com.nextbar.bar.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import com.nextbar.bar.exception.ValidationException;
import com.nextbar.bar.model.BarStockItem;
import com.nextbar.bar.dto.response.BarStockItemDto;
import com.nextbar.bar.repository.BarRepository;
import com.nextbar.bar.repository.BarStockItemRepository;
import com.nextbar.bar.service.BarStockService;

import lombok.RequiredArgsConstructor;

/**
 * Implementation of the BarStockService interface. This service provides
 * methods to manage stock items for a bar, including adding, reducing, and
 * retrieving stock items.
 */
@Service
@RequiredArgsConstructor
public class BarStockServiceImpl implements BarStockService {

    // Injecting the BarStockItemRepository to interact with stock items
    private final BarStockItemRepository stockRepo;
    // Injecting the BarRepository to fetch bar details
    private final BarRepository barRepository;

    // Retrieves the stock items for a specific bar, converting them to DTOs
    @Override
    public List<BarStockItemDto> getStock(@NonNull UUID barId) {

        // Validate that the bar exists
        if (!barRepository.existsById(barId)) {
            throw new ValidationException("Bar not found: " + barId);
        }
        // Fetch stock items for the bar, convert to DTOs.
        return stockRepo.findByBarId(barId).stream()
                .map(this::toDto)
                .toList();
    }

    // Adds stock for a specific product in a bar, creating a new stock item if it
    // doesn't exist
    @Override
    public void addStock(@NonNull UUID barId, String productName, int quantity) {
        /*
         * Data Validation
         * 1. bar should be available to add stock for it.
         * 2. product name should be provided.
         * 3. quantity should be less than bar max capacity.
         * if these conditions are met, then add stock to the bar.
         */
        // barId should be available in the bar repository. no stock can be added
        // without a bar :)
        if (!barRepository.existsById(barId)) {
            throw new ValidationException("Bar not found: " + barId);
        }

        String normalizedProductName = normalizeProductName(productName);
        if (normalizedProductName == null) {
            throw new ValidationException("Product name is required");
        }

        // quantity plus the existing stock of all items should not exceed the bar's max
        // capacity
        if (quantity + stockRepo.findByBarId(barId).stream()
                .mapToInt(BarStockItem::getQuantity)
                .sum() > barRepository.findById(barId)
                        .orElseThrow(() -> new ValidationException("Bar not found: " + barId))
                        .getMaxCapacity()) {
            throw new ValidationException("Quantity exceeds bar max capacity");
        }

        // Add stock quantity to an existing or new stock item
        BarStockItem item = stockRepo.findByBarIdAndProductNameIgnoreCase(barId, normalizedProductName)
                .orElse(new BarStockItem(null, barId, normalizedProductName, 0, LocalDateTime.now()));
        item.setProductName(normalizedProductName);
        item.setQuantity(item.getQuantity() + quantity);
        item.setUpdatedAt(LocalDateTime.now());
        stockRepo.save(item);
    }

    /**
     * Upsert stock for a specific product in a bar, creating or updating the stock
     * item.
     * 
     * @param barId       The ID of the bar.
     * @param productName The name of the product.
     * @param quantity    The quantity to set for the product.
     */
    @Override
    public void upsertStock(@NonNull UUID barId, String productName, int quantity) {
        if (!barRepository.existsById(barId)) {
            throw new ValidationException("Bar not found: " + barId);
        }
        String normalizedProductName = normalizeProductName(productName);
        if (normalizedProductName == null) {
            throw new ValidationException("Product name is required");
        }
        if (quantity < 0) {
            throw new ValidationException("Quantity cannot be negative");
        }

        BarStockItem item = stockRepo.findByBarIdAndProductNameIgnoreCase(barId, normalizedProductName)
                .orElse(new BarStockItem(null, barId, normalizedProductName, 0, LocalDateTime.now()));
        item.setProductName(normalizedProductName);
        item.setQuantity(quantity);
        item.setUpdatedAt(LocalDateTime.now());
        stockRepo.save(item);
    }

    /**
     * Reduces stock for a specific product in a bar, ensuring sufficient stock is
     * available.
     * 
     * @param barId       The ID of the bar.
     * @param productName The name of the product.
     * @param quantity    The quantity to reduce.
     */
    @Override
    public void reduceStock(@NonNull UUID barId, String productName, int quantity) {

        /*
         * Data Validation
         * 1. bar should be available to reduce stock for it.
         * 2. product name should be provided.
         * 3. quantity should be greater than 0.
         * 4. quantity should be less than or equal to the available stock for the
         * product.
         * if these conditions are met, then reduce stock from the bar.
         */
        // check the availability of bar
        if (!barRepository.existsById(barId)) {
            throw new ValidationException("Bar not found: " + barId);
        }
        String normalizedProductName = normalizeProductName(productName);
        if (normalizedProductName == null) {
            throw new ValidationException("Product name is required");
        }
        // check if quantity is greater than 0
        if (quantity <= 0) {
            throw new ValidationException("Quantity to reduce must be greater than 0, but was: " + quantity);
        }
        // check if stock item exists for the given barId and productName
        if (!stockRepo.existsByBarIdAndProductNameIgnoreCase(barId, normalizedProductName)) {
            throw new ValidationException(
                    "No stock item found for bar ID: " + barId + " and product: " + normalizedProductName);
        }

        // Retrieve the stock item for the bar and product
        BarStockItem item = stockRepo.findByBarIdAndProductNameIgnoreCase(barId, normalizedProductName)
                .orElseThrow(() -> new ValidationException(
                        "Stock item not found for bar ID: " + barId + " and product: " + normalizedProductName));
        // Validate quantity against available stock
        if (item.getQuantity() < quantity) {
            throw new ValidationException(
                    "Insufficient stock for product: " + normalizedProductName + " in bar ID: " + barId);
        }

        // Reduce stock quantity and update timestamp
        item.setQuantity(item.getQuantity() - quantity);
        item.setUpdatedAt(LocalDateTime.now());
        stockRepo.save(item);
    }

    // Retrieves a specific stock item for a product in a bar, converting it to a
    // DTO
    @Override
    public List<BarStockItemDto> getStockByProductAndBar(@NonNull UUID barId, String productName) {

        // Validate bar and product name
        if (!barRepository.existsById(barId)) {
            throw new ValidationException("Bar not found: " + barId);
        }
        String normalizedProductName = normalizeProductName(productName);
        if (normalizedProductName == null) {
            throw new ValidationException("Product name is required");
        }

        // Retrieve the stock item for the bar and product
        BarStockItem item = stockRepo.findByBarIdAndProductNameIgnoreCase(barId, normalizedProductName)
                .orElseThrow(() -> new ValidationException(
                        "Stock item not found for bar ID: " + barId + " and product: " + normalizedProductName));

        // Convert to DTO and return
        return List.of(toDto(item));
    }

    // Checks if enough stock is available for a product in a bar
    @Override
    public boolean isStockAvailable(@NonNull UUID barId, String productName, int quantity) {
        if (!barRepository.existsById(barId)) {
            throw new ValidationException("Bar not found: " + barId);
        }
        String normalizedProductName = normalizeProductName(productName);
        if (normalizedProductName == null) {
            throw new ValidationException("Product name is required");
        }
        if (quantity <= 0) {
            throw new ValidationException("Invalid quantity: " + quantity);
        }

        // Check if stock item exists for the given barId and productName
        if (!stockRepo.existsByBarIdAndProductNameIgnoreCase(barId, normalizedProductName)) {
            throw new ValidationException(
                    "No stock item found for bar ID: " + barId + " and product: " + normalizedProductName);
        }

        // Retrieve the stock item for the bar and product
        BarStockItem item = stockRepo.findByBarIdAndProductNameIgnoreCase(barId, normalizedProductName)
                .orElseThrow(() -> new ValidationException(
                        "Stock item not found for bar ID: " + barId + " and product: " + normalizedProductName));

        // Check if enough stock is available
        return item.getQuantity() >= quantity;
    }

    // Deletes a stock item for a specific product in a bar
    @Override
    public void deleteStockItem(@NonNull UUID barId, String productName) {
        String normalizedProductName = normalizeProductName(productName);
        if (normalizedProductName == null) {
            throw new ValidationException("Product name is required");
        }

        // Find the stock item
        BarStockItem item = stockRepo.findByBarIdAndProductNameIgnoreCase(barId, normalizedProductName)
                .orElseThrow(() -> new ValidationException(
                        "Stock item not found for bar ID: " + barId + " and product: " + normalizedProductName));
        // Delete the stock item
        stockRepo.delete(java.util.Objects.requireNonNull(item));
    }

    @Override
    public void clearStock(@NonNull UUID barId) {
        if (!barRepository.existsById(barId)) {
            throw new ValidationException("Bar not found: " + barId);
        }
        stockRepo.deleteByBarId(barId);
    }

    // Resets all stock quantities for a specific bar to 0
    @Override
    public void resetStock(@NonNull UUID barId) {

        // Reset all stock quantities to 0 for the bar
        // check if bar exists and stock exists for the bar
        if (!barRepository.existsById(barId)) {
            throw new ValidationException("Bar not found: " + barId);
        }
        List<BarStockItem> items = stockRepo.findByBarId(barId);
        if (items.isEmpty()) {
            throw new ValidationException("No stock items found for bar ID: " + barId);
        }
        // Fetch all stock items for the bar and set their quantities to 0
        for (BarStockItem item : items) {
            item.setQuantity(0);
            item.setUpdatedAt(LocalDateTime.now());
        }
        stockRepo.saveAll(items);
    }

    // Converts a BarStockItem to a BarStockItemDto
    private BarStockItemDto toDto(BarStockItem item) {
        return new BarStockItemDto(
                java.util.Objects.requireNonNull(item.getId()),
                item.getBarId(),
                item.getProductName(),
                item.getQuantity());
    }

    private String normalizeProductName(String productName) {
        if (productName == null) {
            return null;
        }
        String normalized = productName.trim();
        return normalized.isBlank() ? null : normalized;
    }
}
