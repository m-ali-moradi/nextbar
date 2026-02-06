package de.fhdo.dropPointsSys.security;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class RbacService {

    public void requireReadAccess() {
        if (!isAdmin() && !isDropPointManager() && !isEventManager()) {
            throw new AccessDeniedException("No access to droppoints");
        }
    }

    public void requireWriteAccess() {
        if (!isAdmin() && !isDropPointManager()) {
            throw new AccessDeniedException("Requires DROP_POINT manager or ADMIN");
        }
    }

    public void requireCreateAccess() {
        if (!isAdmin() && !isEventManager()) {
            throw new AccessDeniedException("Requires EVENT manager or ADMIN");
        }
    }

    private static boolean isAdmin() {
        RbacClaims claims = getClaims();
        if (claims.roles() != null && claims.roles().stream().anyMatch(r -> "ADMIN".equalsIgnoreCase(r))) {
            return true;
        }
        if (claims.assignments() != null) {
            return claims.assignments().stream().anyMatch(a -> a != null && a.toUpperCase().contains(":ADMIN:"));
        }
        return false;
    }

    private static boolean isDropPointManager() {
        for (Assignment a : parseAssignments(getClaims().assignments())) {
            if (a.role.equalsIgnoreCase("MANAGER") && a.service.toUpperCase().contains("DROP")) {
                return true;
            }
        }
        return false;
    }

    private static boolean isEventManager() {
        for (Assignment a : parseAssignments(getClaims().assignments())) {
            if (a.role.equalsIgnoreCase("MANAGER") && a.service.toUpperCase().contains("EVENT")) {
                return true;
            }
        }
        return false;
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
