package com.nextbar.usersservice.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nextbar.usersservice.dto.AdminCreateUserDTO;
import com.nextbar.usersservice.dto.AdminResetPasswordRequestDTO;
import com.nextbar.usersservice.dto.AssignRoleDTO;
import com.nextbar.usersservice.dto.UpdateUserRequestDTO;
import com.nextbar.usersservice.dto.UserAdminDTO;
import com.nextbar.usersservice.dto.UserAssignmentRequestDTO;
import com.nextbar.usersservice.service.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/*
 * Controller for handling user management endpoints such as retrieving users, creating users, updating users, deleting users, and managing user roles and assignments.
 * Provides endpoints for getting all users, getting a user by ID or username, creating a new user, updating an existing user, deleting a user, assigning roles to a user, replacing user assignments, and removing a specific user assignment.
 */
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    public ResponseEntity<List<UserAdminDTO>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    /**
     * Create a new user.
     * 
     * @param dto The user to create.
     * @return The created user.
     */
    @PostMapping
    public ResponseEntity<UserAdminDTO> createUser(@Valid @RequestBody AdminCreateUserDTO dto) {
        return ResponseEntity.ok(userService.createUserAdmin(dto));
    }

    /**
     * Get a user by ID.
     * 
     * @param id The ID of the user.
     * @return The user.
     */
    @GetMapping("/{id}")
    public ResponseEntity<UserAdminDTO> getUserById(@PathVariable @NonNull UUID id) {
        UserAdminDTO user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }

    /**
     * Get a user by username.
     * 
     * @param username The username of the user.
     * @return The user.
     */
    @GetMapping("/username/{username}")
    public ResponseEntity<UserAdminDTO> getUserByUsername(@PathVariable String username) {
        UserAdminDTO user = userService.getUserByUsername(username);
        return ResponseEntity.ok(user);
    }

    /**
     * Update a user.
     * 
     * @param id      The ID of the user.
     * @param userDTO The user to update.
     * @return The updated user.
     */
    @PutMapping("/{id}")
    public ResponseEntity<UserAdminDTO> updateUser(@PathVariable @NonNull UUID id,
            @Valid @RequestBody UpdateUserRequestDTO request) {
        UserAdminDTO updatedUser = userService.updateUser(id, request);
        return ResponseEntity.ok(updatedUser);
    }

    /**
     * Delete a user.
     * 
     * @param id The ID of the user.
     * @return A success response.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable @NonNull UUID id) {
        userService.deleteUser(id);
        return ResponseEntity.ok(new SuccessResponse("User deleted successfully"));
    }

    @PostMapping("/{id}/password/reset")
    public ResponseEntity<?> resetUserPassword(@PathVariable @NonNull UUID id,
            @Valid @RequestBody AdminResetPasswordRequestDTO request) {
        userService.resetUserPassword(id, request.newPassword());
        return ResponseEntity.ok(new SuccessResponse("Password reset successfully"));
    }

    /**
     * Assign a role to a user.
     * 
     * @param dto The role assignment request.
     * @return The updated user.
     */
    @PostMapping("/assign-role")
    public ResponseEntity<UserAdminDTO> assignRole(@Valid @RequestBody AssignRoleDTO dto) {
        return ResponseEntity.ok(userService.assignRole(dto));
    }

    /**
     * Replace all assignments for a user.
     * 
     * @param id          The ID of the user.
     * @param assignments The new assignments.
     * @return The updated assignments.
     */
    @PutMapping("/{id}/assignments")
    public ResponseEntity<UserAdminDTO> replaceAssignments(@PathVariable @NonNull UUID id,
            @Valid @RequestBody List<@Valid UserAssignmentRequestDTO> assignments) {
        return ResponseEntity.ok(userService.replaceAssignments(id, assignments));
    }

    /**
     * Remove a specific assignment from a user.
     * 
     * @param id           The ID of the user.
     * @param assignmentId The ID of the assignment.
     * @return The updated assignments.
     */
    @DeleteMapping("/{id}/assignments/{assignmentId}")
    public ResponseEntity<UserAdminDTO> removeAssignment(@PathVariable @NonNull UUID id,
            @PathVariable @NonNull UUID assignmentId) {
        return ResponseEntity.ok(userService.removeAssignment(id, assignmentId));
    }

    /**
     * Response class for success messages.
     * 
     * @param message The success message.
     */
    record SuccessResponse(String message) {
    }
}
