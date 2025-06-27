package com.dmsa.warehouse.feign;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.dmsa.warehouse.dto.DropPointDto;

@FeignClient(name = "droppoint-service")
public interface DropPointClient {
  
    @GetMapping("/droppoints/{id}")
    DropPointDto getDropPoint(@PathVariable("id") Long id);

    @GetMapping("/droppoints")
    List<DropPointDto> getAllDropPoints();  
  
  }