package com.coditects.usersservice.dto;


import java.util.List;
import java.util.UUID;

public record UserMeDTO(
        UUID id,
        String username,
        String email,
        List<UserRoleDTO> roles
) {}
