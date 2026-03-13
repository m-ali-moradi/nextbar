package com.nextbar.usersservice.dto;

import java.util.UUID;

public record ServiceResponseDTO(
        UUID id,
        String code,
        String description) {
}