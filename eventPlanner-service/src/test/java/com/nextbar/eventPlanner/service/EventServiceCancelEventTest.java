package com.nextbar.eventPlanner.service;

import java.lang.reflect.Proxy;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import com.nextbar.eventPlanner.dto.response.EventResponse;
import com.nextbar.eventPlanner.event.EventPublisher;
import com.nextbar.eventPlanner.exception.InvalidStateTransitionException;
import com.nextbar.eventPlanner.exception.ResourceNotFoundException;
import com.nextbar.eventPlanner.model.Bar;
import com.nextbar.eventPlanner.model.DropPoint;
import com.nextbar.eventPlanner.model.Event;
import com.nextbar.eventPlanner.model.EventStatus;
import com.nextbar.eventPlanner.repository.BarRepository;
import com.nextbar.eventPlanner.repository.DropPointRepository;
import com.nextbar.eventPlanner.repository.EventRepository;

/**
 * Unit tests for {@link EventService} focusing on the {@code cancelEvent(...)} method.
 *
 * These tests verify that canceling an event correctly updates the event status and
 * deactivates associated bars and drop points if the event was previously running.
 * We also check that appropriate exceptions are thrown when preconditions are not met
 * (e.g., event not found, invalid state transition).
 */

class EventServiceCancelEventTest {

    private static final class TestState {
        private Event event;
        private List<Bar> bars = List.of();
        private List<DropPoint> dropPoints = List.of();
        private List<Bar> savedBars = new ArrayList<>();
        private List<DropPoint> savedDropPoints = new ArrayList<>();
    }

    private static final class NoOpEventPublisher extends EventPublisher {
        private NoOpEventPublisher() {
            super(null);
        }
    }

    private EventService createService(TestState state) {
        EventRepository eventRepository = (EventRepository) Proxy.newProxyInstance(
                EventRepository.class.getClassLoader(),
                new Class<?>[] { EventRepository.class },
                (proxy, method, args) -> {
                    switch (method.getName()) {
                        case "findById": {
                            Long id = (Long) args[0];
                            if (state.event != null && state.event.getId().equals(id)) {
                                return Optional.of(state.event);
                            }
                            return Optional.empty();
                        }
                        case "save": {
                            state.event = (Event) args[0];
                            return state.event;
                        }
                        case "hashCode":
                            return System.identityHashCode(proxy);
                        case "equals":
                            return proxy == args[0];
                        case "toString":
                            return "EventRepositoryProxy";
                        default:
                            throw new UnsupportedOperationException(method.getName());
                    }
                });

        BarRepository barRepository = (BarRepository) Proxy.newProxyInstance(
                BarRepository.class.getClassLoader(),
                new Class<?>[] { BarRepository.class },
                (proxy, method, args) -> {
                    switch (method.getName()) {
                        case "findByEventId":
                            return state.bars;
                        case "saveAll": {
                            state.savedBars = new ArrayList<>();
                            for (Object item : (Iterable<?>) args[0]) {
                                state.savedBars.add((Bar) item);
                            }
                            return state.savedBars;
                        }
                        case "hashCode":
                            return System.identityHashCode(proxy);
                        case "equals":
                            return proxy == args[0];
                        case "toString":
                            return "BarRepositoryProxy";
                        default:
                            throw new UnsupportedOperationException(method.getName());
                    }
                });

        DropPointRepository dropPointRepository = (DropPointRepository) Proxy.newProxyInstance(
                DropPointRepository.class.getClassLoader(),
                new Class<?>[] { DropPointRepository.class },
                (proxy, method, args) -> {
                    switch (method.getName()) {
                        case "findByEventId":
                            return state.dropPoints;
                        case "saveAll": {
                            state.savedDropPoints = new ArrayList<>();
                            for (Object item : (Iterable<?>) args[0]) {
                                state.savedDropPoints.add((DropPoint) item);
                            }
                            return state.savedDropPoints;
                        }
                        case "hashCode":
                            return System.identityHashCode(proxy);
                        case "equals":
                            return proxy == args[0];
                        case "toString":
                            return "DropPointRepositoryProxy";
                        default:
                            throw new UnsupportedOperationException(method.getName());
                    }
                });

        return new EventService(eventRepository, barRepository, dropPointRepository, new NoOpEventPublisher());
    }

    private Event buildEvent(EventStatus status) {
        return Event.builder()
                .id(20L)
                .name("Summer Event")
                .date(LocalDate.of(2026, 8, 1))
                .location("FH Campus")
                .organizerName("NextBar Team")
                .organizerEmail("team@nextbar.local")
                .status(status)
                .build();
    }

    @Test
    void cancelEvent_shouldDeactivateBarsAndDropPointsWhenPreviousStatusWasRunning() {
        TestState state = new TestState();
        state.event = buildEvent(EventStatus.RUNNING);
        state.bars = List.of(Bar.builder().id(1L).eventOccupancy(true).build());
        state.dropPoints = List.of(DropPoint.builder().id(2L).eventOccupancy(true).build());

        EventService eventService = createService(state);

        EventResponse response = eventService.cancelEvent(20L);

        assertEquals(EventStatus.CANCELLED, response.getStatus());
        assertTrue(state.savedBars.stream().allMatch(bar -> Boolean.FALSE.equals(bar.getEventOccupancy())));
        assertTrue(state.savedDropPoints.stream().allMatch(dp -> Boolean.FALSE.equals(dp.getEventOccupancy())));
    }

    @Test
    void cancelEvent_shouldSetCancelledWithoutDeactivationWhenScheduled() {
        TestState state = new TestState();
        state.event = buildEvent(EventStatus.SCHEDULED);

        EventService eventService = createService(state);

        EventResponse response = eventService.cancelEvent(20L);

        assertEquals(EventStatus.CANCELLED, response.getStatus());
        assertTrue(state.savedBars.isEmpty());
        assertTrue(state.savedDropPoints.isEmpty());
    }

    @Test
    void cancelEvent_shouldRejectCompletedEvent() {
        TestState state = new TestState();
        state.event = buildEvent(EventStatus.COMPLETED);

        EventService eventService = createService(state);

        assertThrows(InvalidStateTransitionException.class, () -> eventService.cancelEvent(20L));
    }

    @Test
    void cancelEvent_shouldThrowWhenEventNotFound() {
        TestState state = new TestState();
        EventService eventService = createService(state);

        assertThrows(ResourceNotFoundException.class, () -> eventService.cancelEvent(999L));
    }
}
