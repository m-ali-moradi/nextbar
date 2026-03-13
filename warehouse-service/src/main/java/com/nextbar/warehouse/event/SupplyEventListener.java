package com.nextbar.warehouse.event;

import com.nextbar.warehouse.model.entity.SupplyRequest;
import com.nextbar.warehouse.model.entity.SupplyRequestItem;
import com.nextbar.warehouse.model.enums.SupplyRequestStatus;
import com.nextbar.warehouse.repository.SupplyRequestRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.Transactional;

import java.util.function.Consumer;

/**
 * Listens for supply request events from bar-service via RabbitMQ.
 * When a bar creates a supply request, this listener stores it locally.
 */
@Configuration
public class SupplyEventListener {

    private static final Logger log = LoggerFactory.getLogger(SupplyEventListener.class);

    private final SupplyRequestRepository supplyRequestRepository;

    public SupplyEventListener(SupplyRequestRepository supplyRequestRepository) {
        this.supplyRequestRepository = supplyRequestRepository;
    }

    /**
     * Consumer function for SupplyRequestCreatedEvent.
     * Creates a local copy of the supply request for warehouse processing.
     */
    @Bean
    @Transactional
    public Consumer<SupplyRequestCreatedEvent> supplyRequestCreated() {
        return event -> {
            log.info("Received SupplyRequestCreatedEvent: requestId={}, barId={}, barName={}",
                    event.getRequestId(), event.getBarId(), event.getBarName());

            // Check if already exists (idempotency)
            if (supplyRequestRepository.existsById(event.getRequestId())) {
                log.warn("Supply request {} already exists, skipping", event.getRequestId());
                return;
            }

            // Create local supply request entity
            SupplyRequest request = new SupplyRequest();
            request.setId(event.getRequestId());
            request.setBarId(event.getBarId());
            request.setBarName(event.getBarName());
            request.setStatus(SupplyRequestStatus.REQUESTED);
            request.setCreatedAt(event.getCreatedAt());

            // Add items
            if (event.getItems() != null) {
                for (SupplyRequestCreatedEvent.SupplyItem item : event.getItems()) {
                    SupplyRequestItem requestItem = new SupplyRequestItem(
                            item.getProductId(),
                            item.getProductName(),
                            item.getQuantity());
                    request.addItem(requestItem);
                }
            }

            supplyRequestRepository.save(request);
            log.info("Created local supply request: {} for bar {}",
                    event.getRequestId(), event.getBarName());
        };
    }
}
