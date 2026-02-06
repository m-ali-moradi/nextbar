package com.nextbar.gateway.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ============================================================
 * Reactive WebSocket Handler for Real-time Events
 * ============================================================
 * 
 * This handler manages WebSocket connections from frontend clients
 * and broadcasts events received from backend services via RabbitMQ.
 * 
 * Architecture:
 * 1. Frontend connects via WebSocket to /ws/events
 * 2. Backend services publish events to RabbitMQ
 * 3. Gateway consumes events and broadcasts to all connected clients
 * 4. Clients receive events and refresh relevant data
 * 
 * Features:
 * - Automatic heartbeat (ping every 30 seconds)
 * - Connection tracking for debugging
 * - JSON serialization of events
 * - Graceful error handling
 */
@Component
public class EventWebSocketHandler implements WebSocketHandler {

    private static final Logger logger = LoggerFactory.getLogger(EventWebSocketHandler.class);

    /**
     * Sink for broadcasting events to all connected clients
     * Uses multicast to send same event to all subscribers
     */
    private final Sinks.Many<NextBarEvent> eventSink = Sinks.many().multicast().onBackpressureBuffer();

    /**
     * Track active sessions for debugging and metrics
     */
    private final Map<String, WebSocketSession> activeSessions = new ConcurrentHashMap<>();

    /**
     * JSON serializer for converting events to JSON strings
     */
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Handle new WebSocket connection
     * 
     * Each connected client will:
     * 1. Be registered in activeSessions
     * 2. Receive all broadcasted events as JSON
     * 3. Receive periodic ping messages for keep-alive
     * 4. Be removed from activeSessions on disconnect
     */
    @Override
    public Mono<Void> handle(WebSocketSession session) {
        String sessionId = session.getId();

        logger.info("🔌 WebSocket client connected: {}", sessionId);
        activeSessions.put(sessionId, session);

        // Create event stream: merges real events with periodic heartbeat
        Flux<WebSocketMessage> eventStream = eventSink.asFlux()
                .map(event -> {
                    try {
                        String json = objectMapper.writeValueAsString(event);
                        return session.textMessage(json);
                    } catch (Exception e) {
                        logger.error("Failed to serialize event: {}", e.getMessage());
                        return session.textMessage("{\"error\":\"serialization_failed\"}");
                    }
                });

        // Heartbeat stream: sends ping every 30 seconds to keep connection alive
        Flux<WebSocketMessage> heartbeat = Flux.interval(Duration.ofSeconds(30))
                .map(tick -> session.textMessage("{\"type\":\"HEARTBEAT\",\"timestamp\":\"" +
                        java.time.Instant.now().toString() + "\"}"));

        // Merge events and heartbeat into single output stream
        Flux<WebSocketMessage> outputStream = Flux.merge(eventStream, heartbeat);

        // Handle incoming messages from client (for future bidirectional use)
        Mono<Void> input = session.receive()
                .doOnNext(message -> {
                    String payload = message.getPayloadAsText();
                    logger.debug("📩 Received from {}: {}", sessionId, payload);
                    // Future: Handle client-to-server messages (subscriptions, filters, etc.)
                })
                .doOnComplete(() -> {
                    logger.info("🔌 WebSocket client disconnected: {}", sessionId);
                    activeSessions.remove(sessionId);
                })
                .doOnError(error -> {
                    logger.error("❌ WebSocket error for {}: {}", sessionId, error.getMessage());
                    activeSessions.remove(sessionId);
                })
                .then();

        // Send output stream to client
        Mono<Void> output = session.send(outputStream);

        // Run both input and output handlers concurrently
        return Mono.zip(input, output).then();
    }

    // ==================== Event Broadcasting ====================

    /**
     * Broadcast an event to ALL connected WebSocket clients
     * Called by RabbitMQ event listener when backend publishes an event
     * 
     * @param event The event to broadcast
     */
    public void broadcastEvent(NextBarEvent event) {
        int clientCount = activeSessions.size();
        logger.info("📡 Broadcasting {} to {} clients", event.getType(), clientCount);

        Sinks.EmitResult result = eventSink.tryEmitNext(event);
        if (result.isFailure()) {
            logger.warn("Failed to emit event: {}", result);
        }
    }

    /**
     * Get count of currently connected clients
     */
    public int getConnectedClientCount() {
        return activeSessions.size();
    }

    /**
     * Get IDs of all connected sessions (for debugging)
     */
    public Iterable<String> getConnectedSessionIds() {
        return activeSessions.keySet();
    }
}
