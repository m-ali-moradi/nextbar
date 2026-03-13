package com.nextbar.usersservice.controller;

import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.util.ReflectionTestUtils;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.nextbar.usersservice.dto.LoginRequestDTO;
import com.nextbar.usersservice.dto.UserAdminDTO;
import com.nextbar.usersservice.security.JwtAuthenticationFilter;
import com.nextbar.usersservice.service.TooManyLoginAttemptsException;
import com.nextbar.usersservice.service.UserService;

import jakarta.servlet.http.Cookie;

/**
 * Unit tests for {@link AuthController}.
 *
 * These tests cover the core authentication flows: logging in, refreshing
 * tokens,
 * logging out, and checking token status. We verify that valid requests succeed
 * with the expected responses and that invalid requests are rejected with
 * appropriate status codes and error messages.
 */

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
@TestPropertySource(properties = "security.internal.secret=test-internal-secret")
class AuthControllerTest {

        private static final String AUTH_BASE = "/api/v1/users";

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private AuthController authController;

        @MockBean
        private UserService userService;

        @MockBean
        private JwtAuthenticationFilter jwtAuthenticationFilter;

        @BeforeEach
        void setUp() {
                ReflectionTestUtils.setField(authController, "internalSecret", "test-internal-secret");
        }

        @Test
        void loginReturnsAccessTokenAndRefreshCookie() throws Exception {
                UserAdminDTO user = new UserAdminDTO(
                                Objects.requireNonNull(UUID.randomUUID()),
                                "ali",
                                "Ali",
                                "Moradi",
                                "ali@example.com",
                                true,
                                List.of());

                when(userService.login(any(LoginRequestDTO.class)))
                                .thenReturn(new UserService.AuthSession("access-token", "refresh-token", user));

                mockMvc.perform(post(AUTH_BASE + "/login")
                                .contentType(APPLICATION_JSON)
                                .content("{\"username\":\"ali\",\"password\":\"secret\"}"))
                                .andExpect(status().isOk())
                                .andExpect(header().string("Set-Cookie", containsString("refreshToken=refresh-token")))
                                .andExpect(jsonPath("$.accessToken").value("access-token"))
                                .andExpect(jsonPath("$.user.username").value("ali"));
        }

        @Test
        void loginReturnsTooManyRequestsWhenTemporarilyLocked() throws Exception {
                when(userService.login(any(LoginRequestDTO.class)))
                                .thenThrow(new TooManyLoginAttemptsException(Duration.ofSeconds(45)));

                mockMvc.perform(post(AUTH_BASE + "/login")
                                .contentType(APPLICATION_JSON)
                                .content("{\"username\":\"ali\",\"password\":\"secret\"}"))
                                .andExpect(status().isTooManyRequests())
                                .andExpect(header().string("Retry-After", "45"));
        }

        @Test
        void refreshReturnsUnauthorizedWhenCookieIsMissingOrInvalid() throws Exception {
                when(userService.refresh(any())).thenThrow(new IllegalArgumentException("Invalid refresh token"));

                mockMvc.perform(post(AUTH_BASE + "/refresh"))
                                .andExpect(status().isUnauthorized());
        }

        @Test
        void logoutClearsRefreshCookieAndInvokesTokenRevocation() throws Exception {
                mockMvc.perform(post(AUTH_BASE + "/logout")
                                .cookie(new Cookie("refreshToken", "refresh-token"))
                                .header("Authorization", "Bearer access-token"))
                                .andExpect(status().isOk())
                                .andExpect(header().string("Set-Cookie", containsString("refreshToken=")))
                                .andExpect(header().string("Set-Cookie", containsString("Max-Age=0")));

                verify(userService).logout("access-token", "refresh-token");
        }

        @Test
        void tokenStatusReturnsForbiddenWithoutValidInternalHeaders() throws Exception {
                mockMvc.perform(post(AUTH_BASE + "/token-status")
                                .contentType(APPLICATION_JSON)
                                .content("{\"accessToken\":\"token\"}"))
                                .andExpect(status().isForbidden());
        }

        @Test
        void tokenStatusReturnsRevokedFlagWhenInternalHeadersAreValid() throws Exception {
                when(userService.isAccessTokenRevoked("token")).thenReturn(true);

                mockMvc.perform(post(AUTH_BASE + "/token-status")
                                .header("X-Internal-Service", "gateway")
                                .header("X-Internal-Secret", "test-internal-secret")
                                .contentType(APPLICATION_JSON)
                                .content("{\"accessToken\":\"token\"}"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.revoked").value(true));
        }
}
