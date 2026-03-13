package com.nextbar.dropPoint.service;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.nextbar.dropPoint.domain.DropPoint;
import com.nextbar.dropPoint.domain.DropPointStatus;
import com.nextbar.dropPoint.event.DropPointCollectionEventPublisher;
import com.nextbar.dropPoint.exception.ValidationException;
import com.nextbar.dropPoint.repositories.DropPointRepository;

/**
 * Unit tests for {@link DropPointService}.
 *
 * These tests cover the core behaviors of adding empties, notifying the warehouse,
 * and removing empties. We verify that state transitions occur correctly and that
 * invalid operations are rejected with clear validation errors.
 */

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("null")
class DropPointServiceTest {

    @Mock
    private DropPointRepository dropPointRepository;

    @Mock
    private DropPointCollectionEventPublisher collectionEventPublisher;

    @Test
    void addEmpty_shouldSetFullWhenReachingCapacity() {
        DropPoint dropPoint = new DropPoint();
        dropPoint.setId(10L);
        dropPoint.setCapacity(5);
        dropPoint.setCurrent_empties_stock(4);
        dropPoint.setStatus(DropPointStatus.EMPTY);

        when(dropPointRepository.findById(10L)).thenReturn(Optional.of(dropPoint));
        when(dropPointRepository.save(any(DropPoint.class))).thenAnswer(invocation -> invocation.getArgument(0));

        DropPointService service = new DropPointService(dropPointRepository, collectionEventPublisher);

        Optional<DropPoint> result = service.addEmpty(10L);

        assertTrue(result.isPresent());
        assertEquals(5, result.get().getCurrent_empties_stock());
        assertEquals(DropPointStatus.FULL, result.get().getStatus());
    }

    @Test
    void addEmpty_shouldRemainFullWhenAlreadyAtCapacity() {
        DropPoint dropPoint = new DropPoint();
        dropPoint.setId(11L);
        dropPoint.setCapacity(3);
        dropPoint.setCurrent_empties_stock(3);
        dropPoint.setStatus(DropPointStatus.FULL);

        when(dropPointRepository.findById(11L)).thenReturn(Optional.of(dropPoint));
        when(dropPointRepository.save(any(DropPoint.class))).thenAnswer(invocation -> invocation.getArgument(0));

        DropPointService service = new DropPointService(dropPointRepository, collectionEventPublisher);

        Optional<DropPoint> result = service.addEmpty(11L);

        assertTrue(result.isPresent());
        assertEquals(3, result.get().getCurrent_empties_stock());
        assertEquals(DropPointStatus.FULL, result.get().getStatus());
    }

    @Test
    void notifyWarehouse_shouldThrowWhenDropPointIsNotFull() {
        DropPoint dropPoint = new DropPoint();
        dropPoint.setId(21L);
        dropPoint.setStatus(DropPointStatus.EMPTY);

        when(dropPointRepository.findById(21L)).thenReturn(Optional.of(dropPoint));

        DropPointService service = new DropPointService(dropPointRepository, collectionEventPublisher);

        ValidationException exception = assertThrows(ValidationException.class, () -> service.notifyWarehouse(21L));

        assertEquals("Drop point must be FULL before warehouse notification", exception.getMessage());
        verify(dropPointRepository, never()).save(any(DropPoint.class));
        verify(collectionEventPublisher, never()).publishCollectionStatusChanged(any(DropPoint.class));
    }

    @Test
    void notifyWarehouse_shouldSetNotifiedAndPublishEvent_whenDropPointIsFull() {
        DropPoint dropPoint = new DropPoint();
        dropPoint.setId(22L);
        dropPoint.setStatus(DropPointStatus.FULL);

        when(dropPointRepository.findById(22L)).thenReturn(Optional.of(dropPoint));
        when(dropPointRepository.save(any(DropPoint.class))).thenAnswer(invocation -> invocation.getArgument(0));

        DropPointService service = new DropPointService(dropPointRepository, collectionEventPublisher);

        Optional<DropPoint> result = service.notifyWarehouse(22L);

        assertTrue(result.isPresent());
        assertEquals(DropPointStatus.NOTIFIED, result.get().getStatus());
        verify(collectionEventPublisher).publishCollectionStatusChanged(result.get());
    }

    @Test
    void removeEmpties_shouldResetStockSetEmptyAndPublishEvent() {
        DropPoint dropPoint = new DropPoint();
        dropPoint.setId(30L);
        dropPoint.setCurrent_empties_stock(8);
        dropPoint.setStatus(DropPointStatus.FULL);

        when(dropPointRepository.findById(30L)).thenReturn(Optional.of(dropPoint));
        when(dropPointRepository.save(any(DropPoint.class))).thenAnswer(invocation -> invocation.getArgument(0));

        DropPointService service = new DropPointService(dropPointRepository, collectionEventPublisher);

        Optional<DropPoint> result = service.removeEmpties(30L);

        assertTrue(result.isPresent());
        assertEquals(0, result.get().getCurrent_empties_stock());
        assertEquals(DropPointStatus.EMPTY, result.get().getStatus());
        verify(collectionEventPublisher).publishCollectionStatusChanged(result.get());
    }
}
