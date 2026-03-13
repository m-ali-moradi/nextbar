package com.nextbar.dropPoint.security;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * RbacService provides role-based access control checks for the drop-points-service.
 * It checks user roles and assignments to determine if they have the necessary permissions
 * for read, write, or create operations on drop points.
 */
@Component
public class RbacService {

    private static final String ROLE_ADMIN = "ADMIN";
    private static final String ROLE_OPERATOR = "OPERATOR";
    private static final String ROLE_DROP_POINT_OPERATOR = "DROP_POINT_OPERATOR";

    public void requireReadAccess() {
        if (!hasDropPointsAccess()) {
            throw new AccessDeniedException("Requires DROP_POINT_OPERATOR or ADMIN");
        }
    }

    public void requireWriteAccess() {
        if (!hasDropPointsAccess()) {
            throw new AccessDeniedException("Requires DROP_POINT_OPERATOR or ADMIN");
        }
    }

    public void requireCreateAccess() {
        if (!isAdmin()) {
            throw new AccessDeniedException("Requires ADMIN");
        }
    }

    public void requireAdminAccess() {
        if (!isAdmin()) {
            throw new AccessDeniedException("Requires ADMIN");
        }
    }

    private static boolean hasDropPointsAccess() {
        return isAdmin() || isDropPointOperator();
    }

    private static boolean isAdmin() {
        RbacClaims claims = getClaims();
        if (claims.roles() != null && claims.roles().stream().anyMatch(r -> ROLE_ADMIN.equalsIgnoreCase(r))) {
            return true;
        }
        if (claims.assignments() != null) {
            return claims.assignments().stream().anyMatch(a -> a != null && a.toUpperCase().contains(":ADMIN:"));
        }
        return false;
    }

    private static boolean isDropPointOperator() {
        RbacClaims claims = getClaims();
        if (claims.roles() != null
                && claims.roles().stream().anyMatch(r -> ROLE_DROP_POINT_OPERATOR.equalsIgnoreCase(r))) {
            return true;
        }

        for (Assignment a : parseAssignments(claims.assignments())) {
            if (a.service.toUpperCase().contains("DROP")
                    && isDropPointOperatorRole(a.role)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isDropPointOperatorRole(String role) {
        return ROLE_DROP_POINT_OPERATOR.equalsIgnoreCase(role)
                || ROLE_OPERATOR.equalsIgnoreCase(role);
    }

    private static RbacClaims getClaims() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            return new RbacClaims(Collections.emptyList(), Collections.emptyList());
        }
        Object details = auth.getDetails();
        if (details instanceof RbacClaims claims) {
            return claims;
        }
        return new RbacClaims(Collections.emptyList(), Collections.emptyList());
    }

    private static List<Assignment> parseAssignments(List<String> assignments) {
        if (assignments == null || assignments.isEmpty()) return Collections.emptyList();
        List<Assignment> out = new ArrayList<>(assignments.size());
        for (String raw : assignments) {
            if (raw == null || raw.isBlank()) continue;
            String[] parts = raw.split(":", 3);
            if (parts.length != 3) continue;
            out.add(new Assignment(parts[0], parts[1], parts[2]));
        }
        return out;
    }

    private record Assignment(String service, String role, String resource) {
    }
}
