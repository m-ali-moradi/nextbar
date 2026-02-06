package com.nextbar.bar.event;

import com.nextbar.bar.model.SupplyStatus;
import com.nextbar.bar.repository.SupplyRequestRepository;
import com.nextbar.events.SupplyRequestUpdatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.Transactional;

import java.util.function.Consumer;

/**
 * Listens for supply request updates from warehouse-service via RabbitMQ.
 * Updates the local supply request status when warehouse changes it.
 */
@Configuration
public class SupplyEventListener {

    private static final Logger log = LoggerFactory.getLogger(SupplyEventListener.class);

    private final SupplyRequestRepository supplyRequestRepository;

    public SupplyEventListener(SupplyRequestRepository supplyRequestRepository) {
        this.supplyRequestRepository = supplyRequestRepository;
    }

    /**
     * Consumer function for SupplyRequestUpdatedEvent.
     * Spring Cloud Stream will bind this to the 'supplyRequestUpdated-in-0'
     * binding.
     */
    @Bean
    @Transactional
    public Consumer<SupplyRequestUpdatedEvent> supplyRequestUpdated() {
        return event -> {
            log.info("Received SupplyRequestUpdatedEvent: requestId={}, status={}",
                    event.getRequestId(), event.getStatus());

            supplyRequestRepository.findById(event.getRequestId())
                    .ifPresentOrElse(
                            request -> {
                                // Map the event status to local enum
                                SupplyStatus newStatus = mapStatus(event.getStatus());
                                request.setStatus(newStatus);
                                supplyRequestRepository.save(request);
                                log.info("Updated supply request {} to status {}",
                                        event.getRequestId(), newStatus);
                            },
                            () -> log.warn("Supply request not found: {}", event.getRequestId()));
        };
    }

    private SupplyStatus mapStatus(com.nextbar.events.SupplyStatus eventStatus) {
        return switch (eventStatus) {
            case PENDING -> SupplyStatus.REQUESTED;
            case IN_PROGRESS -> SupplyStatus.IN_PROGRESS;
            case DELIVERED -> SupplyStatus.DELIVERED;
            case REJECTED -> SupplyStatus.REJECTED;
            case COMPLETED -> SupplyStatus.COMPLETED;
        };
    }
}
