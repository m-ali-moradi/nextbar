package com.nextbar.usersservice.dto;

import java.util.UUID;

public record UserRoleDTO(
        String service, // BAR_SERVICE, EVENT_SERVICE, WAREHOUSE_SERVICE,DROPPOINT_SERVICE.
        String role, // BARTENDER, ADMIN, BAR_MANAGER, EVENT_MANAGER, WAREHOUSE_MANAGER,
                     // WAREHOUSE_WORKER, DROPBOX_MANAGER, DROPPOINT_OPERATOR.
        UUID resourceId // barId (nullable)
) {
}
