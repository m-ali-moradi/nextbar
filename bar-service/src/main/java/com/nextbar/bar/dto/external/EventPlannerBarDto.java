package com.nextbar.bar.dto.external;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

/**
 * DTO representing a bar for event planners, including its stock information.
 */

@Getter
@Setter
public class EventPlannerBarDto {
    private Long id;
    private String name;
    private String location;
    private Integer capacity;
    private List<EventPlannerBarStockDto> stocks;

    @Getter
    @Setter
    public static class EventPlannerBarStockDto {
        private String itemName;
        private Integer quantity;
    }
}
