package com.nextbar.bar.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.nextbar.bar.exception.ValidationException;
import com.nextbar.bar.model.UsageLog;
import com.nextbar.bar.repository.BarRepository;
import com.nextbar.bar.repository.UsageLogRepository;

/**
 * Unit tests for {@link UsageLogServiceImpl}.
 *
 * These tests focus on the "selling" behavior represented by
 * {@code logDrinkServed(...)}. We verify that valid input is stored as a usage
 * log and invalid input is rejected with clear validation errors.
 */
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("null")
class UsageLogServiceImplTest {

    @Mock
    private UsageLogRepository usageLogRepository;

    @Mock
    private BarRepository barRepository;

    @InjectMocks
    private UsageLogServiceImpl usageLogService;

    @Captor
    private ArgumentCaptor<UsageLog> usageLogCaptor;

    @Test
    @DisplayName("logDrinkServed saves a log entry for valid input")
    void logDrinkServed_savesLog_whenInputIsValid() {
        UUID barId = UUID.randomUUID();
        when(barRepository.existsById(barId)).thenReturn(true);
        when(usageLogRepository.save(any(UsageLog.class))).thenAnswer(invocation -> invocation.getArgument(0));

        usageLogService.logDrinkServed(barId, "  Cola  ", 2);

        verify(usageLogRepository).save(usageLogCaptor.capture());
        UsageLog savedLog = usageLogCaptor.getValue();
        assertEquals(barId, savedLog.getBarId());
        assertEquals("Cola", savedLog.getProductName());
        assertEquals(2, savedLog.getQuantity());
    }

    @Test
    @DisplayName("logDrinkServed throws ValidationException when bar does not exist")
    void logDrinkServed_throws_whenBarDoesNotExist() {
        UUID unknownBarId = UUID.randomUUID();
        when(barRepository.existsById(unknownBarId)).thenReturn(false);

        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> usageLogService.logDrinkServed(unknownBarId, "Cola", 1));

        assertEquals("Bar not found: " + unknownBarId, exception.getMessage());
        verify(usageLogRepository, never()).save(any());
    }

    @Test
    @DisplayName("logDrinkServed throws ValidationException when product name is blank")
    void logDrinkServed_throws_whenProductNameIsBlank() {
        UUID barId = UUID.randomUUID();
        when(barRepository.existsById(barId)).thenReturn(true);

        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> usageLogService.logDrinkServed(barId, "   ", 1));

        assertEquals("Product name is required", exception.getMessage());
        verify(usageLogRepository, never()).save(any());
    }
}
