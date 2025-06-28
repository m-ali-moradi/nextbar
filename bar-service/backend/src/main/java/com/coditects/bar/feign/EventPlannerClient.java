package com.coditects.bar.feign;


import com.coditects.bar.model.dto.BarStockItemDto;
import com.coditects.bar.model.dto.EventPlannerBarDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;


import java.util.List;

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

