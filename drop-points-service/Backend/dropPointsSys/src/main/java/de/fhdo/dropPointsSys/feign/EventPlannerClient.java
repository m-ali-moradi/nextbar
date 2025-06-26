package de.fhdo.dropPointsSys.feign;

import de.fhdo.dropPointsSys.dto.DropPointDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@FeignClient(name="eventplanner-service", path="/api/eventplanner")
public interface EventPlannerClient {

    @GetMapping
    public List<DropPointDto> getDropPoints();
}
