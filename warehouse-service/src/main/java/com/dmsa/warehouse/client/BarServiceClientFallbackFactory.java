package com.dmsa.warehouse.client;

import com.dmsa.warehouse.dto.external.BarDto;
import com.dmsa.warehouse.dto.external.SupplyRequestDto;
import com.dmsa.warehouse.exception.ServiceUnavailableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/**
 * Fallback factory for BarServiceClient.
 * Provides meaningful error handling when bar-service is unavailable.
 */
@Component
public class BarServiceClientFallbackFactory implements FallbackFactory<BarServiceClient> {

    private static final Logger log = LoggerFactory.getLogger(BarServiceClientFallbackFactory.class);

    @Override
    public BarServiceClient create(Throwable cause) {
        log.error("Bar service fallback triggered: {}", cause.getMessage());

        return new BarServiceClient() {

            @Override
            public List<BarDto> getAllBars() {
                throw new ServiceUnavailableException("bar-service", cause);
            }

            @Override
            public List<SupplyRequestDto> getSupplyRequests(UUID barId) {
                throw new ServiceUnavailableException("bar-service", cause);
            }

            @Override
            public SupplyRequestDto getSupplyRequest(UUID barId, UUID requestId) {
                throw new ServiceUnavailableException("bar-service", cause);
            }

            @Override
            public ResponseEntity<Void> updateSupplyStatus(UUID barId, UUID requestId,
                    int quantity, String status) {
                throw new ServiceUnavailableException("bar-service", cause);
            }
        };
    }
}
