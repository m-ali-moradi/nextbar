package com.nextbar.dropPoint.event;

import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.nextbar.dropPoint.service.DropPointService;

/**
 * DropPointCreatedConsumer listens for DropPointCreatedEvent events and processes them to create or update drop points in the system.
 * It ensures that new drop points are registered with a default capacity when they are created in the event planner service.
 */

@Configuration
public class DropPointCreatedConsumer {

    private static final Logger logger = LoggerFactory.getLogger(DropPointCreatedConsumer.class);
    private static final int DEFAULT_CAPACITY = 100;

    private final DropPointService dropPointService;

    public DropPointCreatedConsumer(DropPointService dropPointService) {
        this.dropPointService = dropPointService;
    }

    @Bean
    public Consumer<DropPointCreatedEvent> consumeDropPointCreatedEvent() {
        return payload -> {
            if (payload == null || payload.getId() == null) {
                logger.warn("Received invalid DropPointCreatedEvent payload");
                return;
            }

            String location = payload.getLocation() != null ? payload.getLocation() : "N/A";
            dropPointService.upsertFromBootstrapEvent(payload.getId(), location, DEFAULT_CAPACITY);

            logger.info("Processed DropPointCreatedEvent dropPointId={} eventId={}",
                    payload.getId(), payload.getEventId());
        };
    }
}
