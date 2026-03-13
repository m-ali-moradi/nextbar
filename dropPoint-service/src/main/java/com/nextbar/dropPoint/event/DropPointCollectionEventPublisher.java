package com.nextbar.dropPoint.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Component;

import com.nextbar.dropPoint.domain.DropPoint;

/**
 * DropPointCollectionEventPublisher is responsible for publishing events related to drop point collection status changes.
 */
@Component
public class DropPointCollectionEventPublisher {

    private static final Logger logger = LoggerFactory.getLogger(DropPointCollectionEventPublisher.class);
    private static final String COLLECTION_STATUS_OUT_BINDING = "dropPointCollectionEvent-out-0";

    private final StreamBridge streamBridge;

    public DropPointCollectionEventPublisher(StreamBridge streamBridge) {
        this.streamBridge = streamBridge;
    }

    public void publishCollectionStatusChanged(DropPoint dropPoint) {
        DropPointCollectionStatusEvent event = DropPointCollectionStatusEvent.from(
                dropPoint.getId(),
                dropPoint.getLocation(),
                dropPoint.getCurrent_empties_stock(),
                dropPoint.getStatus());

        boolean sent = streamBridge.send(COLLECTION_STATUS_OUT_BINDING, event);
        if (sent) {
            logger.info("Published DropPointCollectionStatusEvent dropPointId={} status={} empties={}",
                    event.getDropPointId(), event.getStatus(), event.getBottleCount());
        } else {
            logger.error("Failed to publish DropPointCollectionStatusEvent for dropPointId={}", event.getDropPointId());
        }
    }
}
