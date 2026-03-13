package com.nextbar.usersservice.dto;

import java.util.UUID;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AdminCreateUserDTO(
        @NotBlank String username,
        @NotBlank String firstName,
        @NotBlank String lastName,
        @NotBlank @Email String email,
        @NotBlank @Size(min = 8, max = 128) String password,
                String roleName,
                String serviceCode,
                UUID resourceId // Optional. if provided, the user will be assigned to the specified resource
                                // (e.g., bar or event) with the given role. If not provided, the role will be
                                // global (not tied to a specific resource).
) {

    @AssertTrue(message = "serviceCode is required when roleName is provided (except ADMIN)")
    public boolean isRoleServiceCombinationValid() {
        if (roleName == null || roleName.isBlank()) {
            return true;
        }
        if ("ADMIN".equalsIgnoreCase(roleName.trim())) {
            return true;
        }
        return serviceCode != null && !serviceCode.isBlank();
    }

    @AssertTrue(message = "roleName is required when serviceCode is provided")
    public boolean isServiceRoleCombinationValid() {
        if (serviceCode == null || serviceCode.isBlank()) {
            return true;
        }
        return roleName != null && !roleName.isBlank();
    }
}
