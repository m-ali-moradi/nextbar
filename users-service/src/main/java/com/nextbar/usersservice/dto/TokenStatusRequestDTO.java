package com.nextbar.usersservice.dto;

import jakarta.validation.constraints.NotBlank;

public record TokenStatusRequestDTO(
        @NotBlank String accessToken
) {}