package com.conditects.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@SpringBootApplication
@EnableDiscoveryClient
public class GatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(GatewayApplication.class, args);
    }

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/**").allowedOrigins("http://localhost:5173");

            }
        };
    }

    // Define routes for the gateway
    @Bean
    public RouteLocator routeLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                // Route requests starting with /api/bars to bar-service
                .route("bar-service", r -> r.path("/api/bars/**")
                        .filters(f -> f.stripPrefix(1)) // Remove /api from path
                        .uri("lb://bar-service")) // lb:// uses Eureka for load balancing
                // Add routes for other services here (e.g., stock-service)
                .route("droppoint-service", r -> r.path("/api/droppoints/**")
                        .filters(f -> f.stripPrefix(1)) // Remove /api from path
                        .uri("lb://droppoint-service")) // lb:// uses Eureka for load balancing)
                // routes for other services here
                .build();
    }
}
