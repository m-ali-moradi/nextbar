package com.nextbar.eventPlanner.dto.external;

import lombok.*;
import java.util.UUID;
import java.util.List;

/**
 * DTO representing a User from the users-service.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDto {

    private UUID id;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private boolean active;
    private List<UserRoleAssignmentDTO> assignments;
}
