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

import com.nextbar.eventPlanner.dto.response.EventDetailResponse;
import com.nextbar.eventPlanner.event.DropPointBootstrapEvent;
import com.nextbar.eventPlanner.event.EventPublisher;
import com.nextbar.eventPlanner.event.EventStartedEvent;
import com.nextbar.eventPlanner.exception.ResourceNotFoundException;
import com.nextbar.eventPlanner.model.Bar;
import com.nextbar.eventPlanner.model.DropPoint;
import com.nextbar.eventPlanner.model.Event;
import com.nextbar.eventPlanner.model.EventStatus;
import com.nextbar.eventPlanner.repository.BarRepository;
import com.nextbar.eventPlanner.repository.DropPointRepository;
import com.nextbar.eventPlanner.repository.EventRepository;

/**
 * Unit tests for {@link EventService} focusing on the {@code startEvent(...)} method.
 *
 * These tests verify that starting an event correctly updates the event status and
 * activates associated bars and drop points. We also check that appropriate exceptions
 * are thrown when preconditions are not met (e.g., no bars or drop points).
 */

class EventServiceStartEventTest {

    private static final class TestState {
        private Event event;
        private List<Bar> bars = List.of();
        private List<DropPoint> dropPoints = List.of();
        private List<Bar> savedBars = new ArrayList<>();
        private List<DropPoint> savedDropPoints = new ArrayList<>();
    }

    private static final class NoOpEventPublisher extends EventPublisher {
        private boolean startedEventPublished;
        private boolean bootstrapEventPublished;

        private NoOpEventPublisher() {
            super(null);
        }

        @Override
        public void publishEventStarted(EventStartedEvent event) {
            startedEventPublished = true;
        }

        @Override
        public void publishDropPointBootstrap(DropPointBootstrapEvent event) {
            bootstrapEventPublished = true;
        }
    }

    private EventService createService(TestState state, NoOpEventPublisher publisher) {
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

        return new EventService(eventRepository, barRepository, dropPointRepository, publisher);
    }

    private Event buildScheduledEvent() {
        return Event.builder()
                .id(10L)
                .name("Campus Festival")
                .date(LocalDate.of(2026, 7, 1))
                .location("Campus")
                .organizerName("NextBar Team")
                .organizerEmail("team@nextbar.local")
                .status(EventStatus.SCHEDULED)
                .build();
    }

    @Test
    void startEvent_shouldFailWhenNoBarsExist() {
        TestState state = new TestState();
        state.event = buildScheduledEvent();
        state.bars = List.of();

        NoOpEventPublisher publisher = new NoOpEventPublisher();
        EventService eventService = createService(state, publisher);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> eventService.startEvent(10L));

        assertEquals("Cannot start event: at least one bar is required.", ex.getMessage());
        assertTrue(state.savedBars.isEmpty());
        assertTrue(state.savedDropPoints.isEmpty());
        assertTrue(!publisher.startedEventPublished);
        assertTrue(!publisher.bootstrapEventPublished);
    }

    @Test
    void startEvent_shouldFailWhenNoDropPointsExist() {
        TestState state = new TestState();
        state.event = buildScheduledEvent();
        state.bars = List.of(Bar.builder().id(1L).eventOccupancy(false).build());
        state.dropPoints = List.of();

        NoOpEventPublisher publisher = new NoOpEventPublisher();
        EventService eventService = createService(state, publisher);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> eventService.startEvent(10L));

        assertEquals("Cannot start event: at least one drop point is required.", ex.getMessage());
        assertTrue(state.savedBars.isEmpty());
        assertTrue(state.savedDropPoints.isEmpty());
        assertTrue(!publisher.startedEventPublished);
        assertTrue(!publisher.bootstrapEventPublished);
    }

    @Test
    void startEvent_shouldSetRunningAndActivateBarsAndDropPointsWhenBothExist() {
        TestState state = new TestState();
        state.event = buildScheduledEvent();
        state.bars = List.of(Bar.builder().id(1L).eventOccupancy(false).build());
        state.dropPoints = List.of(DropPoint.builder().id(2L).eventOccupancy(false).build());

        NoOpEventPublisher publisher = new NoOpEventPublisher();
        EventService eventService = createService(state, publisher);

        EventDetailResponse response = eventService.startEvent(10L);

        assertEquals(EventStatus.RUNNING, response.getStatus());
        assertTrue(state.savedBars.stream().allMatch(barItem -> Boolean.TRUE.equals(barItem.getEventOccupancy())));
        assertTrue(state.savedDropPoints.stream().allMatch(dp -> Boolean.TRUE.equals(dp.getEventOccupancy())));
        assertTrue(publisher.startedEventPublished);
        assertTrue(publisher.bootstrapEventPublished);
    }

    @Test
    void startEvent_shouldThrowWhenEventNotFound() {
        TestState state = new TestState();
        NoOpEventPublisher publisher = new NoOpEventPublisher();
        EventService eventService = createService(state, publisher);

        assertThrows(ResourceNotFoundException.class, () -> eventService.startEvent(999L));
    }
}
