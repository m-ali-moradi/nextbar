package com.nextbar.usersservice.dto;

import java.util.List;
import java.util.UUID;

public record UserAdminDTO(
        UUID id,
        String username,
        String email,
        boolean enabled,
        List<UserRoleAssignmentDTO> assignments
) {}
