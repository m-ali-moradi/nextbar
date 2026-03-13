package com.nextbar.usersservice.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nextbar.usersservice.dto.ChangePasswordRequestDTO;
import com.nextbar.usersservice.dto.UpdateProfileRequestDTO;
import com.nextbar.usersservice.service.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Controller for handling user profile-related endpoints such as retrieving and
 * updating the current user's profile and changing the password.
 */
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class ProfileController {

    private final UserService userService;

    /**
     * Endpoint to retrieve the current user's profile information.
     * 
     * @param authentication The authentication object containing the current user's
     *                       details.
     * @return The current user's profile information or an error response if
     *         retrieval fails.
     */
    @GetMapping("/profile")
    public ResponseEntity<?> getProfile(Authentication authentication) {
        try {
            String username = authentication.getName();
            return ResponseEntity.ok(userService.getCurrentUserProfile(username));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(e.getMessage()));
        }
    }

    /**
     * Endpoint to update the current user's profile information.
     * 
     * @param authentication The authentication object containing the current user's
     *                       details.
     * @param request        The profile information to update.
     * @return The updated profile information or an error response if update fails.
     */
    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(Authentication authentication,
            @Valid @RequestBody UpdateProfileRequestDTO request) {
        try {
            String username = authentication.getName();
            return ResponseEntity.ok(userService.updateCurrentUserProfile(username, request));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(e.getMessage()));
        }
    }

    /**
     * Endpoint to change the current user's password.
     * 
     * @param authentication The authentication object containing the current user's
     *                       details.
     * @param request        The password information to change.
     * @return A success response if the password was changed successfully or an
     *         error response if the change fails.
     */
    @PostMapping("/password/change")
    public ResponseEntity<?> changePassword(Authentication authentication,
            @Valid @RequestBody ChangePasswordRequestDTO request) {
        try {
            String username = authentication.getName();
            userService.changeCurrentUserPassword(username, request);
            return ResponseEntity.ok(new SuccessResponse("Password changed successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(e.getMessage()));
        }
    }

    record ErrorResponse(String message) {
    }

    record SuccessResponse(String message) {
    }
}