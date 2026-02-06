package com.nextbar.usersservice.dto;

import java.util.UUID;

public record AdminCreateUserDTO(
        String username,
        String email,
        String password,
        String roleName,
        String serviceCode,
        UUID resourceId
) {}
