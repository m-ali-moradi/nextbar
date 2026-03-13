package com.nextbar.warehouse.event;

import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.nextbar.warehouse.EmptyBottleCollectionService;

@Configuration
public class DropPointCollectionEventListener {

    private static final Logger log = LoggerFactory.getLogger(DropPointCollectionEventListener.class);

    private final EmptyBottleCollectionService emptyBottleCollectionService;

    public DropPointCollectionEventListener(EmptyBottleCollectionService emptyBottleCollectionService) {
        this.emptyBottleCollectionService = emptyBottleCollectionService;
    }

    @Bean
    public Consumer<DropPointCollectionStatusEvent> consumeDropPointCollectionEvent() {
        return event -> {
            if (event == null || event.getDropPointId() == null || event.getStatus() == null) {
                log.warn("Received invalid DropPointCollectionStatusEvent payload: {}", event);
                return;
            }

            log.info("Received DropPointCollectionStatusEvent dropPointId={} status={} empties={}",
                    event.getDropPointId(), event.getStatus(), event.getBottleCount());

            emptyBottleCollectionService.handleDropPointStatusEvent(event);
        };
    }
}
