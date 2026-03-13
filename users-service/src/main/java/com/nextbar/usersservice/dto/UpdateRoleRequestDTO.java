package com.nextbar.usersservice.dto;

import java.util.Set;

public record UpdateRoleRequestDTO(
                Boolean global,
                Set<String> permissionCodes) {
}