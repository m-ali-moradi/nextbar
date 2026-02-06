package com.nextbar.bar.event;

import com.nextbar.bar.model.dto.SupplyRequestDto;
import com.nextbar.events.SupplyRequestCreatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

/**
 * Publishes supply request events to RabbitMQ.
 * When a bar creates a supply request, this publisher notifies the warehouse.
 */
@Component
public class SupplyEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(SupplyEventPublisher.class);
    private static final String SUPPLY_REQUEST_CREATED_BINDING = "supplyRequestCreated-out-0";

    private final StreamBridge streamBridge;

    public SupplyEventPublisher(StreamBridge streamBridge) {
        this.streamBridge = streamBridge;
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
    }
}
