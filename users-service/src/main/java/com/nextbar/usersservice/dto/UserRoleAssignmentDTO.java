package com.coditects.usersservice.dto;

import java.util.UUID;

public record UserRoleAssignmentDTO(
        UUID assignmentId,
        String service,
        String role,
        UUID resourceId
) {}