package com.nextbar.usersservice.bootstrap;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.nextbar.usersservice.model.Permission;
import com.nextbar.usersservice.model.Role;
import com.nextbar.usersservice.model.Service;
import com.nextbar.usersservice.model.User;
import com.nextbar.usersservice.model.UserRoleAssignment;
import com.nextbar.usersservice.repository.PermissionRepository;
import com.nextbar.usersservice.repository.RoleRepository;
import com.nextbar.usersservice.repository.ServiceRepository;
import com.nextbar.usersservice.repository.UserRepository;
import com.nextbar.usersservice.repository.UserRoleAssignmentRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DummyUsersInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DummyUsersInitializer.class);

    private static final UUID DEMO_BAR_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final ServiceRepository serviceRepository;
    private final UserRoleAssignmentRepository assignmentRepository;

    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        Service barService = ensureService("BAR_SERVICE", "Bar domain service");
        Service eventService = ensureService("EVENT_SERVICE", "Event planner domain service");
        Service dropPointService = ensureService("DROP_POINT_SERVICE", "Drop point domain service");
        Service warehouseService = ensureService("WAREHOUSE_SERVICE", "Warehouse domain service");

        Map<String, String> permissionsToEnsure = new LinkedHashMap<>();
        permissionsToEnsure.put("USERS_ADMIN", "Manage users in users-service");
        permissionsToEnsure.put("BAR_READ", "Read bar resources");
        permissionsToEnsure.put("BAR_WRITE", "Write/operate on bar resources");
        permissionsToEnsure.put("EVENT_READ", "Read event resources");
        permissionsToEnsure.put("EVENT_WRITE", "Write event resources");
        permissionsToEnsure.put("DROP_POINT_READ", "Read drop point resources");
        permissionsToEnsure.put("DROP_POINT_WRITE", "Write drop point resources");
        permissionsToEnsure.put("WAREHOUSE_READ", "Read warehouse resources");
        permissionsToEnsure.put("WAREHOUSE_WRITE", "Write warehouse resources");

        Map<String, Permission> ensuredPermissions = new LinkedHashMap<>();
        for (Map.Entry<String, String> entry : permissionsToEnsure.entrySet()) {
            ensuredPermissions.put(entry.getKey(), ensurePermission(entry.getKey(), entry.getValue()));
        }

        Role adminRole = ensureRole(
                "ADMIN",
                true,
                new HashSet<>(ensuredPermissions.values()));

        Role barManagerRole = ensureRole(
                "BAR_MANAGER",
                false,
                Set.of(ensuredPermissions.get("BAR_READ"), ensuredPermissions.get("BAR_WRITE")));

        Role bartenderRole = ensureRole(
                "BARTENDER",
                false,
                Set.of(ensuredPermissions.get("BAR_READ"), ensuredPermissions.get("BAR_WRITE")));

        // Warehouse roles
        Role warehouseManagerRole = ensureRole(
                "WAREHOUSE_MANAGER",
                false,
                Set.of(ensuredPermissions.get("WAREHOUSE_READ"), ensuredPermissions.get("WAREHOUSE_WRITE")));

        Role warehouseOperatorRole = ensureRole(
                "WAREHOUSE_OPERATOR",
                false,
                Set.of(ensuredPermissions.get("WAREHOUSE_READ"), ensuredPermissions.get("WAREHOUSE_WRITE")));

        User admin = ensureUser(
                "admin",
                "admin@nextbar.local",
                "admin123");

        User barManager = ensureUser(
                "bar_manager",
                "bar_manager@nextbar.local",
                "manager123");

        User bartender = ensureUser(
                "bartender",
                "bartender@nextbar.local",
                "bartender123");

        // Warehouse users
        User warehouseManager = ensureUser(
                "warehouse_manager",
                "warehouse_manager@nextbar.local",
                "warehouse123");

        User warehouseOperator = ensureUser(
                "warehouse_operator",
                "warehouse_operator@nextbar.local",
                "operator123");

        ensureAssignment(admin, adminRole, eventService, null);
        ensureAssignment(admin, adminRole, barService, null);
        ensureAssignment(admin, adminRole, dropPointService, null);
        ensureAssignment(admin, adminRole, warehouseService, null);

        ensureAssignment(barManager, barManagerRole, barService, null);

        ensureAssignment(bartender, bartenderRole, barService, DEMO_BAR_ID);

        // Warehouse assignments
        ensureAssignment(warehouseManager, warehouseManagerRole, warehouseService, null);
        ensureAssignment(warehouseOperator, warehouseOperatorRole, warehouseService, null);

        log.info(
                "Dummy users ensured: admin/admin123, bar_manager/manager123, bartender/bartender123 (bartender barId={})",
                DEMO_BAR_ID);
        log.info("Warehouse users ensured: warehouse_manager/warehouse123, warehouse_operator/operator123");
    }

    private Service ensureService(String code, String description) {
        return serviceRepository.findByCode(code)
                .orElseGet(() -> {
                    Service s = new Service();
                    s.setCode(code);
                    s.setDescription(description);
                    return serviceRepository.save(s);
                });
    }

    private Permission ensurePermission(String code, String description) {
        return permissionRepository.findByCode(code)
                .orElseGet(() -> {
                    Permission p = new Permission();
                    p.setCode(code);
                    p.setDescription(description);
                    return permissionRepository.save(p);
                });
    }

    private Role ensureRole(String name, boolean global, Set<Permission> desiredPermissions) {
        Role role = roleRepository.findByName(name).orElseGet(() -> {
            Role r = new Role();
            r.setName(name);
            r.setGlobal(global);
            r.setPermissions(new HashSet<>());
            return roleRepository.save(r);
        });

        if (role.isGlobal() != global) {
            role.setGlobal(global);
        }

        if (role.getPermissions() == null) {
            role.setPermissions(new HashSet<>());
        }

        boolean changed = role.getPermissions().addAll(desiredPermissions);
        if (changed) {
            role = roleRepository.save(role);
        }

        return role;
    }

    private User ensureUser(String username, String email, String rawPassword) {
        return userRepository.findByUsername(username)
                .orElseGet(() -> {
                    User u = new User();
                    u.setUsername(username);
                    u.setEmail(email);
                    u.setPasswordHash(passwordEncoder.encode(rawPassword));
                    u.setEnabled(true);
                    u.setLocked(false);
                    return userRepository.save(u);
                });
    }

    private void ensureAssignment(User user, Role role, Service service, UUID resourceId) {
        boolean exists = assignmentRepository.findByUserAndService(user, service).stream().anyMatch(a -> {
            if (a.getRole() == null || a.getRole().getId() == null)
                return false;
            if (a.getService() == null || a.getService().getId() == null)
                return false;
            boolean roleMatches = a.getRole().getId().equals(role.getId());
            boolean resourceMatches = (resourceId == null && a.getResourceId() == null)
                    || (resourceId != null && resourceId.equals(a.getResourceId()));
            return roleMatches && resourceMatches;
        });

        if (exists) {
            return;
        }

        UserRoleAssignment assignment = new UserRoleAssignment();
        assignment.setUser(user);
        assignment.setRole(role);
        assignment.setService(service);
        assignment.setResourceId(resourceId);
        assignmentRepository.save(assignment);
    }
}
