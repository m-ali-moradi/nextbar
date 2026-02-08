package de.fhdo.eventPlanner;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * Main application class for the Event Planner Service.
 * 
 * This service manages events, bars, bar stocks, and drop points for
 * the NextBar system. It integrates with:
 * - UserService: For fetching staff lists
 * - WarehouseService: For fetching inventory items
 * - RabbitMQ: For publishing domain events
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
public class EventPlannerApplication {

	public static void main(String[] args) {
		SpringApplication.run(EventPlannerApplication.class, args);
	}
}
