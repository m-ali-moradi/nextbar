package com.nextbar.usersservice.dto;

import java.util.UUID;

import org.springframework.lang.NonNull;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AssignRoleDTO(
                @NotNull @NonNull UUID userId,
                @NotBlank String serviceCode,
                @NotBlank String roleName,
                UUID resourceId // Optional. if provided, the user will be assigned to the specified resource
                                // (e.g., bar or event) with the given role. If not provided, the role will be
                                // global (not tied to a specific resource).
) {
}
