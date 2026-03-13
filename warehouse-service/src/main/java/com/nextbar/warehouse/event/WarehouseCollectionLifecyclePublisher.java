package com.nextbar.warehouse.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Component;

@Component
public class WarehouseCollectionLifecyclePublisher {

    private static final Logger log = LoggerFactory.getLogger(WarehouseCollectionLifecyclePublisher.class);
    private static final String COLLECTION_LIFECYCLE_OUT_BINDING = "dropPointCollectionLifecycle-out-0";

    private final StreamBridge streamBridge;

    public WarehouseCollectionLifecyclePublisher(StreamBridge streamBridge) {
        this.streamBridge = streamBridge;
    }

    public void publishAccepted(Long dropPointId) {
        publish(dropPointId, "ACCEPTED");
    }

    public void publishCollected(Long dropPointId) {
        publish(dropPointId, "COLLECTED");
    }

    private void publish(Long dropPointId, String status) {
        WarehouseCollectionLifecycleEvent event = WarehouseCollectionLifecycleEvent.of(dropPointId, status);
        boolean sent = streamBridge.send(COLLECTION_LIFECYCLE_OUT_BINDING, event);
        if (sent) {
            log.info("Published warehouse collection lifecycle status={} dropPointId={}", status, dropPointId);
        } else {
            log.error("Failed to publish warehouse collection lifecycle status={} dropPointId={}", status, dropPointId);
        }
    }
}
