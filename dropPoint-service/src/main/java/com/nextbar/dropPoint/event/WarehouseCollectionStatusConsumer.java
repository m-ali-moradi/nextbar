package com.nextbar.dropPoint.event;

import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.nextbar.dropPoint.service.DropPointService;

/**
 * Consumes collection status updates from warehouse-service.
 */
@Configuration
public class WarehouseCollectionStatusConsumer {

    private static final Logger logger = LoggerFactory.getLogger(WarehouseCollectionStatusConsumer.class);

    private final DropPointService dropPointService;

    public WarehouseCollectionStatusConsumer(DropPointService dropPointService) {
        this.dropPointService = dropPointService;
    }

    @Bean
    public Consumer<WarehouseCollectionStatusEvent> consumeWarehouseCollectionStatusEvent() {
        return event -> {
            if (event == null || event.getDropPointId() == null || event.getStatus() == null) {
                logger.warn("Ignoring invalid WarehouseCollectionStatusEvent payload");
                return;
            }

            String status = event.getStatus();
            if ("ACCEPTED".equalsIgnoreCase(status)) {
                dropPointService.markAcceptedByWarehouse(event.getDropPointId());
                logger.info("Drop point {} marked ACCEPTED from warehouse event", event.getDropPointId());
                return;
            }

            if ("COLLECTED".equalsIgnoreCase(status)) {
                dropPointService.markCollectedByWarehouse(event.getDropPointId());
                logger.info("Drop point {} marked EMPTY after warehouse COLLECTED event", event.getDropPointId());
                return;
            }

            logger.debug("Ignoring warehouse collection status={} for dropPointId={}",
                    status, event.getDropPointId());
        };
    }
}
