package de.fhdo.dropPointsSys.feign;

import de.fhdo.dropPointsSys.dto.DropPointDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(name="warehouse-service", path="/warehouse/droppoints/{id}/status")
public interface WarehouseClient {

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE,consumes = "application/json")
    public DropPointDto status(Long id);
}
