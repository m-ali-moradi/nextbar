package com.nextbar.usersservice.dto;

import java.util.UUID;

public record AssignRoleDTO(
        UUID userId,
        String serviceCode,
        String roleName,
        UUID resourceId
) {}
