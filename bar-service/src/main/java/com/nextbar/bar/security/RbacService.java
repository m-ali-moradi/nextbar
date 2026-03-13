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

/**
 * Service for Role-Based Access Control (RBAC) in the bar service.
 * It checks the authenticated user's roles and assignments to determine access permissions.
 * Roles and assignments are expected to be provided in the authentication details as RbacClaims.
 */
@Component
public class RbacService {

    public boolean canListAllBars() {
        return isAdmin() || isBarManager();
    }

    public void requireBarStaffOrAdmin() {
        if (!isAdmin() && !isBarManager() && !isBarStaff()) {
            throw new AccessDeniedException("Requires BAR staff/manager or ADMIN");
        }
    }

    public Set<UUID> requireStaffBarIdsForListing() {
        if (canListAllBars()) {
            return Collections.emptySet();
        }
        if (!isBarStaff()) {
            throw new AccessDeniedException("No bar access role");
        }
        return getStaffBarIds();
    }

    public void requireBarAccess(UUID barId) {
        if (!canAccessBar(barId)) {
            throw new AccessDeniedException("No access to bar " + barId);
        }
    }

    // Determines if the authenticated user has access to a specific bar based on their roles and assignments. Admins and bar managers have access to all bars, while bar staff have access only to bars specified in their assignments.
    public boolean canAccessBar(UUID barId) {
        if (barId == null)
            return false;
        if (isAdmin() || isBarManager())
            return true;
        return getStaffBarIds().contains(barId);
    }

    // A user is considered an admin if they have the "ADMIN" role or any assignment with a role containing "ADMIN", regardless of the service or resource.
    public boolean isAdmin() {
        RbacClaims claims = getClaims();
        if (claims.roles() != null && claims.roles().stream().anyMatch(RbacRole.ADMIN::equalsIgnoreCase)) {
            return true;
        }
        if (claims.assignments() != null) {
            return claims.assignments().stream().anyMatch(RbacRole.ADMIN::presentInAssignment);
        }
        return false;
    }

    // A user is considered a bar manager if they have an assignment for the bar service
    public boolean isBarManager() {
        for (Assignment a : parseAssignments(getClaims().assignments())) {
            if (isBarService(a.service) && RbacRole.MANAGER.equalsIgnoreCase(a.role)) {
                // resource "*" means all bars.
                if (a.resource.equals("*") || a.resource.isBlank()) {
                    return true;
                }
            }
        }
        return false;
    }

    // A user is considered bar staff if they have any BARTENDER assignment for the bar service, regardless of the resource.
    public boolean isBarStaff() {
        for (Assignment a : parseAssignments(getClaims().assignments())) {
            if (isBarService(a.service) && isStaffRole(a.role)) {
                return true;
            }
        }
        return false;
    }

    // Retrieves the set of bar IDs that the user has staff access to based on their assignments. If an assignment has a resource of "*" or is blank, it is ignored for this purpose since it grants access to all bars.
    public Set<UUID> getStaffBarIds() {
        Set<UUID> ids = new HashSet<>();
        for (Assignment a : parseAssignments(getClaims().assignments())) {
            if (isBarService(a.service) && isStaffRole(a.role)) {
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

    // Helper method to check if a service string indicates the bar service. This is a simple check that looks for the substring "BAR" in the service name, ignoring case. In a real application, this might be more complex and based on specific service identifiers.
    private static boolean isBarService(String service) {
        return RbacServiceName.BAR.containsIgnoreCase(service);
    }

    // Helper method to check if a role string indicates bar staff.
    private static boolean isStaffRole(String role) {
        return RbacRole.BARTENDER.containsIgnoreCase(role);
    }

    // Helper method to retrieve RBAC claims from the authentication details. If there are no claims or if the details are not of type RbacClaims, it returns an empty RbacClaims object. This allows the service to safely access roles and assignments without risking null pointer exceptions.
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

    // Helper method to parse a list of assignment strings into a list of Assignment objects. Each assignment string is expected to be in the format "service:role:resource". If the string is malformed, it is skipped. This allows the service to work with structured assignment data while still accepting flexible input formats.
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

    // Record class to represent a parsed assignment with service, role, and resource fields. This provides a structured way to work with assignment data after parsing it from strings.
    private record Assignment(String service, String role, String resource) {
    }
}
