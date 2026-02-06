package com.dmsa.warehouse.client;

import com.dmsa.warehouse.dto.external.DropPointDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

/**
 * Feign client for communicating with the drop-point service.
 */
@FeignClient(name = "droppoint-service", fallbackFactory = DropPointServiceClientFallbackFactory.class)
public interface DropPointServiceClient {

    /**
     * Get all drop points.
     */
    @GetMapping("/droppoints")
    List<DropPointDto> getAllDropPoints();

    /**
     * Get a specific drop point by ID.
     */
    @GetMapping("/droppoints/{id}")
    DropPointDto getDropPoint(@PathVariable("id") Long id);
}
