package com.nextbar.usersservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateUserDTO(
        @NotBlank(message = "Username is required")
        @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
        String username,
        @NotBlank(message = "Email is required")
        @Email
        String email,
        @Size(min = 3, max = 50, message = "First name must be between 3 and 50 characters")
        @NotBlank(message = "First name is required")
        String firstName,
        @Size(min = 2, max = 50, message = "Last name must be between 2 and 50 characters")
        @NotBlank(message = "Last name is required")
        String lastName,
        @NotBlank(message = "Password is required")
        @Size(min = 6, message = "Password must be at least 6 characters long")
        String password
) {}
