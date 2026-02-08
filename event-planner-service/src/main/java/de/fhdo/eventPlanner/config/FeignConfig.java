package de.fhdo.eventPlanner.config;

import feign.RequestInterceptor;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * OpenFeign configuration for propagating JWT tokens to downstream services.
 */
@Configuration
public class FeignConfig {

    /**
     * Interceptor to propagate the Authorization header to Feign clients.
     */
    @Bean
    public RequestInterceptor authorizationRequestInterceptor() {
        return requestTemplate -> {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder
                    .getRequestAttributes();

            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                String authorization = request.getHeader("Authorization");

                if (authorization != null) {
                    requestTemplate.header("Authorization", authorization);
                }
            }
        };
    }
}
