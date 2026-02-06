package com.dmsa.warehouse.event;

import com.dmsa.warehouse.model.entity.SupplyRequest;
import com.dmsa.warehouse.model.enums.SupplyRequestStatus;
import com.nextbar.events.SupplyRequestUpdatedEvent;
import com.nextbar.events.SupplyStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * ============================================================
 * Supply Event Publisher - Publishes events to RabbitMQ
 * ============================================================
 * 
 * This publisher sends events to two destinations:
 * 1. Spring Cloud Stream binding (supply.request.updated) - for bar-service
 * consumption
 * 2. Fanout exchange (nextbar.events) - for WebSocket broadcast to frontend
 * clients
 * 
 * The dual publishing ensures:
 * - Bar service receives type-safe stream events for processing
 * - Frontend clients receive real-time updates via WebSocket
 */
@Component
public class SupplyEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(SupplyEventPublisher.class);

    // Spring Cloud Stream binding for bar-service
    private static final String SUPPLY_REQUEST_UPDATED_BINDING = "supplyRequestUpdated-out-0";

    // Fanout exchange for WebSocket broadcast (gateway consumes this)
    private static final String WEBSOCKET_EVENTS_EXCHANGE = "nextbar.events";

    private final StreamBridge streamBridge;
    private final RabbitTemplate rabbitTemplate;

    public SupplyEventPublisher(StreamBridge streamBridge, RabbitTemplate rabbitTemplate) {
        this.streamBridge = streamBridge;
        this.rabbitTemplate = rabbitTemplate;
    }

    /**
     * Publishes a SupplyRequestUpdatedEvent when warehouse updates a supply
     * request.
     * 
     * This method:
     * 1. Sends to Spring Cloud Stream (for bar-service)
     * 2. Broadcasts to WebSocket fanout exchange (for frontend clients)
     *
     * @param request the updated supply request
     */
    public void publishSupplyRequestUpdated(SupplyRequest request) {
        SupplyStatus eventStatus = mapStatus(request.getStatus());

        // Get fulfilled quantity from first item (simplification for now)
        int fulfilledQuantity = request.getItems().isEmpty() ? 0
                : request.getItems().get(0).getQuantity();

        // ============================================================
        // 1. Publish to Spring Cloud Stream (bar-service consumption)
        // ============================================================
        var event = new SupplyRequestUpdatedEvent(
                request.getId(),
                request.getBarId(),
                eventStatus,
                request.getRejectionReason(),
                fulfilledQuantity,
                LocalDateTime.now());

        boolean sent = streamBridge.send(SUPPLY_REQUEST_UPDATED_BINDING, event);
        if (sent) {
            log.info("Published SupplyRequestUpdatedEvent to stream: requestId={}, status={}",
                    request.getId(), eventStatus);
        } else {
            log.error("Failed to publish SupplyRequestUpdatedEvent to stream: requestId={}", request.getId());
        }

        // ============================================================
        // 2. Broadcast to WebSocket fanout exchange (frontend clients)
        // ============================================================
        publishToWebSocket(request, eventStatus);
    }

    /**
     * Broadcasts event to WebSocket clients via fanout exchange.
     * 
     * The gateway consumes from this exchange and pushes to all
     * connected WebSocket clients.
     */
    private void publishToWebSocket(SupplyRequest request, SupplyStatus status) {
        try {
            // Build event payload for frontend
            Map<String, Object> payload = new HashMap<>();
            payload.put("requestId", request.getId().toString());
            payload.put("barId", request.getBarId().toString());
            payload.put("barName", request.getBarName());
            payload.put("status", status.name());
            payload.put("rejectionReason", request.getRejectionReason());

            // Build NextBarEvent structure expected by gateway
            Map<String, Object> webSocketEvent = new HashMap<>();
            webSocketEvent.put("type", "SUPPLY_REQUEST_UPDATED");
            webSocketEvent.put("source", "warehouse-service");
            webSocketEvent.put("resourceId", request.getBarId().toString());
            webSocketEvent.put("payload", payload);
            webSocketEvent.put("timestamp", Instant.now().toString());

            // Fanout exchange doesn't use routing key (empty string)
            rabbitTemplate.convertAndSend(WEBSOCKET_EVENTS_EXCHANGE, "", webSocketEvent);

            log.debug("Published event to WebSocket exchange: type=SUPPLY_REQUEST_UPDATED, barId={}",
                    request.getBarId());
        } catch (Exception e) {
            // Don't fail the main operation if WebSocket broadcast fails
            log.error("Failed to publish to WebSocket exchange: {}", e.getMessage());
        }
    }

    private SupplyStatus mapStatus(SupplyRequestStatus status) {
        return switch (status) {
            case REQUESTED -> SupplyStatus.PENDING;
            case IN_PROGRESS -> SupplyStatus.IN_PROGRESS;
            case DELIVERED -> SupplyStatus.DELIVERED;
            case REJECTED -> SupplyStatus.REJECTED;
        };
    }
}
