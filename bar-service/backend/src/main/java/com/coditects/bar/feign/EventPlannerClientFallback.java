package com.coditects.bar.feign;

import com.coditects.bar.model.dto.EventPlannerBarDto;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class EventPlannerClientFallback implements FallbackFactory<EventPlannerClient> {

    @Override
    public EventPlannerClient create(Throwable cause) {
        return new EventPlannerClient() {
            @Override
            public List<EventPlannerBarDto> fetchBarPlans() {
                System.err.println("Failed to fetch bars from eventplanner-service: " + cause.getMessage());
                return null; // fallback handler will handle null
            }
        };
    }
}

