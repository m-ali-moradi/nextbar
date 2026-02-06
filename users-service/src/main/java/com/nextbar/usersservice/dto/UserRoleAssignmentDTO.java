package com.nextbar.usersservice.dto;

import java.util.UUID;

public record UserRoleAssignmentDTO(
        UUID assignmentId,
        String service,
        String role,
        UUID resourceId
) {}