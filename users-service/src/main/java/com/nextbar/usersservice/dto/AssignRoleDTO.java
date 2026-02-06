package com.coditects.usersservice.dto;

import java.util.UUID;

public record AssignRoleDTO(
        UUID userId,
        String serviceCode,
        String roleName,
        UUID resourceId
) {}
