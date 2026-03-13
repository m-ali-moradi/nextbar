package com.nextbar.usersservice.dto;

import java.util.Set;
import java.util.UUID;

public record RoleResponseDTO(
        UUID id,
        String roleName,
        boolean global,
        Set<String> permissionCodes) {
}