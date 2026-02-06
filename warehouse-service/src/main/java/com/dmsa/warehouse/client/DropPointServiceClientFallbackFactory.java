package com.dmsa.warehouse.client;

import com.dmsa.warehouse.dto.external.DropPointDto;
import com.dmsa.warehouse.exception.ServiceUnavailableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Fallback factory for DropPointServiceClient.
 * Provides meaningful error handling when drop-point service is unavailable.
 */
@Component
public class DropPointServiceClientFallbackFactory implements FallbackFactory<DropPointServiceClient> {

    private static final Logger log = LoggerFactory.getLogger(DropPointServiceClientFallbackFactory.class);

    @Override
    public DropPointServiceClient create(Throwable cause) {
        log.error("Drop point service fallback triggered: {}", cause.getMessage());

        return new DropPointServiceClient() {

            @Override
            public List<DropPointDto> getAllDropPoints() {
                throw new ServiceUnavailableException("droppoint-service", cause);
            }

            @Override
            public DropPointDto getDropPoint(Long id) {
                throw new ServiceUnavailableException("droppoint-service", cause);
            }
        };
    }
}
