package de.fhdo.eventPlanner.service;

import de.fhdo.eventPlanner.client.WarehouseServiceClient;
import de.fhdo.eventPlanner.dto.external.WarehouseStockDto;
import de.fhdo.eventPlanner.dto.request.CreateBarStockRequest;
import de.fhdo.eventPlanner.dto.response.BarStockResponse;
import de.fhdo.eventPlanner.exception.InvalidStateTransitionException;
import de.fhdo.eventPlanner.exception.ResourceNotFoundException;
import de.fhdo.eventPlanner.model.Bar;
import de.fhdo.eventPlanner.model.BarStock;
import de.fhdo.eventPlanner.model.EventStatus;
import de.fhdo.eventPlanner.repository.BarRepository;
import de.fhdo.eventPlanner.repository.BarStockRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for managing BarStock.
 * Handles stock entries for bars with integration to WarehouseService.
 */
@Service
public class BarStockService {

    private static final Logger log = LoggerFactory.getLogger(BarStockService.class);

    private final BarStockRepository barStockRepository;
    private final BarRepository barRepository;
    private final WarehouseServiceClient warehouseServiceClient;

    public BarStockService(BarStockRepository barStockRepository,
            BarRepository barRepository,
            WarehouseServiceClient warehouseServiceClient) {
        this.barStockRepository = barStockRepository;
        this.barRepository = barRepository;
        this.warehouseServiceClient = warehouseServiceClient;
    }

    /**
     * Get all stock entries for a bar.
     */
    @Transactional(readOnly = true)
    public List<BarStockResponse> getStocksByBarId(Long barId) {
        log.debug("Fetching stocks for bar ID: {}", barId);

        // Validate bar exists
        if (!barRepository.existsById(barId)) {
            throw new ResourceNotFoundException("Bar", barId);
        }

        return barStockRepository.findByBarId(barId).stream()
                .map(BarStockResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Get stock entry by ID.
     */
    @Transactional(readOnly = true)
    public BarStockResponse getStockById(Long id) {
        log.debug("Fetching stock with ID: {}", id);
        BarStock stock = findStockById(id);
        return BarStockResponse.fromEntity(stock);
    }

    /**
     * Create a new stock entry for a bar.
     */
    @Transactional
    public BarStockResponse createStock(CreateBarStockRequest request) {
        log.info("Creating stock entry for bar ID: {}, item: {}",
                request.getBarId(), request.getItemName());

        // Validate bar exists
        Bar bar = barRepository.findById(request.getBarId())
                .orElseThrow(() -> new ResourceNotFoundException("Bar", request.getBarId()));

        // Only allow stock modification for scheduled events
        if (bar.getEvent().getStatus() != EventStatus.SCHEDULED) {
            throw new InvalidStateTransitionException(
                    "Can only add stock to bars on events in SCHEDULED status. Current status: " +
                            bar.getEvent().getStatus());
        }

        // Check for duplicate item
        if (barStockRepository.existsByBarIdAndItemName(request.getBarId(), request.getItemName())) {
            throw new IllegalArgumentException(
                    "Stock item '" + request.getItemName() + "' already exists for this bar");
        }

        BarStock stock = BarStock.builder()
                .bar(bar)
                .itemName(request.getItemName())
                .quantity(request.getQuantity())
                .build();

        stock = barStockRepository.save(stock);
        log.info("Created stock entry with ID: {}", stock.getId());

        return BarStockResponse.fromEntity(stock);
    }

    /**
     * Update stock quantity.
     */
    @Transactional
    public BarStockResponse updateStockQuantity(Long id, Integer quantity) {
        log.info("Updating stock ID: {} with quantity: {}", id, quantity);

        BarStock stock = findStockById(id);

        // Only allow stock modification for scheduled events
        if (stock.getBar().getEvent().getStatus() != EventStatus.SCHEDULED) {
            throw new InvalidStateTransitionException(
                    "Can only modify stock for bars on events in SCHEDULED status. Current status: " +
                            stock.getBar().getEvent().getStatus());
        }

        stock.setQuantity(quantity);
        stock = barStockRepository.save(stock);
        log.info("Updated stock ID: {}", stock.getId());

        return BarStockResponse.fromEntity(stock);
    }

    /**
     * Delete a stock entry.
     */
    @Transactional
    public void deleteStock(Long id) {
        log.info("Deleting stock with ID: {}", id);

        BarStock stock = findStockById(id);

        // Only allow deletion for scheduled events
        if (stock.getBar().getEvent().getStatus() != EventStatus.SCHEDULED) {
            throw new InvalidStateTransitionException(
                    "Can only delete stock for bars on events in SCHEDULED status. Current status: " +
                            stock.getBar().getEvent().getStatus());
        }

        barStockRepository.delete(stock);
        log.info("Deleted stock with ID: {}", id);
    }

    /**
     * Get available inventory from WarehouseService.
     */
    public List<WarehouseStockDto> getWarehouseInventory() {
        log.debug("Fetching inventory from WarehouseService");
        return warehouseServiceClient.getAllStock();
    }

    /**
     * Get warehouse stock by beverage type.
     */
    public WarehouseStockDto getWarehouseStockByType(String beverageType) {
        log.debug("Fetching warehouse stock for type: {}", beverageType);
        return warehouseServiceClient.getStockByType(beverageType);
    }

    /**
     * Bulk create stock entries for a bar from warehouse inventory.
     */
    @Transactional
    public List<BarStockResponse> createStocksFromWarehouse(Long barId, List<String> itemNames,
            List<Integer> quantities) {
        log.info("Creating stock entries from warehouse for bar ID: {}", barId);

        if (itemNames.size() != quantities.size()) {
            throw new IllegalArgumentException("Item names and quantities lists must have the same size");
        }

        Bar bar = barRepository.findById(barId)
                .orElseThrow(() -> new ResourceNotFoundException("Bar", barId));

        if (bar.getEvent().getStatus() != EventStatus.SCHEDULED) {
            throw new InvalidStateTransitionException(
                    "Can only add stock to bars on events in SCHEDULED status");
        }

        List<BarStock> stocks = java.util.stream.IntStream.range(0, itemNames.size())
                .mapToObj(i -> BarStock.builder()
                        .bar(bar)
                        .itemName(itemNames.get(i))
                        .quantity(quantities.get(i))
                        .build())
                .collect(Collectors.toList());

        stocks = barStockRepository.saveAll(stocks);
        log.info("Created {} stock entries for bar ID: {}", stocks.size(), barId);

        return stocks.stream()
                .map(BarStockResponse::fromEntity)
                .collect(Collectors.toList());
    }

    private BarStock findStockById(Long id) {
        return barStockRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("BarStock", id));
    }
}
