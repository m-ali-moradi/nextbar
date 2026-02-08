package de.fhdo.eventPlanner.client;

import de.fhdo.eventPlanner.dto.external.UserDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

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
    @GetMapping("/users")
    List<UserDto> getAllUsers();

    /**
     * Get a specific user by ID.
     */
    @GetMapping("/users/{id}")
    UserDto getUserById(@PathVariable("id") UUID id);

    /**
     * Get a user by username.
     */
    @GetMapping("/users/username/{username}")
    UserDto getUserByUsername(@PathVariable("username") String username);
}
