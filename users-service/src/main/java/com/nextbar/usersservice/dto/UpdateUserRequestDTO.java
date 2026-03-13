package com.nextbar.usersservice.dto;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public record UpdateUserRequestDTO(
        @Size(min = 3, max = 50) String username,
        @Size(min = 1, max = 100) String firstName,
        @Size(min = 1, max = 100) String lastName,
        @Email @Size(max = 255) String email,
        Boolean enabled,
        List<@Valid UserAssignmentRequestDTO> assignments) {
}
