package com.coditects.bar.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.coditects.bar.exception.ValidationException;
import com.coditects.bar.model.BarStockItem;
import com.coditects.bar.model.Product;
import com.coditects.bar.model.dto.BarStockItemDto;
import com.coditects.bar.repository.BarRepository;
import com.coditects.bar.repository.BarStockItemRepository;
import com.coditects.bar.repository.ProductRepository;
import com.coditects.bar.service.BarStockService;

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
    // Injecting the ProductRepository to fetch product details
    private final ProductRepository productRepository;
    // Injecting the BarRepository to fetch bar details
    private final BarRepository barRepository;


    // Retrieves the stock items for a specific bar, converting them to DTOs
    @Override
    public List<BarStockItemDto> getStock(UUID barId) {

        // Validate that the bar exists
        if (!barRepository.existsById(barId)) {
            throw new ValidationException("Bar not found: " + barId);
        }
        // Fetch stock items for the bar, convert to DTOs.
        return stockRepo.findByBarId(barId).stream()
                .map(item -> {
                    Product product = productRepository.findById(item.getProductId())
                            .orElseThrow(() -> new ValidationException("Product not found: " + item.getProductId()));
                    return toDto(item, product);
                })
                .toList();
    }

    // Adds stock for a specific product in a bar, creating a new stock item if it doesn't exist
    @Override
    public void addStock(UUID barId, UUID productId, int quantity) {
        /* 
            Data Validation
            1. bar should be available to add stock for it.
            2. product should be available to be added to the stock.
            3. quantity should be less than bar max capacity.
            if these conditions are met, then add stock to the bar.
         */
        // barId should be available in the bar repository. no stock can be added without a bar :)
        if (!barRepository.existsById(barId)) {
            throw new ValidationException("Bar not found: " + barId);
        }

        // productId should be available in the product repository
        if (!productRepository.existsById(productId)) {
            throw new ValidationException("No drink found with ID: " + productId);
        }

        // quantity plus the existing stock of all items should not exceed the bar's max capacity
        if (quantity + stockRepo.findByBarId(barId).stream()
                .mapToInt(BarStockItem::getQuantity)
                .sum() > barRepository.findById(barId)
                .orElseThrow(() -> new ValidationException("Bar not found: " + barId))
                .getMaxCapacity()) {
            throw new ValidationException("Quantity exceeds bar max capacity");
        }

        // Add stock quantity to an existing or new stock item
        BarStockItem item = stockRepo.findByBarIdAndProductId(barId, productId)
                .orElse(new BarStockItem(null, barId, productId, 0, LocalDateTime.now()));
        item.setQuantity(item.getQuantity() + quantity);
        item.setUpdatedAt(LocalDateTime.now());
        stockRepo.save(item);
    }

    // Reduces stock for a specific product in a bar, ensuring sufficient stock is available
    @Override
    public void reduceStock(UUID barId, UUID productId, int quantity) {

        /* 
            Data Validation
            1. bar should be available to reduce stock for it.
            2. product should be available to be reduced from the stock.
            3. quantity should be greater than 0.
            4. quantity should be less than or equal to the available stock for the product.
            if these conditions are met, then reduce stock from the bar.
         */
        // check the availability of bar
        if (!barRepository.existsById(barId)) {
            throw new ValidationException("Bar not found: " + barId);
        }
        // check the availability of product
        if (!productRepository.existsById(productId)) {
            throw new ValidationException("No drink found with ID: " + productId);
        }
        // check if quantity is greater than 0
        if (quantity <= 0) {
            throw new ValidationException("Quantity to reduce must be greater than 0, but was: " + quantity);
        }
        // check if stock item exists for the given barId and productId
        if (!stockRepo.existsByBarIdAndProductId(barId, productId)) {
            throw new ValidationException("No stock item found for bar ID: " + barId + " and product ID: " + productId);
        }

        // Retrieve the stock item for the bar and product
        BarStockItem item = stockRepo.findByBarIdAndProductId(barId, productId)
                .orElseThrow(() -> new ValidationException("Stock item not found for bar ID: " + barId + " and product ID: " + productId));
        // Validate quantity against available stock
        if (item.getQuantity() < quantity) {
            throw new ValidationException("Insufficient stock for product ID: " + productId + " in bar ID: " + barId);
        }

        // Reduce stock quantity and update timestamp
        item.setQuantity(item.getQuantity() - quantity);
        item.setUpdatedAt(LocalDateTime.now());
        stockRepo.save(item);
    }


    // Retrieves a specific stock item for a product in a bar, converting it to a DTO
    @Override
    public List<BarStockItemDto> getStockByProductAndBar(UUID barId, UUID productId) {

        // Validate bar and product existence
        if (!barRepository.existsById(barId)) {
            throw new ValidationException("Bar not found: " + barId);
        }
        if (!productRepository.existsById(productId)) {
            throw new ValidationException("No drink found with ID: " + productId);
        }

        // Retrieve the stock item for the bar and product
        BarStockItem item = stockRepo.findByBarIdAndProductId(barId, productId)
                .orElseThrow(() -> new ValidationException("Stock item not found for bar ID: " + barId + " and product ID: " + productId));

        // Fetch the product details
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ValidationException("Product not found: " + productId));

        // Convert to DTO and return
        return List.of(toDto(item, product));
    }
    // Checks if enough stock is available for a product in a bar
    @Override
    public boolean isStockAvailable(UUID barId, UUID productId, int quantity) {
        if (!barRepository.existsById(barId)) {
            throw new ValidationException("Bar not found: " + barId);
        }
        if (!productRepository.existsById(productId)) {
            throw new ValidationException("No drink found with ID: " + productId);
        }
        if (quantity <= 0) {
            throw new ValidationException("Invalid quantity: " + quantity);
        }

        // Check if stock item exists for the given barId and productId
        if (!stockRepo.existsByBarIdAndProductId(barId, productId)) {
            throw new ValidationException("No stock item found for bar ID: " + barId + " and product ID: " + productId);
        }

        // Retrieve the stock item for the bar and product
        BarStockItem item = stockRepo.findByBarIdAndProductId(barId, productId)
                .orElseThrow(() -> new ValidationException("Stock item not found for bar ID: " + barId + " and product ID: " + productId));

        // Check if enough stock is available
        return item.getQuantity() >= quantity;
    }

    // Deletes a stock item for a specific product in a bar
    @Override
    public void deleteStockItem(UUID barId, UUID productId) {

        // Find the stock item
        BarStockItem item = stockRepo.findByBarIdAndProductId(barId, productId)
                .orElseThrow(() -> new ValidationException("Stock item not found for bar ID: " + barId + " and product ID: " + productId));
        // Delete the stock item
        stockRepo.delete(item);
    }

    // Resets all stock quantities for a specific bar to 0
    @Override
    public void resetStock(UUID barId) {

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

    // Converts a BarStockItem to a BarStockItemDto, including product name
    private BarStockItemDto toDto(BarStockItem item, Product product) {
        return new BarStockItemDto(
                item.getId(),
                item.getBarId(),
                item.getProductId(),
                product.getName(),
                item.getQuantity()
        );
    }
}
