package com.nextbar.bar.event;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.Transactional;

import com.nextbar.bar.model.EventBarAssociation;
import com.nextbar.bar.repository.BarRepository;
import com.nextbar.bar.repository.EventBarAssociationRepository;
import com.nextbar.bar.service.BarService;
import com.nextbar.bar.service.BarStockService;

/**
 * Event listener for handling events from the Event Planner service related to bar creation and updates.
 * the main responsibility of this class is to listen for events such as BarCreatedEvent, BarBootstrapEvent, and EventCompletedEvent,
 * and to create or update bars and their stock in response to these events. It also manages the associations between events and bars 
 * to ensure that the bar data is correctly linked to the corresponding events in the Event Planner service.
 */
@Configuration
public class EventPlannerBarEventListener {

    private static final Logger log = LoggerFactory.getLogger(EventPlannerBarEventListener.class);
    private static final int DEFAULT_BAR_CAPACITY = 100;

    private final BarRepository barRepository;
    private final EventBarAssociationRepository eventBarAssociationRepository;
    private final BarService barService;
    private final BarStockService barStockService;

    public EventPlannerBarEventListener(
            BarRepository barRepository,
            EventBarAssociationRepository eventBarAssociationRepository,
            BarService barService,
            BarStockService barStockService) {
        this.barRepository = barRepository;
        this.eventBarAssociationRepository = eventBarAssociationRepository;
        this.barService = barService;
        this.barStockService = barStockService;
    }

    /**
     * Comsume BarCreatedEvent to create a new bar or link to an existing bar based on the event payload.
     * @return
     */
    @Bean
    @Transactional
    public Consumer<BarCreatedEvent> consumeBarCreatedEvent() {
        return event -> {
            if (event == null || event.getId() == null) {
                log.warn("Received invalid BarCreatedEvent payload");
                return;
            }

            UUID repositoryBarId = Objects.requireNonNull(resolveTargetBarId(event));
            if (!barRepository.existsById(repositoryBarId)) {
                String name = event.getName() != null ? event.getName() : "EventPlannerBar-" + event.getId();
                String location = event.getLocation() != null ? event.getLocation() : "N/A";
                Integer sourceCapacity = event.getCapacity();
                int capacity = sourceCapacity != null && sourceCapacity > 0
                        ? sourceCapacity
                        : DEFAULT_BAR_CAPACITY;

                barService.registerBar(repositoryBarId, name, location, capacity);
                log.info("Created bar from BarCreatedEvent barId={} resolvedBarId={}", event.getId(), repositoryBarId);
            }

            saveAssociation(event.getEventId(), repositoryBarId, event.getEventName(), "SCHEDULED");
            log.info("Processed BarCreatedEvent barId={} eventId={}", event.getId(), event.getEventId());
        };
    }

    /**
     * Consume BarBootstrapEvent to create or update bars and their stock based on the event payload.
     * @return
     */
    @Bean
    @Transactional
    public Consumer<BarBootstrapEvent> consumeBarBootstrapEvent() {
        return event -> {
            if (event == null || event.getBars() == null || event.getBars().isEmpty()) {
                log.debug("Received empty BarBootstrapEvent payload");
                return;
            }

            int upsertedStockItems = 0;

            for (BarBootstrapEvent.BarBootstrapItem sourceBar : event.getBars()) {
                if (sourceBar == null || sourceBar.getBarId() == null) {
                    continue;
                }

                UUID barId = Objects.requireNonNull(toStableUuid(sourceBar.getBarId()));
                if (!barRepository.existsById(barId)) {
                    String name = sourceBar.getName() != null ? sourceBar.getName() : "EventPlannerBar-" + sourceBar.getBarId();
                    String location = sourceBar.getLocation() != null ? sourceBar.getLocation() : "N/A";
                    Integer sourceCapacity = sourceBar.getCapacity();
                    int capacity = sourceCapacity != null && sourceCapacity > 0 ? sourceCapacity : DEFAULT_BAR_CAPACITY;
                    barService.registerBar(barId, name, location, capacity);
                }

                try {
                    saveAssociation(event.getEventId(), barId, event.getEventName(), "RUNNING");
                } catch (RuntimeException ex) {
                    log.warn("Failed to save event-bar association eventId={} barId={} cause={}",
                            event.getEventId(), barId, ex.getMessage());
                }

                List<BarBootstrapEvent.BarStockItem> stocks = sourceBar.getStocks();
                if (stocks == null) {
                    continue;
                }

                for (BarBootstrapEvent.BarStockItem stock : stocks) {
                    if (stock == null || stock.getItemName() == null || stock.getItemName().isBlank()) {
                        continue;
                    }
                    int quantity = stock.getQuantity() != null ? Math.max(0, stock.getQuantity()) : 0;
                    try {
                        barStockService.upsertStock(barId, stock.getItemName(), quantity);
                    } catch (RuntimeException ex) {
                        log.warn("Failed to upsert stock for eventId={} barId={} item={} cause={}",
                                event.getEventId(), barId, stock.getItemName(), ex.getMessage());
                        continue;
                    }
                    upsertedStockItems++;
                }
            }

            log.info("Processed BarBootstrapEvent eventId={} bars={} stockItems={}",
                    event.getEventId(),
                    event.getBars().size(),
                    upsertedStockItems);
        };
    }

    /**
     * Consume EventCompletedEvent to clear stock and disassociate bars linked to the completed event.
     * @return
     */
    @Bean
    @Transactional
    public Consumer<EventCompletedEvent> consumeEventCompletedEvent() {
        return event -> {
            if (event == null || event.getEventId() == null) {
                log.warn("Received invalid EventCompletedEvent payload");
                return;
            }

            List<EventBarAssociation> links = eventBarAssociationRepository.findByEventId(event.getEventId());
            if (links.isEmpty()) {
                log.debug("No event-bar associations found for eventId={}", event.getEventId());
                return;
            }

            for (EventBarAssociation link : links) {
                try {
                    UUID linkedBarId = link.getBarId();
                    if (linkedBarId == null) {
                        log.warn("Skipping stock clear for null barId eventId={}", event.getEventId());
                        continue;
                    }
                    barStockService.clearStock(linkedBarId);
                } catch (Exception ex) {
                    log.warn("Failed to clear stock for barId={} eventId={} cause={}",
                            link.getBarId(), event.getEventId(), ex.getMessage());
                }

                try {
                    link.setEventStatus("COMPLETED");
                    eventBarAssociationRepository.save(link);
                } catch (Exception ex) {
                    log.warn("Failed to update event status to COMPLETED for barId={} eventId={} cause={}",
                            link.getBarId(), event.getEventId(), ex.getMessage());
                }
            }

            log.info("Processed EventCompletedEvent eventId={} completedBars={}", event.getEventId(), links.size());
        };
    }

    /**
     * Generate a stable UUID from a long source ID.
     * @param sourceId The source ID to generate UUID from.
     * @return The generated UUID.
     */
    private UUID toStableUuid(Long sourceId) {
        return UUID.nameUUIDFromBytes(("int-" + sourceId).getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Resolve the target bar ID from the BarCreatedEvent payload, supporting both new and existing resource modes.
     * @param event The BarCreatedEvent containing the payload.
     * @return The resolved UUID for the bar.
     */
    private UUID resolveTargetBarId(BarCreatedEvent event) {
        String mode = event.getResourceMode() != null
                ? event.getResourceMode().trim().toUpperCase(Locale.ROOT)
                : "NEW";

        if ("EXISTING".equals(mode) && event.getExistingResourceId() != null && !event.getExistingResourceId().isBlank()) {
            try {
                long existingLongId = Long.parseLong(event.getExistingResourceId().trim());
                return Objects.requireNonNull(toStableUuid(existingLongId));
            } catch (NumberFormatException ex) {
                log.warn("Invalid existingResourceId='{}' in BarCreatedEvent, fallback to id={}",
                        event.getExistingResourceId(), event.getId());
            }
        }

        return Objects.requireNonNull(toStableUuid(event.getId()));
    }

    /**
     * Save the association between an event and a bar if it does not already exist.
     * @param eventId The ID of the event.
     * @param barId The ID of the bar.
     */
    private void saveAssociation(Long eventId, UUID barId, String eventName, String eventStatus) {
        if (eventId == null || barId == null) {
            return;
        }
        if (eventBarAssociationRepository.existsByEventIdAndBarId(eventId, barId)) {
            return;
        }
        EventBarAssociation association = new EventBarAssociation();
        association.setEventId(eventId);
        association.setBarId(barId);
        association.setEventName(eventName != null && !eventName.isBlank() ? eventName : "N/A");
        association.setEventStatus(eventStatus != null && !eventStatus.isBlank() ? eventStatus : "UNKNOWN");
        eventBarAssociationRepository.save(association);
    }
}
