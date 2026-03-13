package com.nextbar.usersservice.dto;

import java.util.UUID;
import org.springframework.lang.NonNull;

public record UserProfileDTO(
        @NonNull UUID id,
        String username,
        String firstName,
        String lastName,
        String email,
        boolean enabled,
        boolean locked) {
}