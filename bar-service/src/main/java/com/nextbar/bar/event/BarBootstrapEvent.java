package com.nextbar.bar.event;

import java.time.Instant;
import java.util.List;
/**
 * Event representing the bootstrap of bar data.
 */
public class BarBootstrapEvent {

    private Long eventId;
    private String eventName;
    private List<BarBootstrapItem> bars;
    private Instant timestamp;

    public Long getEventId() {
        return eventId;
    }

    public void setEventId(Long eventId) {
        this.eventId = eventId;
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public List<BarBootstrapItem> getBars() {
        return bars;
    }

    public void setBars(List<BarBootstrapItem> bars) {
        this.bars = bars;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public static class BarBootstrapItem {
        private Long barId;
        private String name;
        private String location;
        private Integer capacity;
        private List<BarStockItem> stocks;

        public Long getBarId() {
            return barId;
        }

        public void setBarId(Long barId) {
            this.barId = barId;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getLocation() {
            return location;
        }

        public void setLocation(String location) {
            this.location = location;
        }

        public Integer getCapacity() {
            return capacity;
        }

        public void setCapacity(Integer capacity) {
            this.capacity = capacity;
        }

        public List<BarStockItem> getStocks() {
            return stocks;
        }

        public void setStocks(List<BarStockItem> stocks) {
            this.stocks = stocks;
        }
    }

    public static class BarStockItem {
        private String itemName;
        private Integer quantity;

        public String getItemName() {
            return itemName;
        }

        public void setItemName(String itemName) {
            this.itemName = itemName;
        }

        public Integer getQuantity() {
            return quantity;
        }

        public void setQuantity(Integer quantity) {
            this.quantity = quantity;
        }
    }
}
