package com.nextbar.usersservice.security;

import java.util.List;

/**
 * Record to hold RBAC (Role-Based Access Control) claims.
 * 
 * @param roles       List of roles assigned to the user.
 * @param assignments List of assignments (e.g., bar assignments) for the user.
 */
public record RbacClaims(
        List<String> roles,
        List<String> assignments) {
}