package de.fhdo.eventPlanner.client;

import de.fhdo.eventPlanner.dto.external.UserDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Fallback implementation for UserServiceClient.
 * Provides default responses when the users-service is unavailable.
 */
@Component
public class UserServiceClientFallback implements UserServiceClient {

    private static final Logger log = LoggerFactory.getLogger(UserServiceClientFallback.class);

    @Override
    public List<UserDto> getAllUsers() {
        log.warn("UserService unavailable - returning empty user list");
        return Collections.emptyList();
    }

    @Override
    public UserDto getUserById(UUID id) {
        log.warn("UserService unavailable - cannot fetch user with ID: {}", id);
        return null;
    }

    @Override
    public UserDto getUserByUsername(String username) {
        log.warn("UserService unavailable - cannot fetch user with username: {}", username);
        return null;
    }
}
