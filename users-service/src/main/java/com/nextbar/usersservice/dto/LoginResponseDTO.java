package com.coditects.usersservice.dto;

public record LoginResponseDTO(
        String accessToken,
        UserMeDTO user
) {}
