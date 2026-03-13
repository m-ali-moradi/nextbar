package com.nextbar.usersservice.dto;

import java.util.UUID;

public record AssignmentResponseDTO(
        UUID assignmentId,
        UUID userId,
        String username,
        String firstName,
        String lastName,
        String roleName,
        String serviceCode,
        UUID resourceId) {
}