package com.nextbar.warehouse.config;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
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

import com.nextbar.warehouse.security.InternalRequestVerificationFilter;
import com.nextbar.warehouse.security.JwtAuthenticationFilter;

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
        private final InternalRequestVerificationFilter internalRequestVerificationFilter;

        @Value("${CORS_ALLOWED_ORIGINS:http://localhost:5173}")
        private String corsAllowedOrigins;

        public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter,
                        InternalRequestVerificationFilter internalRequestVerificationFilter) {
                this.jwtAuthenticationFilter = jwtAuthenticationFilter;
                this.internalRequestVerificationFilter = internalRequestVerificationFilter;
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
                                                                "/actuator/health",
                                                                "/actuator/prometheus",
                                                                "/actuator/info",
                                                                "/h2-console/**",
                                                                "/swagger-ui/**",
                                                                "/swagger-ui.html",
                                                                "/v3/api-docs/**",
                                                                "/api-docs/**")
                                                .permitAll()

                                                .requestMatchers("/actuator/**").hasAuthority("ADMIN")

                                                // Stock management - managers can write, operators can read
                                                .requestMatchers(HttpMethod.GET, "/api/v1/warehouse/stock/**")
                                                .hasAnyRole(
                                                                "ADMIN", "WAREHOUSE_MANAGER", "WAREHOUSE_OPERATOR")
                                                .requestMatchers(HttpMethod.POST, "/api/v1/warehouse/stock/**")
                                                .hasAnyRole(
                                                                "ADMIN", "WAREHOUSE_MANAGER")
                                                .requestMatchers(HttpMethod.PUT, "/api/v1/warehouse/stock/**")
                                                .hasAnyRole(
                                                                "ADMIN", "WAREHOUSE_MANAGER")
                                                .requestMatchers(HttpMethod.DELETE, "/api/v1/warehouse/stock/**")
                                                .hasAnyRole(
                                                                "ADMIN", "WAREHOUSE_MANAGER")

                                                // Supply requests - both can view and process
                                                .requestMatchers("/api/v1/warehouse/bars/**").hasAnyRole(
                                                                "ADMIN", "WAREHOUSE_MANAGER", "WAREHOUSE_OPERATOR")
                                                .requestMatchers("/api/v1/warehouse/supply/**").hasAnyRole(
                                                                "ADMIN", "WAREHOUSE_MANAGER", "WAREHOUSE_OPERATOR")

                                                // Collections - operators can view pending, managers can manage all
                                                .requestMatchers(HttpMethod.GET, "/api/v1/warehouse/collections/**")
                                                .hasAnyRole(
                                                                "ADMIN", "WAREHOUSE_MANAGER", "WAREHOUSE_OPERATOR")
                                                .requestMatchers(HttpMethod.POST, "/api/v1/warehouse/collections/**")
                                                .hasAnyRole(
                                                                "ADMIN", "WAREHOUSE_MANAGER", "WAREHOUSE_OPERATOR")
                                                .requestMatchers(HttpMethod.PUT, "/api/v1/warehouse/collections/**")
                                                .hasAnyRole(
                                                                "ADMIN", "WAREHOUSE_MANAGER", "WAREHOUSE_OPERATOR")

                                                // Legacy endpoints
                                                .requestMatchers("/api/v1/warehouse/droppoints/**").hasAnyRole(
                                                                "ADMIN", "WAREHOUSE_MANAGER", "WAREHOUSE_OPERATOR")
                                                .requestMatchers("/api/v1/warehouse/empties/**").hasAnyRole(
                                                                "ADMIN", "WAREHOUSE_MANAGER", "WAREHOUSE_OPERATOR")

                                                // All other warehouse endpoints require authentication
                                                .anyRequest().authenticated())
                                .addFilterBefore(internalRequestVerificationFilter,
                                                UsernamePasswordAuthenticationFilter.class)
                                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                                // Allow H2 console frames
                                .headers(headers -> headers.frameOptions(frame -> frame.disable()));

                return http.build();
        }

        @Bean
        public CorsConfigurationSource corsConfigurationSource() {
                CorsConfiguration configuration = new CorsConfiguration();
                configuration.setAllowedOrigins(parseAllowedOrigins());
                configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
                configuration.setAllowedHeaders(List.of("*"));
                configuration.setAllowCredentials(true);
                configuration.setMaxAge(3600L);

                UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
                source.registerCorsConfiguration("/**", configuration);
                return source;
        }

        private List<String> parseAllowedOrigins() {
                return List.of(corsAllowedOrigins.split(","))
                                .stream()
                                .map(String::trim)
                                .filter(origin -> !origin.isEmpty())
                                .collect(Collectors.toList());
        }

        /**
         * Prevent Spring Boot from auto-registering the JwtAuthenticationFilter as a
         * servlet filter.
         * It must ONLY run inside the Spring Security filter chain.
         */
        @Bean
        public FilterRegistrationBean<JwtAuthenticationFilter> jwtFilterRegistration(
                        JwtAuthenticationFilter filter) {
                FilterRegistrationBean<JwtAuthenticationFilter> reg = new FilterRegistrationBean<>(filter);
                reg.setEnabled(false);
                return reg;
        }

        @Bean
        public FilterRegistrationBean<InternalRequestVerificationFilter> internalFilterRegistration(
                        InternalRequestVerificationFilter filter) {
                FilterRegistrationBean<InternalRequestVerificationFilter> reg = new FilterRegistrationBean<>(filter);
                reg.setEnabled(false);
                return reg;
        }
}
