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
 * EventCompletedConsumer listens for EventCompletedEvent messages and processes them to
 * reset drop points associated with the completed event and update their status.
 */

@Configuration
public class EventCompletedConsumer {

    private static final Logger logger = LoggerFactory.getLogger(EventCompletedConsumer.class);

    private final DropPointService dropPointService;
    private final EventDroppointAssociationRepository associationRepository;

    public EventCompletedConsumer(DropPointService dropPointService,
            EventDroppointAssociationRepository associationRepository) {
        this.dropPointService = dropPointService;
        this.associationRepository = associationRepository;
    }

    @Bean
    public Consumer<EventCompletedEvent> consumeEventCompletedEvent() {
        return event -> {
            if (event == null || event.getEventId() == null) {
                logger.warn("Received invalid EventCompletedEvent payload");
                return;
            }

            List<Long> dropPointIds = event.getDropPointIds();
            if (dropPointIds == null || dropPointIds.isEmpty()) {
                logger.debug("EventCompletedEvent eventId={} has no dropPointIds to reset", event.getEventId());
                return;
            }

            for (Long dropPointId : dropPointIds) {
                if (dropPointId == null) {
                    continue;
                }
                dropPointService.resetForEventCompletion(dropPointId);
            }

            List<EventDroppointAssociation> associations = associationRepository.findByEventId(event.getEventId());
            for (EventDroppointAssociation association : associations) {
                association.setEventStatus("COMPLETED");
                associationRepository.save(association);
            }

            logger.info("Processed EventCompletedEvent eventId={} dropPoints={}",
                    event.getEventId(), dropPointIds.size());
        };
    }
}
