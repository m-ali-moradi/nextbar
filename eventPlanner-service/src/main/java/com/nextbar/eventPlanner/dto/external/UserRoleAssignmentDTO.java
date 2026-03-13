package com.nextbar.eventPlanner.dto.external;

import java.util.UUID;

/**
 * DTO representing a user role assignment in the context of an external service.
 *
 * @param assignmentId Unique identifier for the role assignment
 * @param service Name of the external service (e.g., "BarService", "EventService")
 * @param role Name of the assigned role (e.g., "Admin", "Bartender")
 * @param resourceId Identifier of the resource to which the role is assigned (e.g., Bar ID, Droppoint ID)
 */
public record UserRoleAssignmentDTO(
                UUID assignmentId,
                String service,
                String role,
                UUID resourceId) {
}