package de.fhdo.dropPointsSys.feign;

import de.fhdo.dropPointsSys.dto.DropPointDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@FeignClient(name="eventplanner-service", path="/events", fallback = EventPlannerFallback.class)
public interface EventPlannerClient {

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE,consumes = "application/json")
    List<DropPointDto> getDropPoints();
}
