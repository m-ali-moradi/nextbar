package com.nextbar.usersservice.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;

public record UserAssignmentRequestDTO(
                @NotBlank String serviceCode,
                @NotBlank String roleName,
                UUID resourceId) {
}
