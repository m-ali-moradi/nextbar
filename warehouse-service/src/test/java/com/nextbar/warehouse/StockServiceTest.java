package com.nextbar.warehouse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.nextbar.warehouse.dto.request.CreateStockRequest;
import com.nextbar.warehouse.exception.InsufficientStockException;
import com.nextbar.warehouse.exception.ResourceNotFoundException;
import com.nextbar.warehouse.model.entity.BeverageStock;
import com.nextbar.warehouse.repository.BeverageStockRepository;

/**
 * Unit tests for {@link StockService}.
 *
 * <p>
 * These tests cover core inventory rules that are business-critical:
 * duplicate prevention, partial fulfillment during deduction, and strict
 * validation when stock cannot be fulfilled.
 * </p>
 */
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("null")
class StockServiceTest {

    @Mock
    private BeverageStockRepository stockRepository;

    @InjectMocks
    private StockService stockService;

    @Captor
    private ArgumentCaptor<BeverageStock> stockCaptor;

    @Test
    @DisplayName("createStock persists a new beverage stock when type is not present")
    void createStock_savesEntity_whenBeverageTypeIsNew() {
        CreateStockRequest request = new CreateStockRequest();
        request.setBeverageType("Cola");
        request.setQuantity(100);
        request.setMinStockLevel(20);

        when(stockRepository.existsByBeverageTypeIgnoreCase("Cola")).thenReturn(false);
        when(stockRepository.save(any(BeverageStock.class))).thenAnswer(invocation -> {
            BeverageStock saved = invocation.getArgument(0);
            saved.setId(1L);
            return saved;
        });

        var response = stockService.createStock(request);

        verify(stockRepository).save(stockCaptor.capture());
        BeverageStock persisted = stockCaptor.getValue();
        assertEquals("Cola", persisted.getBeverageType());
        assertEquals(100, persisted.getQuantity());
        assertEquals(20, persisted.getMinStockLevel());
        assertEquals("Cola", response.getBeverageType());
        assertEquals(100, response.getQuantity());
    }

    @Test
    @DisplayName("createStock rejects duplicate beverage type")
    void createStock_throws_whenBeverageTypeAlreadyExists() {
        CreateStockRequest request = new CreateStockRequest();
        request.setBeverageType("Cola");
        request.setQuantity(10);

        when(stockRepository.existsByBeverageTypeIgnoreCase("Cola")).thenReturn(true);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> stockService.createStock(request));

        assertTrue(exception.getMessage().contains("Beverage type already exists"));
        verify(stockRepository, never()).save(any(BeverageStock.class));
    }

    @Test
    @DisplayName("deductStock allows partial fulfillment when requested exceeds available")
    void deductStock_returnsAvailableQuantity_whenRequestExceedsAvailable() {
        BeverageStock stock = new BeverageStock("Cola", 4);
        stock.setMinStockLevel(1);

        when(stockRepository.findByBeverageTypeForUpdate("Cola")).thenReturn(Optional.of(stock));
        when(stockRepository.save(any(BeverageStock.class))).thenAnswer(invocation -> invocation.getArgument(0));

        int deducted = stockService.deductStock("Cola", 7);

        assertEquals(4, deducted);
        assertEquals(0, stock.getQuantity());
        verify(stockRepository).save(stock);
    }

    @Test
    @DisplayName("deductStock throws when beverage type does not exist")
    void deductStock_throws_whenBeverageTypeNotFound() {
        when(stockRepository.findByBeverageTypeForUpdate("Unknown")).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> stockService.deductStock("Unknown", 2));

        assertTrue(exception.getMessage().contains("BeverageStock not found"));
        verify(stockRepository, never()).save(any(BeverageStock.class));
    }

    @Test
    @DisplayName("deductStock throws InsufficientStockException when available quantity is zero")
    void deductStock_throws_whenNoStockAvailable() {
        BeverageStock stock = new BeverageStock("Water", 0);
        when(stockRepository.findByBeverageTypeForUpdate("Water")).thenReturn(Optional.of(stock));

        InsufficientStockException exception = assertThrows(
                InsufficientStockException.class,
                () -> stockService.deductStock("Water", 3));

        assertEquals("Water", exception.getBeverageType());
        assertEquals(3, exception.getRequestedQuantity());
        assertEquals(0, exception.getAvailableQuantity());
        verify(stockRepository, never()).save(any(BeverageStock.class));
    }
}
