package com.nextbar.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.gateway.config.SimpleUrlHandlerMappingGlobalCorsAutoConfiguration;

/**
 * Gateway Application
 * 
 * Excludes SimpleUrlHandlerMappingGlobalCorsAutoConfiguration because:
 * 1. We have a custom CorsWebFilter that handles CORS
 * 2. The auto-config conflicts with our WebSocket SimpleUrlHandlerMapping
 */
@SpringBootApplication(exclude = { SimpleUrlHandlerMappingGlobalCorsAutoConfiguration.class })
@EnableDiscoveryClient
public class GatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(GatewayApplication.class, args);
    }
}