package com.nextbar.bar.security;

/**
 * Enum representing the names of RBAC services in the application.
 * Currently, it includes only the "BAR" service, but can be extended in the future
 * to include additional services as needed.
 */
public enum RbacServiceName {
    BAR;

    public String value() {
        return name();
    }

    public boolean containsIgnoreCase(String raw) {
        return raw != null && raw.toUpperCase().contains(value());
    }
}
