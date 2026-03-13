package com.nextbar.usersservice.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nextbar.usersservice.dto.LoginRequestDTO;
import com.nextbar.usersservice.dto.LoginResponseDTO;
import com.nextbar.usersservice.dto.TokenStatusRequestDTO;
import com.nextbar.usersservice.dto.TokenStatusResponseDTO;
import com.nextbar.usersservice.dto.WebSocketTicketResponseDTO;
import com.nextbar.usersservice.service.TooManyLoginAttemptsException;
import com.nextbar.usersservice.service.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Controller for handling authentication-related endpoints such as login,
 * token refresh, logout, and token status checking.
 */
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class AuthController {

    private static final String REFRESH_COOKIE_NAME = "refreshToken";
    private static final String INTERNAL_SERVICE_HEADER = "X-Internal-Service";
    private static final String INTERNAL_SECRET_HEADER = "X-Internal-Secret";

    private final UserService userService;

    @Value("${security.refresh-cookie.secure:false}")
    private boolean refreshCookieSecure;

    @Value("${security.refresh-cookie.same-site:Lax}")
    private String refreshCookieSameSite;

    @Value("${security.jwt.refresh-expiration:1209600000}")
    private long refreshCookieMaxAgeMs;

    @Value("${security.internal.secret:}")
    private String internalSecret;

    /**
     * Login endpoint that accepts a LoginRequestDTO and returns a LoginResponseDTO
     * with access and refresh tokens if successful.
     * 
     * @param request The login request.
     * @return The login response.
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequestDTO request) {
        try {
            UserService.AuthSession session = userService.login(request);
            LoginResponseDTO response = new LoginResponseDTO(session.accessToken(), session.user());
            return ResponseEntity.ok()
                    .header("Set-Cookie", buildRefreshCookie(session.refreshToken()).toString())
                    .body(response);
        } catch (TooManyLoginAttemptsException e) {
            long retryAfterSeconds = Math.max(1L, e.getRetryAfter().toSeconds());
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .header("Retry-After", String.valueOf(retryAfterSeconds))
                    .body(new ErrorResponse(e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponse(e.getMessage()));
        }
    }

    /**
     * Token refresh endpoint that accepts a RefreshTokenRequestDTO and returns a
     * new LoginResponseDTO with refreshed tokens if the refresh token is valid.
     * 
     * @param request The refresh token request.
     * @return The login response.
     */
    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@CookieValue(value = REFRESH_COOKIE_NAME, required = false) String refreshToken) {
        try {
            UserService.AuthSession session = userService.refresh(refreshToken);
            LoginResponseDTO response = new LoginResponseDTO(session.accessToken(), session.user());
            return ResponseEntity.ok()
                    .header("Set-Cookie", buildRefreshCookie(session.refreshToken()).toString())
                    .body(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponse(e.getMessage()));
        }
    }

    /**
     * Logout endpoint that accepts an optional RefreshTokenRequestDTO and an
     * optional Authorization header containing the access token, and logs out the
     * user by revoking the provided tokens.
     * 
     * @param request             The refresh token request.
     * @param authorizationHeader The access token.
     * @return A success response.
     */
    @PostMapping("/logout")
    public ResponseEntity<?> logout(
            @CookieValue(value = REFRESH_COOKIE_NAME, required = false) String refreshToken,
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader) {
        try {
            String accessToken = extractBearerToken(authorizationHeader);
            userService.logout(accessToken, refreshToken);
            return ResponseEntity.ok()
                    .header("Set-Cookie", clearRefreshCookie().toString())
                    .build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(e.getMessage()));
        }
    }

    private ResponseCookie buildRefreshCookie(String refreshToken) {
        String safeRefreshToken = refreshToken == null ? "" : refreshToken;
        long maxAgeSeconds = Math.max(1L, refreshCookieMaxAgeMs / 1000L);
        return ResponseCookie.from(REFRESH_COOKIE_NAME, safeRefreshToken)
                .httpOnly(true)
                .secure(refreshCookieSecure)
                .sameSite(refreshCookieSameSite)
                .path("/")
                .maxAge(maxAgeSeconds)
                .build();
    }

    private ResponseCookie clearRefreshCookie() {
        return ResponseCookie.from(REFRESH_COOKIE_NAME, "")
                .httpOnly(true)
                .secure(refreshCookieSecure)
                .sameSite(refreshCookieSameSite)
                .path("/")
                .maxAge(0)
                .build();
    }

    /**
     * Token status endpoint that accepts a TokenStatusRequestDTO containing an
     * access token and returns a TokenStatusResponseDTO indicating whether the
     * token is revoked or not.
     * 
     * @param request The token status request.
     * @return The token status response.
     */
    @PostMapping("/token-status")
    public ResponseEntity<?> tokenStatus(
            @Valid @RequestBody TokenStatusRequestDTO request,
            @RequestHeader(value = INTERNAL_SERVICE_HEADER, required = false) String serviceHeader,
            @RequestHeader(value = INTERNAL_SECRET_HEADER, required = false) String secretHeader) {
        if (!isInternalRequest(serviceHeader, secretHeader)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ErrorResponse("Forbidden"));
        }

        try {
            boolean revoked = userService.isAccessTokenRevoked(request.accessToken());
            return ResponseEntity.ok(new TokenStatusResponseDTO(revoked));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(e.getMessage()));
        }
    }

    @GetMapping("/ws-ticket")
    public ResponseEntity<WebSocketTicketResponseDTO> issueWebSocketTicket(Authentication authentication) {
        String username = authentication.getName();
        String ticket = userService.issueWebSocketTicket(username);
        return ResponseEntity.ok(new WebSocketTicketResponseDTO(ticket));
    }

    private boolean isInternalRequest(String serviceHeader, String secretHeader) {
        if (serviceHeader == null || serviceHeader.isBlank()) {
            return false;
        }
        String configuredSecret = internalSecret == null ? "" : internalSecret.trim();
        String providedSecret = secretHeader == null ? "" : secretHeader.trim();
        if (configuredSecret.isBlank()) {
            return false;
        }
        return configuredSecret.equals(providedSecret);
    }

    /**
     * Helper method to extract the Bearer token from the Authorization header.
     * 
     * @param authorizationHeader The authorization header.
     * @return The access token.
     */
    private String extractBearerToken(String authorizationHeader) {
        if (authorizationHeader == null || authorizationHeader.isBlank()) {
            return null;
        }
        if (!authorizationHeader.startsWith("Bearer ")) {
            return null;
        }
        return authorizationHeader.substring(7);
    }

    /**
     * Response class for error messages.
     * 
     * @param message The error message.
     */
    record ErrorResponse(String message) {
    }
}
