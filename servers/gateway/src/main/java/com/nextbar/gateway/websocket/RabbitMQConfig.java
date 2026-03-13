package com.nextbar.gateway.websocket;

import org.springframework.amqp.core.AnonymousQueue;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * ============================================================
 * RabbitMQ Configuration for Real-time Event Consumption
 * ============================================================
 * 
 * This configuration sets up RabbitMQ infrastructure for the
 * gateway to receive events from backend services.
 * 
 * Event Flow:
 * 1. Backend services publish events to 'nextbar.events' exchange
 * 2. Gateway queue receives all events (fanout binding)
 * 3. RabbitEventListener processes events and broadcasts via WebSocket
 * 
 * Exchange: nextbar.events (fanout - broadcasts to all bound queues)
 * Queue: per-instance auto-delete queue (receives all events for WebSocket
 * broadcast)
 */
@Configuration
public class RabbitMQConfig {

    // ==================== Exchange Names ====================

    /**
     * Main exchange for all NextBar events
     * Fanout type ensures all services receive all events
     */
    public static final String EVENTS_EXCHANGE = "nextbar.events";

    // ==================== Exchange Definitions ====================

    /**
     * Fanout exchange for broadcasting events to all consumers
     * 
     * Fanout is used because:
     * - All connected WebSocket clients should receive all events
     * - No filtering/routing needed at the broker level
     * - Frontend handles filtering based on event type/resourceId
     */
    @Bean
    public FanoutExchange eventsExchange() {
        return new FanoutExchange(EVENTS_EXCHANGE, true, false);
    }

    // ==================== Queue Definitions ====================

    /**
     * Queue for the gateway to receive events
     * 
     * Uses an auto-delete, exclusive queue per instance so each
     * gateway replica gets its own copy of fanout events.
     */
    @Bean
    public Queue gatewayEventsQueue() {
        return new AnonymousQueue();
    }

    // ==================== Bindings ====================

    /**
     * Bind gateway queue to events exchange
     * 
     * With fanout exchange, all messages go to all bound queues
     * No routing key needed for fanout exchanges
     */
    @Bean
    public Binding gatewayEventsBinding(Queue gatewayEventsQueue, FanoutExchange eventsExchange) {
        return BindingBuilder
                .bind(gatewayEventsQueue)
                .to(eventsExchange);
    }

    // ==================== Message Converter ====================

    /**
     * JSON message converter for serializing/deserializing events
     * Uses Jackson for JSON processing
     */
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    /**
     * RabbitTemplate configured with JSON converter
     * Used by backend services to publish events
     */
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }
}
