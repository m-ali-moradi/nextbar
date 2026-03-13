package com.nextbar.bar.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.nextbar.bar.dto.response.SupplyItemDto;
import com.nextbar.bar.dto.response.SupplyRequestDto;
import com.nextbar.bar.event.SupplyEventPublisher;
import com.nextbar.bar.exception.ValidationException;
import com.nextbar.bar.model.Bar;
import com.nextbar.bar.model.SupplyRequest;
import com.nextbar.bar.model.SupplyStatus;
import com.nextbar.bar.repository.BarRepository;
import com.nextbar.bar.repository.SupplyRequestRepository;

/**
 * Unit tests for {@link SupplyRequestServiceImpl}.
 *
 * These tests intentionally cover a small, easy-to-read subset of behavior:
 * successful creation, quantity validation, and resilience when event
 * publishing fails.
 */
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("null")
class SupplyRequestServiceImplTest {

    @Mock
    private SupplyRequestRepository supplyRequestRepository;

    @Mock
    private BarRepository barRepository;

    @Mock
    private SupplyEventPublisher supplyEventPublisher;

    @InjectMocks
    private SupplyRequestServiceImpl supplyRequestService;

    @Captor
    private ArgumentCaptor<SupplyRequest> supplyRequestCaptor;

    @Test
    @DisplayName("createRequest saves request and publishes event for valid input")
    void createRequest_savesAndPublishes_whenInputIsValid() {
        UUID barId = UUID.randomUUID();
        List<SupplyItemDto> items = List.of(new SupplyItemDto("  Beer  ", 5));
        Bar bar = new Bar();
        bar.setId(barId);
        bar.setName("Main Bar");

        when(barRepository.existsById(barId)).thenReturn(true);
        when(barRepository.findById(barId)).thenReturn(Optional.of(bar));
        when(supplyRequestRepository.save(any(SupplyRequest.class))).thenAnswer(invocation -> invocation.getArgument(0));

        SupplyRequestDto result = supplyRequestService.createRequest(barId, items);

        verify(supplyRequestRepository).save(supplyRequestCaptor.capture());
        SupplyRequest savedRequest = supplyRequestCaptor.getValue();
        assertEquals(barId, savedRequest.getBarId());
        assertEquals(SupplyStatus.REQUESTED, savedRequest.getStatus());
        assertEquals("Beer", savedRequest.getItems().get(0).getProductName());
        assertEquals(5, savedRequest.getItems().get(0).getQuantity());

        assertEquals(barId, result.barId());
        assertEquals(SupplyStatus.REQUESTED, result.status());
        verify(supplyEventPublisher).publishSupplyRequestCreated(result, "Main Bar");
    }

    @Test
    @DisplayName("createRequest throws ValidationException when quantity is out of range")
    void createRequest_throws_whenQuantityIsOutOfRange() {
        UUID barId = UUID.randomUUID();
        List<SupplyItemDto> items = List.of(new SupplyItemDto("Beer", 0));
        when(barRepository.existsById(barId)).thenReturn(true);

        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> supplyRequestService.createRequest(barId, items));

        assertEquals("Supply quantity must be between 1 and 50, but was 0 for product 'Beer'", exception.getMessage());
        verify(supplyRequestRepository, never()).save(any());
    }

    @Test
    @DisplayName("createRequest still succeeds when event publishing fails")
    void createRequest_stillReturnsSavedRequest_whenPublishFails() {
        UUID barId = UUID.randomUUID();
        List<SupplyItemDto> items = List.of(new SupplyItemDto("Water", 2));
        Bar bar = new Bar();
        bar.setId(barId);
        bar.setName("Main Bar");

        when(barRepository.existsById(barId)).thenReturn(true);
        when(barRepository.findById(barId)).thenReturn(Optional.of(bar));
        when(supplyRequestRepository.save(any(SupplyRequest.class))).thenAnswer(invocation -> invocation.getArgument(0));
        doThrow(new RuntimeException("broker unavailable"))
                .when(supplyEventPublisher)
                .publishSupplyRequestCreated(any(SupplyRequestDto.class), eq("Main Bar"));

        SupplyRequestDto result = supplyRequestService.createRequest(barId, items);

        assertEquals(barId, result.barId());
        assertEquals(SupplyStatus.REQUESTED, result.status());
        verify(supplyRequestRepository).save(any(SupplyRequest.class));
    }
}
