package com.nextbar.usersservice.dto;

import java.util.List;
import java.util.UUID;
import org.springframework.lang.NonNull;

public record UserAdminDTO(
                @NonNull UUID id,
                String username,
                String firstName,
                String lastName,
                String email,
                boolean enabled,
                List<UserRoleAssignmentDTO> assignments) {
}
