package com.nextbar.bar.security;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class RbacService {

    public void requireEventManagerOrAdmin() {
        if (!isAdmin() && !isEventManager()) {
            throw new AccessDeniedException("Requires EVENT manager or ADMIN");
        }
    }

    public void requireBarAccess(UUID barId) {
        if (!canAccessBar(barId)) {
            throw new AccessDeniedException("No access to bar " + barId);
        }
    }

    public boolean canAccessBar(UUID barId) {
        if (barId == null)
            return false;
        if (isAdmin() || isBarManager() || isWarehouseStaff())
            return true;
        return getOperatorBarIds().contains(barId);
    }

    public boolean isAdmin() {
        RbacClaims claims = getClaims();
        if (claims.roles() != null && claims.roles().stream().anyMatch(r -> "ADMIN".equalsIgnoreCase(r))) {
            return true;
        }
        if (claims.assignments() != null) {
            return claims.assignments().stream().anyMatch(a -> a != null && a.toUpperCase().contains(":ADMIN:"));
        }
        return false;
    }

    public boolean isWarehouseStaff() {
        // Global roles check
        if (getClaims().roles() != null && getClaims().roles().stream()
                .anyMatch(r -> r.toUpperCase().contains("WAREHOUSE"))) {
            return true;
        }

        // Assignments check
        for (Assignment a : parseAssignments(getClaims().assignments())) {
            if (a.service.toUpperCase().contains("WAREHOUSE")) {
                return true;
            }
        }
        return false;
    }

    public boolean isEventManager() {
        for (Assignment a : parseAssignments(getClaims().assignments())) {
            if (a.role.equalsIgnoreCase("MANAGER") && a.service.toUpperCase().contains("EVENT")) {
                return true;
            }
        }
        return false;
    }

    public boolean isBarManager() {
        for (Assignment a : parseAssignments(getClaims().assignments())) {
            if (isBarService(a.service) && a.role.equalsIgnoreCase("MANAGER")) {
                // resource "*" means all bars.
                if (a.resource.equals("*") || a.resource.isBlank()) {
                    return true;
                }
            }
        }
        return false;
    }

    public Set<UUID> getOperatorBarIds() {
        Set<UUID> ids = new HashSet<>();
        for (Assignment a : parseAssignments(getClaims().assignments())) {
            if (isBarService(a.service) && isOperatorRole(a.role)) {
                if (a.resource == null || a.resource.isBlank() || a.resource.equals("*")) {
                    continue;
                }
                try {
                    ids.add(UUID.fromString(a.resource));
                } catch (IllegalArgumentException ignored) {
                    // ignore malformed resource IDs
                }
            }
        }
        return ids;
    }

    private static boolean isBarService(String service) {
        if (service == null)
            return false;
        return service.toUpperCase().contains("BAR");
    }

    private static boolean isOperatorRole(String role) {
        if (role == null)
            return false;
        String r = role.toUpperCase();
        return r.contains("OPERATOR") || r.contains("BARTENDER");
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
        if (assignments == null || assignments.isEmpty())
            return Collections.emptyList();
        List<Assignment> out = new ArrayList<>(assignments.size());
        for (String raw : assignments) {
            if (raw == null || raw.isBlank())
                continue;
            String[] parts = raw.split(":", 3);
            if (parts.length != 3)
                continue;
            out.add(new Assignment(parts[0], parts[1], parts[2]));
        }
        return out;
    }

    private record Assignment(String service, String role, String resource) {
    }
}
