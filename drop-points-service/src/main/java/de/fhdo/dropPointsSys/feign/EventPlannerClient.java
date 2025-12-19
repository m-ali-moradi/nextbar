package de.fhdo.dropPointsSys.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@FeignClient(name="eventplanner-service", path="/api/events/drop-point-plan",fallback = EventPlannerFallback.class)
@Component
public interface EventPlannerClient {

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE,consumes = "application/json")
    List<?> fetchDropPoints();
}
