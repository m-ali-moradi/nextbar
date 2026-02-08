package de.fhdo.eventPlanner.security;

import java.util.List;

/**
 * Holds the RBAC claims extracted from gateway headers.
 * Used for fine-grained authorization checks.
 */
public record RbacClaims(List<String> roles, List<String> assignments) {

    /**
     * Check if user has a specific role.
     */
    public boolean hasRole(String role) {
        return roles != null && roles.stream()
                .anyMatch(r -> r.equalsIgnoreCase(role));
    }

    /**
     * Check if user is assigned to a specific service with a role.
     * Assignment format: SERVICE:ROLE:RESOURCE_ID
     */
    public boolean hasAssignment(String service, String role) {
        if (assignments == null)
            return false;
        String prefix = (service + ":" + role + ":").toUpperCase();
        return assignments.stream()
                .anyMatch(a -> a != null && a.toUpperCase().startsWith(prefix));
    }

    /**
     * Check if user has access to a specific resource.
     */
    public boolean hasResourceAccess(String service, String role, String resourceId) {
        if (assignments == null)
            return false;
        String pattern = (service + ":" + role + ":" + resourceId).toUpperCase();
        return assignments.stream()
                .anyMatch(a -> a != null && a.toUpperCase().equals(pattern));
    }

    /**
     * Check if user is admin.
     */
    public boolean isAdmin() {
        return hasRole("ADMIN") ||
                (assignments != null && assignments.stream()
                        .anyMatch(a -> a != null && a.toUpperCase().contains(":ADMIN:")));
    }
}
