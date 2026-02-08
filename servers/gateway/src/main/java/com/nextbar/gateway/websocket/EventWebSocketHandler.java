package com.nextbar.gateway.websocket;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.socket.CloseStatus;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

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
    private static final int EVENT_BUFFER_SIZE = 1000;
    private final Sinks.Many<NextBarEvent> eventSink = Sinks.many().multicast()
            .onBackpressureBuffer(EVENT_BUFFER_SIZE, false);

    /**
     * Track active sessions for debugging and metrics
     */
    private final Map<String, ClientContext> activeSessions = new ConcurrentHashMap<>();

    /**
     * JSON serializer for converting events to JSON strings
     */
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${security.jwt.secret}")
    private String jwtSecret;

    @Value("${gateway.websocket.allow-anonymous:false}")
    private boolean allowAnonymous;

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

        ClientContext context = authenticateSession(session);
        if (context == null) {
            return session.close(new CloseStatus(4401, "Unauthorized"));
        }

        logger.info("🔌 WebSocket client connected: {}", sessionId);
        activeSessions.put(sessionId, context);

        // Create event stream: merges real events with periodic heartbeat
        Flux<WebSocketMessage> eventStream = eventSink.asFlux()
            .filter(event -> isEventAllowed(event, context))
            .onBackpressureLatest()
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

    private ClientContext authenticateSession(WebSocketSession session) {
        String token = extractToken(session);
        if (token == null || token.isBlank()) {
            if (allowAnonymous) {
                logger.warn("WebSocket auth skipped: anonymous access enabled");
                return new ClientContext(true, Set.of());
            }
            logger.warn("WebSocket auth failed: missing token");
            return null;
        }

        Claims claims;
        try {
            claims = Jwts.parser()
                    .verifyWith(Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8)))
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (Exception e) {
            logger.warn("WebSocket auth failed: invalid token");
            return null;
        }

        List<String> roles = extractStringListClaim(claims, "roles");
        boolean allowAll = roles.stream().anyMatch(role -> role.toUpperCase().contains("ADMIN")
                || role.toUpperCase().contains("WAREHOUSE"));

        Set<String> allowedResourceIds = new HashSet<>(extractStringListClaim(claims, "assignments"));
        if (allowedResourceIds.isEmpty()) {
            String barId = getQueryParam(session, "barId");
            if (barId != null && !barId.isBlank()) {
                allowedResourceIds.add(barId);
            }
        }

        if (!allowAll && allowedResourceIds.isEmpty()) {
            logger.warn("WebSocket auth failed: no resource scope");
            return null;
        }

        return new ClientContext(allowAll, allowedResourceIds);
    }

    private String extractToken(WebSocketSession session) {
        String authHeader = session.getHandshakeInfo().getHeaders().getFirst("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }

        String token = getQueryParam(session, "token");
        if (token == null || token.isBlank()) {
            token = getQueryParam(session, "access_token");
        }
        return token;
    }

    private String getQueryParam(WebSocketSession session, String key) {
        MultiValueMap<String, String> params = UriComponentsBuilder
            .fromUri(session.getHandshakeInfo().getUri())
            .build()
            .getQueryParams();
        List<String> values = params.get(key);
        return values == null || values.isEmpty() ? null : values.get(0);
    }

    private List<String> extractStringListClaim(Claims claims, String name) {
        Object claim = claims.get(name);
        if (claim instanceof List<?> list) {
            return list.stream().map(String::valueOf).collect(Collectors.toList());
        }
        return List.of();
    }

    private boolean isEventAllowed(NextBarEvent event, ClientContext context) {
        if (context.allowAll) {
            return true;
        }
        String resourceId = event.getResourceId();
        if (resourceId == null || resourceId.isBlank()) {
            return false;
        }
        return context.allowedResourceIds.contains(resourceId);
    }

    private static final class ClientContext {
        private final boolean allowAll;
        private final Set<String> allowedResourceIds;

        private ClientContext(boolean allowAll, Set<String> allowedResourceIds) {
            this.allowAll = allowAll;
            this.allowedResourceIds = allowedResourceIds;
        }
    }
}
