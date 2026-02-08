package com.nextbar.bar.event;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Component;

import com.nextbar.bar.model.dto.SupplyRequestDto;
import com.nextbar.events.SupplyRequestCreatedEvent;

/**
 * Publishes supply request events to RabbitMQ.
 * When a bar creates a supply request, this publisher notifies the warehouse.
 */
@Component
public class SupplyEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(SupplyEventPublisher.class);
    private static final String SUPPLY_REQUEST_CREATED_BINDING = "supplyRequestCreated-out-0";
    private static final String WEBSOCKET_EVENTS_EXCHANGE = "nextbar.events";

    private final StreamBridge streamBridge;
    private final RabbitTemplate rabbitTemplate;

    public SupplyEventPublisher(StreamBridge streamBridge, RabbitTemplate rabbitTemplate) {
        this.streamBridge = streamBridge;
        this.rabbitTemplate = rabbitTemplate;
    }

    /**
     * Publishes a SupplyRequestCreatedEvent when a bar creates a new supply
     * request.
     *
     * @param request the created supply request DTO
     * @param barName the name of the bar (for display in warehouse)
     */
    public void publishSupplyRequestCreated(SupplyRequestDto request, String barName) {
        var items = request.items().stream()
                .map(item -> new SupplyRequestCreatedEvent.SupplyItem(
                        item.productId(),
                        item.productName(),
                        item.quantity()))
                .collect(Collectors.toList());

        var event = new SupplyRequestCreatedEvent(
                request.id(),
                request.barId(),
                barName,
                items,
                request.createdAt());

        boolean sent = streamBridge.send(SUPPLY_REQUEST_CREATED_BINDING, event);
        if (sent) {
            log.info("Published SupplyRequestCreatedEvent: requestId={}, barId={}",
                    request.id(), request.barId());
        } else {
            log.error("Failed to publish SupplyRequestCreatedEvent: requestId={}", request.id());
        }

        publishToWebSocket(request, barName);
    }

    private void publishToWebSocket(SupplyRequestDto request, String barName) {
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("requestId", request.id().toString());
            payload.put("barId", request.barId().toString());
            payload.put("barName", barName);
            payload.put("items", request.items());
            payload.put("createdAt", request.createdAt());

            Map<String, Object> webSocketEvent = new HashMap<>();
            webSocketEvent.put("type", "SUPPLY_REQUEST_CREATED");
            webSocketEvent.put("source", "bar-service");
            webSocketEvent.put("resourceId", request.barId().toString());
            webSocketEvent.put("payload", payload);
            webSocketEvent.put("timestamp", Instant.now().toString());

            rabbitTemplate.convertAndSend(WEBSOCKET_EVENTS_EXCHANGE, "", webSocketEvent);
            log.debug("Published event to WebSocket exchange: type=SUPPLY_REQUEST_CREATED, barId={}",
                    request.barId());
        } catch (Exception e) {
            log.error("Failed to publish to WebSocket exchange: {}", e.getMessage());
        }
    }
}
