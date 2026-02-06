package com.nextbar.gateway;

import java.util.List;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.gateway.config.SimpleUrlHandlerMappingGlobalCorsAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

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

    @Bean
    CorsWebFilter corsWebFilter() {
        CorsConfiguration config = new CorsConfiguration();

        // must match the exact Origin from the browser (your error shows :5175)
        config.setAllowedOrigins(List.of(
                "http://localhost:5175",
                "http://localhost:5174",
                "http://localhost:5173"));

        // keep true if you send Authorization header or cookies
        config.setAllowCredentials(true);

        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setExposedHeaders(List.of("*"));

        // Allow WebSocket upgrade headers
        config.addAllowedHeader("Upgrade");
        config.addAllowedHeader("Connection");
        config.addAllowedHeader("Sec-WebSocket-Key");
        config.addAllowedHeader("Sec-WebSocket-Version");
        config.addAllowedHeader("Sec-WebSocket-Extensions");

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return new CorsWebFilter(source);
    }
}