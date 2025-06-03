package com.coditects.bar.feign;

import com.coditects.bar.model.Bar;

import com.coditects.bar.model.BarStockItem;
import com.coditects.bar.model.dto.BarDto;
import com.coditects.bar.model.dto.BarStockItemDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;


import java.util.List;

@FeignClient(name = "eventplanner-service", path = "/api/eventplanner")
public interface EventPlannerClient {

    @GetMapping
    List<BarDto> getAllBars();

    @GetMapping("/{barId}/stock")
    List<BarStockItemDto> getStockForBar(@PathVariable("barId") String barId);
}