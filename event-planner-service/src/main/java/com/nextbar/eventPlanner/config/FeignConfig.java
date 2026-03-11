package de.fhdo.eventPlanner.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import feign.RequestInterceptor;
import jakarta.servlet.http.HttpServletRequest;

/**
 * OpenFeign configuration for propagating JWT tokens to downstream services.
 */
@Configuration
public class FeignConfig {

    private static final String HEADER_AUTHORIZATION = "Authorization";
    private static final String HEADER_USER_ID = "X-User-Id";
    private static final String HEADER_ROLES = "X-Roles";
    private static final String HEADER_ASSIGNMENTS = "X-Assignments";

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
                propagateHeader(requestTemplate, request, HEADER_AUTHORIZATION);
                propagateHeader(requestTemplate, request, HEADER_USER_ID);
                propagateHeader(requestTemplate, request, HEADER_ROLES);
                propagateHeader(requestTemplate, request, HEADER_ASSIGNMENTS);
            }
        };
    }

    private void propagateHeader(feign.RequestTemplate template, HttpServletRequest request, String headerName) {
        String headerValue = request.getHeader(headerName);
        if (headerValue != null && !headerValue.isBlank()) {
            template.header(headerName, headerValue);
        }
    }
}
