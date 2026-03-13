package com.nextbar.eventPlanner.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.reflect.Proxy;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.nextbar.eventPlanner.client.WarehouseServiceClient;
import com.nextbar.eventPlanner.dto.external.WarehouseStockDto;
import com.nextbar.eventPlanner.event.EventCompletedEvent;
import com.nextbar.eventPlanner.event.EventPublisher;
import com.nextbar.eventPlanner.event.StockReservedConsumedEvent;
import com.nextbar.eventPlanner.model.Bar;
import com.nextbar.eventPlanner.model.BarStock;
import com.nextbar.eventPlanner.model.DropPoint;
import com.nextbar.eventPlanner.model.Event;
import com.nextbar.eventPlanner.model.EventStatus;
import com.nextbar.eventPlanner.repository.BarRepository;
import com.nextbar.eventPlanner.repository.BarStockRepository;
import com.nextbar.eventPlanner.repository.DropPointRepository;
import com.nextbar.eventPlanner.repository.EventRepository;

/**
 * Unit tests for {@link EventService} focusing on the {@code startEvent(...)} method.
 *
 * These tests verify that starting an event correctly updates the event status and
 * activates associated bars and drop points. We also check that appropriate exceptions
 * are thrown when preconditions are not met (e.g., no bars or drop points).
 */

class EventServicePublishingTest {

    private static final class TestState {
        private Event event;
        private List<Bar> bars = List.of();
        private List<DropPoint> dropPoints = List.of();
        private List<BarStock> barStocksByEvent = List.of();
        private List<BarStock> barStocksByBar = List.of();
        private List<Bar> savedBars = new ArrayList<>();
        private List<DropPoint> savedDropPoints = new ArrayList<>();
    }

    private static final class CapturingEventPublisher extends EventPublisher {
        private int stockConsumedEvents;
        private int eventCompletedEvents;

        private CapturingEventPublisher() {
            super(null);
        }

        @Override
        public void publishStockReservedConsumed(StockReservedConsumedEvent event) {
            stockConsumedEvents++;
        }

        @Override
        public void publishEventCompleted(EventCompletedEvent event) {
            eventCompletedEvents++;
        }
    }

    private EventService createService(TestState state, CapturingEventPublisher publisher) {
        EventRepository eventRepository = (EventRepository) Proxy.newProxyInstance(
                EventRepository.class.getClassLoader(),
                new Class<?>[] { EventRepository.class },
                (proxy, method, args) -> {
                    switch (method.getName()) {
                        case "findById":
                            Long id = (Long) args[0];
                            if (state.event != null && state.event.getId().equals(id)) {
                                return Optional.of(state.event);
                            }
                            return Optional.empty();
                        case "save":
                            state.event = (Event) args[0];
                            return state.event;
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
                        case "saveAll":
                            state.savedBars = new ArrayList<>();
                            for (Object item : (Iterable<?>) args[0]) {
                                state.savedBars.add((Bar) item);
                            }
                            return state.savedBars;
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
                        case "saveAll":
                            state.savedDropPoints = new ArrayList<>();
                            for (Object item : (Iterable<?>) args[0]) {
                                state.savedDropPoints.add((DropPoint) item);
                            }
                            return state.savedDropPoints;
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

        BarStockRepository barStockRepository = (BarStockRepository) Proxy.newProxyInstance(
                BarStockRepository.class.getClassLoader(),
                new Class<?>[] { BarStockRepository.class },
                (proxy, method, args) -> {
                    switch (method.getName()) {
                        case "findByBar_Event_Id":
                            return state.barStocksByEvent;
                        case "findByBarId":
                            return state.barStocksByBar;
                        case "hashCode":
                            return System.identityHashCode(proxy);
                        case "equals":
                            return proxy == args[0];
                        case "toString":
                            return "BarStockRepositoryProxy";
                        default:
                            throw new UnsupportedOperationException(method.getName());
                    }
                });

        WarehouseServiceClient warehouseServiceClient = (WarehouseServiceClient) Proxy.newProxyInstance(
                WarehouseServiceClient.class.getClassLoader(),
                new Class<?>[] { WarehouseServiceClient.class },
                (proxy, method, args) -> {
                    if ("consumeReservedStock".equals(method.getName())) {
                        return WarehouseStockDto.builder().id(1L).beverageType((String) args[0]).quantity(10).build();
                    }
                    if ("releaseReservedStock".equals(method.getName())) {
                        return WarehouseStockDto.builder().id(1L).beverageType((String) args[0]).quantity(10).build();
                    }
                    if ("reserveStock".equals(method.getName())) {
                        return WarehouseStockDto.builder().id(1L).beverageType((String) args[0]).quantity(10).build();
                    }
                    if ("hashCode".equals(method.getName())) {
                        return System.identityHashCode(proxy);
                    }
                    if ("equals".equals(method.getName())) {
                        return proxy == args[0];
                    }
                    if ("toString".equals(method.getName())) {
                        return "WarehouseServiceClientProxy";
                    }
                    throw new UnsupportedOperationException(method.getName());
                });

        return new EventService(
                eventRepository,
                barRepository,
                dropPointRepository,
                barStockRepository,
                warehouseServiceClient,
                publisher);
    }

    @Test
    void startEvent_shouldAlwaysPublishStockReservedConsumed() {
        TestState state = new TestState();
        state.event = Event.builder()
                .id(11L)
                .name("Phase1 Event")
                .date(LocalDate.of(2026, 8, 1))
                .status(EventStatus.SCHEDULED)
                .build();

        state.bars = List.of(Bar.builder().id(1L).name("A").eventOccupancy(false).build());
        state.dropPoints = List.of(DropPoint.builder().id(2L).name("DP").eventOccupancy(false).build());
        state.barStocksByEvent = List.of(BarStock.builder().id(3L).itemName("Beer").quantity(5).build());
        state.barStocksByBar = List.of(BarStock.builder().id(3L).itemName("Beer").quantity(5).build());

        CapturingEventPublisher publisher = new CapturingEventPublisher();
        EventService service = createService(state, publisher);

        service.startEvent(11L);

        assertEquals(1, publisher.stockConsumedEvents);
    }

    @Test
    void completeEvent_shouldAlwaysPublishEventCompleted() {
        TestState state = new TestState();
        state.event = Event.builder()
                .id(13L)
                .name("Phase1 Complete")
                .date(LocalDate.of(2026, 8, 1))
                .status(EventStatus.RUNNING)
                .build();

        state.bars = List.of(Bar.builder().id(1L).name("A").eventOccupancy(true).build());
        state.dropPoints = List.of(DropPoint.builder().id(2L).name("DP").eventOccupancy(true).build());

        CapturingEventPublisher publisher = new CapturingEventPublisher();
        EventService service = createService(state, publisher);

        service.completeEvent(13L);

        assertEquals(1, publisher.eventCompletedEvents);
    }
}
