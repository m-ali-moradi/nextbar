package com.dmsa.warehouse.service;

import com.dmsa.warehouse.dto.request.CreateStockRequest;
import com.dmsa.warehouse.dto.response.StockResponse;
import com.dmsa.warehouse.exception.InsufficientStockException;
import com.dmsa.warehouse.exception.ResourceNotFoundException;
import com.dmsa.warehouse.model.entity.BeverageStock;
import com.dmsa.warehouse.repository.BeverageStockRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for managing beverage stock inventory.
 * Handles stock queries, additions, deductions, and deletions.
 */
@Service
public class StockService {

    private static final Logger log = LoggerFactory.getLogger(StockService.class);

    private final BeverageStockRepository stockRepository;

    public StockService(BeverageStockRepository stockRepository) {
        this.stockRepository = stockRepository;
    }

    /**
     * Get all beverage stock items.
     */
    @Transactional(readOnly = true)
    public List<StockResponse> getAllStock() {
        log.debug("Fetching all beverage stock");
        return stockRepository.findAll().stream()
                .map(StockResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Get stock for a specific beverage type.
     */
    @Transactional(readOnly = true)
    public StockResponse getStockByType(String beverageType) {
        log.debug("Fetching stock for beverage type: {}", beverageType);
        BeverageStock stock = findStockByType(beverageType);
        return StockResponse.fromEntity(stock);
    }

    /**
     * Get available quantity for a beverage type.
     */
    @Transactional(readOnly = true)
    public int getAvailableQuantity(String beverageType) {
        return stockRepository.findByBeverageTypeIgnoreCase(beverageType)
                .map(BeverageStock::getQuantity)
                .orElse(0);
    }

    /**
     * Check if sufficient stock is available.
     */
    @Transactional(readOnly = true)
    public boolean hasSufficientStock(String beverageType, int quantity) {
        return getAvailableQuantity(beverageType) >= quantity;
    }

    /**
     * Create new beverage stock entry.
     */
    @Transactional
    public StockResponse createStock(CreateStockRequest request) {
        log.info("Creating new stock: type={}, quantity={}",
                request.getBeverageType(), request.getQuantity());

        // Check if beverage type already exists
        if (stockRepository.existsByBeverageTypeIgnoreCase(request.getBeverageType())) {
            throw new IllegalArgumentException(
                    "Beverage type already exists: " + request.getBeverageType());
        }

        BeverageStock stock = new BeverageStock(
                request.getBeverageType(),
                request.getQuantity());

        if (request.getMinStockLevel() != null) {
            stock.setMinStockLevel(request.getMinStockLevel());
        }

        stock = stockRepository.save(stock);
        log.info("Created stock with ID: {}", stock.getId());

        return StockResponse.fromEntity(stock);
    }

    /**
     * Deduct stock for fulfilling a supply request.
     * Uses pessimistic locking to prevent race conditions.
     * 
     * @param beverageType the beverage type
     * @param quantity     the quantity to deduct
     * @return the actual quantity deducted (may be less if partial fulfillment)
     */
    @Transactional
    public int deductStock(String beverageType, int quantity) {
        log.info("Deducting stock: type={}, quantity={}", beverageType, quantity);

        BeverageStock stock = stockRepository.findByBeverageTypeForUpdate(beverageType)
                .orElseThrow(() -> new ResourceNotFoundException("BeverageStock", beverageType));

        int available = stock.getQuantity();
        int toDeduct = Math.min(quantity, available);

        if (toDeduct == 0) {
            throw new InsufficientStockException(beverageType, quantity, available);
        }

        stock.setQuantity(available - toDeduct);
        stockRepository.save(stock);

        log.info("Deducted {} units from {}, remaining: {}",
                toDeduct, beverageType, stock.getQuantity());

        if (stock.isLowStock()) {
            log.warn("Low stock warning for {}: {} remaining",
                    beverageType, stock.getQuantity());
        }

        return toDeduct;
    }

    /**
     * Add stock (e.g., from supplier delivery).
     */
    @Transactional
    public StockResponse addStock(String beverageType, int quantity) {
        log.info("Adding stock: type={}, quantity={}", beverageType, quantity);

        BeverageStock stock = stockRepository.findByBeverageTypeForUpdate(beverageType)
                .orElseThrow(() -> new ResourceNotFoundException("BeverageStock", beverageType));

        stock.addStock(quantity);
        stock = stockRepository.save(stock);

        log.info("Added {} units to {}, new total: {}",
                quantity, beverageType, stock.getQuantity());

        return StockResponse.fromEntity(stock);
    }

    /**
     * Delete a beverage stock entry.
     */
    @Transactional
    public void deleteStock(Long id) {
        log.info("Deleting stock with ID: {}", id);

        if (!stockRepository.existsById(id)) {
            throw new ResourceNotFoundException("BeverageStock", id);
        }

        stockRepository.deleteById(id);
        log.info("Deleted stock with ID: {}", id);
    }

    /**
     * Get all items with low stock.
     */
    @Transactional(readOnly = true)
    public List<StockResponse> getLowStockItems() {
        log.debug("Fetching low stock items");
        return stockRepository.findLowStockItems().stream()
                .map(StockResponse::fromEntity)
                .collect(Collectors.toList());
    }

    private BeverageStock findStockByType(String beverageType) {
        return stockRepository.findByBeverageTypeIgnoreCase(beverageType)
                .orElseThrow(() -> new ResourceNotFoundException("BeverageStock", beverageType));
    }
}
