package com.coditects.bar.feign;
import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import com.coditects.bar.model.dto.EventPlannerBarDto;

/**
 * Feign client interface for interacting with the Event Planner service.
 * This client fetches bar plans from the Event Planner service.
 */
@FeignClient(name = "eventplanner-service", path = "/api/events/bar-plan", fallback =  EventPlannerClientFallback.class)
public interface EventPlannerClient {

    // Fetches all bars and their stock items from the Event Planner service
    @GetMapping
    List<EventPlannerBarDto> fetchBarPlans();
}

