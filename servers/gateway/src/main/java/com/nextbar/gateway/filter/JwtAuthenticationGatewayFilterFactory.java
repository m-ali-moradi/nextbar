package com.nextbar.gateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.stereotype.Component;

/**
 * Factory to create JWT Authentication filters for Spring Cloud Gateway routes.
 * This allows using "JwtAuthenticationGatewayFilterFactory" in route configurations.
 */
@Component
public class JwtAuthenticationGatewayFilterFactory 
        extends AbstractGatewayFilterFactory<JwtAuthenticationGatewayFilterFactory.Config> {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public JwtAuthenticationGatewayFilterFactory(JwtAuthenticationFilter jwtAuthenticationFilter) {
        super(Config.class);
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return jwtAuthenticationFilter;
    }

    public static class Config {
        // Configuration properties can be added here if needed
    }
}
