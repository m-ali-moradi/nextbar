package com.nextbar.bar;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * Main application class for the Bar service.
 * This class bootstraps the Spring Boot application and enables service discovery.
 */

@EnableDiscoveryClient
@SpringBootApplication
public class BarApplication {

	public static void main(String[] args) {
        SpringApplication.run(BarApplication.class, args);
    }
}
