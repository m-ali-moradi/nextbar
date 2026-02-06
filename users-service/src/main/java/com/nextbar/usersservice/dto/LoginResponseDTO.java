package com.nextbar.usersservice.dto;

public record LoginResponseDTO(
        String accessToken,
        UserMeDTO user
) {}
