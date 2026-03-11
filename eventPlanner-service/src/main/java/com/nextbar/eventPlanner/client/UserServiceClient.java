package com.nextbar.eventPlanner.client;

import com.nextbar.eventPlanner.dto.external.AssignRoleRequest;
import com.nextbar.eventPlanner.dto.external.UserDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.UUID;

/**
 * Feign client for communicating with the users-service.
 * Used to fetch staff lists for bar and drop point assignments.
 */
@FeignClient(name = "users-service", fallback = UserServiceClientFallback.class)
public interface UserServiceClient {

    /**
     * Get all users from the users-service.
     */
    @GetMapping("/api/v1/users")
    List<UserDto> getAllUsers();

    /**
     * Get a specific user by ID.
     */
    @GetMapping("/api/v1/users/{id}")
    UserDto getUserById(@PathVariable("id") UUID id);

    /**
     * Get a user by username.
     */
    @GetMapping("/api/v1/users/username/{username}")
    UserDto getUserByUsername(@PathVariable("username") String username);

    /**
     * Assign a role to a user with a specific resource ID.
     * Calls the users-service's RoleManagementController POST
     * /users/management/assignments.
     */
    @PostMapping("/api/v1/users/management/assignments")
    Object assignRoleToUser(@RequestBody AssignRoleRequest request);
}
