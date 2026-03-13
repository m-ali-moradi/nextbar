package com.nextbar.eventPlanner.event;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Event representing that stock has been reserved for an event.
 * This event is consumed by the Event Planner Service to update the event's inventory status.
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockReservedConsumedEvent {

    private Long eventId;
    private String itemName;
    private Integer quantity;
    private Instant timestamp;

    public static StockReservedConsumedEvent from(Long eventId, String itemName, Integer quantity) {
        return StockReservedConsumedEvent.builder()
                .eventId(eventId)
                .itemName(itemName)
                .quantity(quantity)
                .timestamp(Instant.now())
                .build();
    }
}
