package com.dmsa.warehouse.config;

import java.util.Arrays;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.dmsa.warehouse.security.JwtAuthenticationFilter;

/**
 * Security configuration for warehouse-service.
 * Configures JWT validation, CORS, and endpoint security with role-based
 * access.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

        private final JwtAuthenticationFilter jwtAuthenticationFilter;

        public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
                this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        }

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
                http
                                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                                .csrf(AbstractHttpConfigurer::disable)
                                .sessionManagement(session -> session
                                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                                .authorizeHttpRequests(auth -> auth
                                                // Public endpoints
                                                .requestMatchers(
                                                                "/actuator/**",
                                                                "/h2-console/**",
                                                                "/swagger-ui/**",
                                                                "/swagger-ui.html",
                                                                "/v3/api-docs/**",
                                                                "/api-docs/**")
                                                .permitAll()

                                                // Stock management - managers can write, operators can read
                                                .requestMatchers(HttpMethod.GET, "/warehouse/stock/**").hasAnyRole(
                                                                "ADMIN", "WAREHOUSE_MANAGER", "WAREHOUSE_OPERATOR")
                                                .requestMatchers(HttpMethod.POST, "/warehouse/stock/**").hasAnyRole(
                                                                "ADMIN", "WAREHOUSE_MANAGER")
                                                .requestMatchers(HttpMethod.PUT, "/warehouse/stock/**").hasAnyRole(
                                                                "ADMIN", "WAREHOUSE_MANAGER")
                                                .requestMatchers(HttpMethod.DELETE, "/warehouse/stock/**").hasAnyRole(
                                                                "ADMIN", "WAREHOUSE_MANAGER")

                                                // Supply requests - both can view and process
                                                .requestMatchers("/warehouse/bars/**").hasAnyRole(
                                                                "ADMIN", "WAREHOUSE_MANAGER", "WAREHOUSE_OPERATOR")
                                                .requestMatchers("/warehouse/supply/**").hasAnyRole(
                                                                "ADMIN", "WAREHOUSE_MANAGER", "WAREHOUSE_OPERATOR")

                                                // Collections - operators can view pending, managers can manage all
                                                .requestMatchers(HttpMethod.GET, "/warehouse/collections/**")
                                                .hasAnyRole(
                                                                "ADMIN", "WAREHOUSE_MANAGER", "WAREHOUSE_OPERATOR")
                                                .requestMatchers(HttpMethod.POST, "/warehouse/collections/**")
                                                .hasAnyRole(
                                                                "ADMIN", "WAREHOUSE_MANAGER", "WAREHOUSE_OPERATOR")
                                                .requestMatchers(HttpMethod.PUT, "/warehouse/collections/**")
                                                .hasAnyRole(
                                                                "ADMIN", "WAREHOUSE_MANAGER", "WAREHOUSE_OPERATOR")

                                                // Legacy endpoints
                                                .requestMatchers("/warehouse/droppoints/**").hasAnyRole(
                                                                "ADMIN", "WAREHOUSE_MANAGER", "WAREHOUSE_OPERATOR")
                                                .requestMatchers("/warehouse/empties/**").hasAnyRole(
                                                                "ADMIN", "WAREHOUSE_MANAGER", "WAREHOUSE_OPERATOR")

                                                // All other warehouse endpoints require authentication
                                                .anyRequest().authenticated())
                                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                                // Allow H2 console frames
                                .headers(headers -> headers.frameOptions(frame -> frame.disable()));

                return http.build();
        }

        @Bean
        public CorsConfigurationSource corsConfigurationSource() {
                CorsConfiguration configuration = new CorsConfiguration();
                configuration.setAllowedOrigins(List.of(
                                "http://localhost:5173",
                                "http://localhost:5174",
                                "http://localhost:5175",
                                "http://localhost:3000",
                                "http://localhost:8080"));
                configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
                configuration.setAllowedHeaders(List.of("*"));
                configuration.setAllowCredentials(true);
                configuration.setMaxAge(3600L);

                UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
                source.registerCorsConfiguration("/**", configuration);
                return source;
        }
}
