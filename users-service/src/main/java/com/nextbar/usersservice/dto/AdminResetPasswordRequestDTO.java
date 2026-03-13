package com.nextbar.usersservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AdminResetPasswordRequestDTO(
        @NotBlank @Size(min = 8, max = 128) String newPassword) {
}