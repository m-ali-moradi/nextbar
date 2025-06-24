package de.fhdo.dropPointsSys.feign;

import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name="warehouse-service", path="/api/warehouse")
public interface WarehouseClient {

    public String getConfirmation();
}
