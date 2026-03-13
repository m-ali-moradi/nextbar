package com.nextbar.bar.security;

/**
 * Enum representing the different roles in the RBAC system.
 */

public enum RbacRole {
    ADMIN,
    MANAGER,
    BARTENDER;

    public String value() {
        return name();
    }

    public boolean equalsIgnoreCase(String raw) {
        return raw != null && value().equalsIgnoreCase(raw);
    }

    public boolean containsIgnoreCase(String raw) {
        return raw != null && raw.toUpperCase().contains(value());
    }

    public boolean presentInAssignment(String assignment) {
        return assignment != null && assignment.toUpperCase().contains(":" + value() + ":");
    }
}
