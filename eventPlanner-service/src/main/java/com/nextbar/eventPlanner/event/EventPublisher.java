package com.nextbar.eventPlanner.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Component;

/**
 * Publisher for domain events to RabbitMQ.
 * This is the central place to publish all events related to event planning, such as BarCreatedEvent, DropPointCreatedEvent, StaffAssignedEvent, etc.
 * Publishes events to Spring Cloud Stream bindings for service-to-service communication.
 */
@Component
public class EventPublisher {

    private static final Logger log = LoggerFactory.getLogger(EventPublisher.class);

    // Spring Cloud Stream bindings
    private static final String BAR_CREATED_BINDING = "barCreated-out-0";
    private static final String DROP_POINT_CREATED_BINDING = "dropPointCreated-out-0";
    private static final String STAFF_ASSIGNED_BINDING = "staffAssigned-out-0";
    private static final String EVENT_STARTED_BINDING = "eventStarted-out-0";
    private static final String BAR_BOOTSTRAP_BINDING = "barBootstrap-out-0";
    private static final String DROP_POINT_BOOTSTRAP_BINDING = "dropPointBootstrap-out-0";
    private static final String EVENT_COMPLETED_BINDING = "eventCompleted-out-0";
    private static final String STOCK_RESERVED_CONSUMED_BINDING = "stockReservedConsumed-out-0";
    private static final String STAFF_RESOURCE_SYNC_BINDING = "staffResourceSync-out-0";

    private final StreamBridge streamBridge;

    public EventPublisher(StreamBridge streamBridge) {
        this.streamBridge = streamBridge;
    }

    /**
     * Publish a BarCreatedEvent.
     */
    public void publishBarCreated(BarCreatedEvent event) {
        try {
            // Publish to stream binding
            boolean sent = streamBridge.send(BAR_CREATED_BINDING, event);
            if (sent) {
                log.info("Published BarCreatedEvent: barId={}, eventId={}",
                        event.getId(), event.getEventId());
            } else {
                log.error("Failed to publish BarCreatedEvent: barId={}", event.getId());
            }

        } catch (Exception e) {
            log.error("Error publishing BarCreatedEvent: {}", e.getMessage(), e);
        }
    }

    /**
     * Publish a DropPointCreatedEvent.
     */
    public void publishDropPointCreated(DropPointCreatedEvent event) {
        try {
            // Publish to stream binding
            boolean sent = streamBridge.send(DROP_POINT_CREATED_BINDING, event);
            if (sent) {
                log.info("Published DropPointCreatedEvent: dropPointId={}, eventId={}",
                        event.getId(), event.getEventId());
            } else {
                log.error("Failed to publish DropPointCreatedEvent: dropPointId={}", event.getId());
            }

        } catch (Exception e) {
            log.error("Error publishing DropPointCreatedEvent: {}", e.getMessage(), e);
        }
    }

    /**
     * Publish a StaffAssignedEvent.
     */
    public void publishStaffAssigned(StaffAssignedEvent event) {
        try {
            // Publish to stream binding
            boolean sent = streamBridge.send(STAFF_ASSIGNED_BINDING, event);
            if (sent) {
                log.info("Published StaffAssignedEvent: staffId={}, resourceId={}, type={}",
                        event.getStaffId(), event.getResourceId(), event.getAssignmentType());
            } else {
                log.error("Failed to publish StaffAssignedEvent: staffId={}", event.getStaffId());
            }

        } catch (Exception e) {
            log.error("Error publishing StaffAssignedEvent: {}", e.getMessage(), e);
        }
    }

    /**
     * Publish an EventStartedEvent.
     */
    public void publishEventStarted(EventStartedEvent event) {
        try {
            // Publish to stream binding
            boolean sent = streamBridge.send(EVENT_STARTED_BINDING, event);
            if (sent) {
                log.info("Published EventStartedEvent: eventId={}, bars={}, dropPoints={}",
                        event.getEventId(),
                        event.getActivatedBarIds().size(),
                        event.getActivatedDropPointIds().size());
            } else {
                log.error("Failed to publish EventStartedEvent: eventId={}", event.getEventId());
            }

        } catch (Exception e) {
            log.error("Error publishing EventStartedEvent: {}", e.getMessage(), e);
        }
    }

    /**
     * Publish a DropPointBootstrapEvent.
     */
    public void publishDropPointBootstrap(DropPointBootstrapEvent event) {
        try {
            boolean sent = streamBridge.send(DROP_POINT_BOOTSTRAP_BINDING, event);
            if (sent) {
                log.info("Published DropPointBootstrapEvent: eventId={}, dropPoints={}",
                        event.getEventId(),
                        event.getDropPoints() != null ? event.getDropPoints().size() : 0);
            } else {
                log.error("Failed to publish DropPointBootstrapEvent: eventId={}", event.getEventId());
            }
        } catch (Exception e) {
            log.error("Error publishing DropPointBootstrapEvent: {}", e.getMessage(), e);
        }
    }

    /**
     * Publish a BarBootstrapEvent.
     */
    public void publishBarBootstrap(BarBootstrapEvent event) {
        try {
            boolean sent = streamBridge.send(BAR_BOOTSTRAP_BINDING, event);
            if (sent) {
                log.info("Published BarBootstrapEvent: eventId={}, bars={}",
                        event.getEventId(),
                        event.getBars() != null ? event.getBars().size() : 0);
            } else {
                log.error("Failed to publish BarBootstrapEvent: eventId={}", event.getEventId());
            }
        } catch (Exception e) {
            log.error("Error publishing BarBootstrapEvent: {}", e.getMessage(), e);
        }
    }

    public void publishEventCompleted(EventCompletedEvent event) {
        // No-op for fanout/WebSocket publishing. Kept for backward compatibility.
    }

    public void publishEventCompletedForResources(EventCompletedEvent event) {
        try {
            boolean sent = streamBridge.send(EVENT_COMPLETED_BINDING, event);
            if (sent) {
                log.info("Published EventCompletedEvent for resources: eventId={}", event.getEventId());
            } else {
                log.error("Failed to publish EventCompletedEvent for resources: eventId={}", event.getEventId());
            }
        } catch (Exception e) {
            log.error("Error publishing EventCompletedEvent for resources: {}", e.getMessage(), e);
        }
    }

    public void publishStaffResourceSync(StaffResourceSyncEvent event) {
        try {
            boolean sent = streamBridge.send(STAFF_RESOURCE_SYNC_BINDING, event);
            if (sent) {
                log.info("Published StaffResourceSyncEvent: eventId={}, action={}, assignments={}",
                        event.getEventId(),
                        event.getAction(),
                        event.getAssignments() != null ? event.getAssignments().size() : 0);
            } else {
                log.error("Failed to publish StaffResourceSyncEvent: eventId={}, action={}",
                        event.getEventId(), event.getAction());
            }
        } catch (Exception e) {
            log.error("Error publishing StaffResourceSyncEvent: {}", e.getMessage(), e);
        }
    }

    public void publishStockReservedConsumed(StockReservedConsumedEvent event) {
        try {
            boolean sent = streamBridge.send(STOCK_RESERVED_CONSUMED_BINDING, event);
            if (sent) {
                log.info("Published StockReservedConsumedEvent: eventId={}, item={}, quantity={}",
                        event.getEventId(), event.getItemName(), event.getQuantity());
            } else {
                log.error("Failed to publish StockReservedConsumedEvent: eventId={}, item={}",
                        event.getEventId(), event.getItemName());
            }

        } catch (Exception e) {
            log.error("Error publishing StockReservedConsumedEvent: {}", e.getMessage(), e);
        }
    }
}
