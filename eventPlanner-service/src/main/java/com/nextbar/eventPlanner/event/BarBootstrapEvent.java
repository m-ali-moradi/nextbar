package com.nextbar.eventPlanner.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

/**
 * Event representing the initial setup of bars for an event, including their details and stock information.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BarBootstrapEvent {

    private Long eventId;
    private String eventName;
    private List<BarBootstrapItem> bars;
    private Instant timestamp;

    public static BarBootstrapEvent from(Long eventId, String eventName, List<BarBootstrapItem> bars) {
        return BarBootstrapEvent.builder()
                .eventId(eventId)
                .eventName(eventName)
                .bars(bars)
                .timestamp(Instant.now())
                .build();
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BarBootstrapItem {
        private Long barId;
        private String name;
        private String location;
        private Integer capacity;
        private List<BarStockItem> stocks;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BarStockItem {
        private String itemName;
        private Integer quantity;
    }
}
