package de.fhdo.eventPlanner.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Publisher for domain events to RabbitMQ.
 * 
 * Publishes events to:
 * 1. Spring Cloud Stream bindings (for service-to-service communication)
 * 2. WebSocket fanout exchange (for frontend real-time updates)
 */
@Component
public class EventPublisher {

    private static final Logger log = LoggerFactory.getLogger(EventPublisher.class);

    // Spring Cloud Stream bindings
    private static final String BAR_CREATED_BINDING = "barCreated-out-0";
    private static final String DROP_POINT_CREATED_BINDING = "dropPointCreated-out-0";
    private static final String STAFF_ASSIGNED_BINDING = "staffAssigned-out-0";
    private static final String EVENT_STARTED_BINDING = "eventStarted-out-0";

    // WebSocket fanout exchange
    private static final String WEBSOCKET_EVENTS_EXCHANGE = "nextbar.events";

    private final StreamBridge streamBridge;
    private final RabbitTemplate rabbitTemplate;

    public EventPublisher(StreamBridge streamBridge, RabbitTemplate rabbitTemplate) {
        this.streamBridge = streamBridge;
        this.rabbitTemplate = rabbitTemplate;
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

            // Broadcast to WebSocket
            publishToWebSocket("BAR_CREATED", event.getId().toString(), event);
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

            // Broadcast to WebSocket
            publishToWebSocket("DROP_POINT_CREATED", event.getId().toString(), event);
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

            // Broadcast to WebSocket
            publishToWebSocket("STAFF_ASSIGNED", event.getResourceId().toString(), event);
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

            // Broadcast to WebSocket
            publishToWebSocket("EVENT_STARTED", event.getEventId().toString(), event);
        } catch (Exception e) {
            log.error("Error publishing EventStartedEvent: {}", e.getMessage(), e);
        }
    }

    /**
     * Broadcast event to WebSocket clients via fanout exchange.
     */
    private void publishToWebSocket(String eventType, String resourceId, Object payload) {
        try {
            Map<String, Object> webSocketEvent = new HashMap<>();
            webSocketEvent.put("type", eventType);
            webSocketEvent.put("source", "eventplanner-service");
            webSocketEvent.put("resourceId", resourceId);
            webSocketEvent.put("payload", payload);
            webSocketEvent.put("timestamp", Instant.now().toString());

            // Fanout exchange doesn't use routing key
            rabbitTemplate.convertAndSend(WEBSOCKET_EVENTS_EXCHANGE, "", webSocketEvent);

            log.debug("Published event to WebSocket exchange: type={}, resourceId={}",
                    eventType, resourceId);
        } catch (Exception e) {
            // Don't fail main operation if WebSocket broadcast fails
            log.error("Failed to publish to WebSocket exchange: {}", e.getMessage());
        }
    }
}
