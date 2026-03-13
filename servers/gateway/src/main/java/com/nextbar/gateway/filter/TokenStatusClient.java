package com.nextbar.gateway.filter;

import java.util.Map;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Mono;

/**
 * A client for checking the revocation status of JWT access tokens by calling
 * the users-service.
 * This is used by the JwtAuthenticationFilter to implement token revocation
 * checks.
 */
@Component
public class TokenStatusClient {

    private final WebClient webClient;
    private final String internalSecret;

    public TokenStatusClient(WebClient.Builder webClientBuilder,
            @Value("${AUTH_SERVICE_BASE_URL:http://localhost:8090}") String authServiceBaseUrl,
            @Value("${security.internal.secret:}") String internalSecret) {
        this.webClient = webClientBuilder.baseUrl(Objects.requireNonNull(authServiceBaseUrl)).build();
        this.internalSecret = internalSecret == null ? "" : internalSecret.trim();
    }

    public Mono<Boolean> isRevoked(String accessToken) {
        String token = Objects.requireNonNull(accessToken);
        return webClient.post()
                .uri("/api/v1/users/token-status")
                .header("X-Internal-Service", "gateway")
                .header("X-Internal-Secret", internalSecret)
                .contentType(Objects.requireNonNull(MediaType.APPLICATION_JSON))
                .bodyValue(Objects.requireNonNull(Map.of("accessToken", token)))
                .retrieve()
                .bodyToMono(TokenStatusResponse.class)
                .map(TokenStatusResponse::revoked)
                // Fail-closed: if token-status check fails, treat token as revoked
                .onErrorResume(ex -> Mono.just(true));
    }

    private record TokenStatusResponse(boolean revoked) {
    }
}