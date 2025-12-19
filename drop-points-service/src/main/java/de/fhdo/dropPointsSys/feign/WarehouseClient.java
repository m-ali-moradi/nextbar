package de.fhdo.dropPointsSys.feign;

import de.fhdo.dropPointsSys.dto.DropPointDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "warehouse-service", path = "/warehouse/droppoints", fallback = WarehouseFallback.class)
@Component
public interface WarehouseClient {

    @GetMapping(value = "/{id}/status", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    DropPointDto status(@PathVariable("id") Long id);
}

