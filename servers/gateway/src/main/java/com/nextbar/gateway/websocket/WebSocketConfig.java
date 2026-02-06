package com.nextbar.gateway.websocket;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.reactive.HandlerMapping;
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping;
import org.springframework.web.reactive.socket.server.support.WebSocketHandlerAdapter;

import java.util.HashMap;
import java.util.Map;

/**
 * ============================================================
 * WebSocket Configuration for Spring Cloud Gateway
 * ============================================================
 * 
 * This configuration enables WebSocket support in the gateway.
 * Since Spring Cloud Gateway uses WebFlux (reactive), we use
 * the reactive WebSocket API instead of the traditional STOMP.
 * 
 * Endpoint: ws://localhost:8080/ws/events
 * 
 * The frontend connects to this endpoint to receive real-time
 * updates when backend services publish events.
 */
@Configuration
public class WebSocketConfig {

    /**
     * Map WebSocket handler to the /ws/events endpoint
     * 
     * Using explicit bean name and highest order to avoid conflicts
     * with Spring Cloud Gateway's SimpleUrlHandlerMapping requirements.
     * 
     * The order is set to Ordered.HIGHEST_PRECEDENCE to ensure
     * WebSocket requests are handled before other routes.
     */
    @Bean("nextbarWebSocketMapping")
    public HandlerMapping webSocketHandlerMapping(EventWebSocketHandler handler) {
        // Use HashMap to avoid immutable map issues
        Map<String, Object> urlMap = new HashMap<>();
        urlMap.put("/ws/events", handler);

        SimpleUrlHandlerMapping mapping = new SimpleUrlHandlerMapping();
        mapping.setUrlMap(urlMap);
        mapping.setOrder(Ordered.HIGHEST_PRECEDENCE); // Highest priority

        return mapping;
    }

    /**
     * Adapter that enables the reactive WebSocket handlers
     * Required for WebFlux-based WebSocket support
     */
    @Bean
    public WebSocketHandlerAdapter webSocketHandlerAdapter() {
        return new WebSocketHandlerAdapter();
    }
}
