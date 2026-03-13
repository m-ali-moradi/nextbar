package com.nextbar.eventPlanner.dto.external;

import lombok.*;
import java.util.UUID;

/**
 * DTO for assigning a role to a user via the users-service.
 * Maps to the users-service's AssignRoleToUserRequestDTO.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssignRoleRequest {

    private UUID userId;
    private String roleName;
    private String serviceCode;
    private UUID resourceId;
}
