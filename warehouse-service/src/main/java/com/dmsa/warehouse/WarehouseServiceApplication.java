package com.dmsa.warehouse;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Warehouse Service Application.
 * 
 * Core responsibilities:
 * 1. Manage beverage stock inventory
 * 2. Process and fulfill supply requests from bars
 * 3. Collect empty bottles from drop points
 */
@SpringBootApplication
public class WarehouseServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(WarehouseServiceApplication.class, args);
    }
}
