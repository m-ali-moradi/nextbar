package com.nextbar.usersservice.dto;

import java.util.UUID;

public record UserRoleDTO(
        String service,      // BAR_SERVICE
        String role,         // BARTENDER
        UUID resourceId      // barId (nullable)
) {}
