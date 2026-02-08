package com.nextbar.gateway.websocket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * ============================================================
 * RabbitMQ Event Listener - Bridges RabbitMQ to WebSocket
 * ============================================================
 * 
 * This listener receives events from RabbitMQ and broadcasts
 * them to all connected WebSocket clients via the handler.
 * 
 * Event Flow:
 * 1. Backend service calls rabbitTemplate.convertAndSend("nextbar.events", "",
 * event)
 * 2. This listener receives the event from gateway.events.queue
 * 3. Event is passed to EventWebSocketHandler.broadcastEvent()
 * 4. All connected WebSocket clients receive the event
 * 
 * The listener automatically handles:
 * - JSON deserialization (via Jackson converter in RabbitMQConfig)
 * - Error handling and retry
 * - Connection management
 */
@Component
public class RabbitEventListener {

    private static final Logger logger = LoggerFactory.getLogger(RabbitEventListener.class);

    private final EventWebSocketHandler webSocketHandler;

    /**
     * Constructor injection of WebSocket handler
     * The handler broadcasts events to connected clients
     */
    public RabbitEventListener(EventWebSocketHandler webSocketHandler) {
        this.webSocketHandler = webSocketHandler;
    }

    /**
    * Listen for events on the gateway events queue
     * 
     * @RabbitListener automatically:
     *                 - Connects to RabbitMQ
     *                 - Consumes messages from the specified queue
     *                 - Deserializes JSON to NextBarEvent object
     *                 - Acknowledges messages after processing
     * 
     * @param event The event received from RabbitMQ
     */
    @RabbitListener(queues = "#{@gatewayEventsQueue.name}")
    public void handleEvent(NextBarEvent event) {
        logger.info("📨 Received event from RabbitMQ: type={}, source={}, resourceId={}",
                event.getType(),
                event.getSource(),
                event.getResourceId());

        try {
            // Broadcast event to all connected WebSocket clients
            webSocketHandler.broadcastEvent(event);

            logger.debug("✅ Event broadcasted to {} clients",
                    webSocketHandler.getConnectedClientCount());
        } catch (Exception e) {
            logger.error("❌ Failed to broadcast event: {}", e.getMessage(), e);
            // Don't rethrow - we don't want to requeue failed broadcasts
        }
    }
}
