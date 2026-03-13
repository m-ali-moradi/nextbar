package com.nextbar.dropPoint.event;

import java.util.List;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.nextbar.dropPoint.domain.EventDroppointAssociation;
import com.nextbar.dropPoint.repositories.EventDroppointAssociationRepository;
import com.nextbar.dropPoint.service.DropPointService;

/**
 * DropPointBootstrapConsumer listens for DropPointBootstrapEvent messages and processes them to
 * initialize or update drop points in the system based on the event data.
 */
@Configuration
public class DropPointBootstrapConsumer {

    private static final Logger logger = LoggerFactory.getLogger(DropPointBootstrapConsumer.class);

    private final DropPointService dropPointService;
    private final EventDroppointAssociationRepository associationRepository;

    public DropPointBootstrapConsumer(DropPointService dropPointService,
            EventDroppointAssociationRepository associationRepository) {
        this.dropPointService = dropPointService;
        this.associationRepository = associationRepository;
    }

    @Bean
    public Consumer<DropPointBootstrapEvent> consumeDropPointBootstrap() {
        return payload -> {
            if (payload == null || payload.getDropPoints() == null || payload.getDropPoints().isEmpty()) {
                logger.warn("Received empty DropPointBootstrapEvent payload");
                return;
            }

            List<DropPointBootstrapEvent.DropPointBootstrapItem> items = payload.getDropPoints();
            for (DropPointBootstrapEvent.DropPointBootstrapItem item : items) {
                dropPointService.upsertFromBootstrapEvent(item.getDropPointId(), item.getLocation(), item.getCapacity());

                if (payload.getEventId() != null && item.getDropPointId() != null
                        && !associationRepository.existsByEventIdAndDropPointId(payload.getEventId(), item.getDropPointId())) {
                    EventDroppointAssociation association = new EventDroppointAssociation();
                    association.setEventId(payload.getEventId());
                    association.setDropPointId(item.getDropPointId());
                    association.setEventName(payload.getEventName() != null ? payload.getEventName() : "N/A");
                    association.setEventStatus("RUNNING");
                    associationRepository.save(association);
                }
            }

            logger.info("Processed DropPointBootstrapEvent for eventId={} with {} drop points",
                    payload.getEventId(), items.size());
        };
    }
}