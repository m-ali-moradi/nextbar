package com.nextbar.usersservice.config;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.nextbar.usersservice.model.Role;
import com.nextbar.usersservice.model.Service;
import com.nextbar.usersservice.model.User;
import com.nextbar.usersservice.model.UserRoleAssignment;
import com.nextbar.usersservice.repository.RoleRepository;
import com.nextbar.usersservice.repository.ServiceRepository;
import com.nextbar.usersservice.repository.UserRepository;
import com.nextbar.usersservice.repository.UserRoleAssignmentRepository;

@Component
public class DataBootstrap implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataBootstrap.class);

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final ServiceRepository serviceRepository;
    private final UserRoleAssignmentRepository assignmentRepository;
    private final PasswordEncoder passwordEncoder;

    public DataBootstrap(UserRepository userRepository, RoleRepository roleRepository,
            ServiceRepository serviceRepository, UserRoleAssignmentRepository assignmentRepository,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.serviceRepository = serviceRepository;
        this.assignmentRepository = assignmentRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        log.info("Checking data bootstrap for default admin user...");

        // Ensure services exist
        List<String> coreServices = List.of(
                "BAR_SERVICE",
                "EVENT_SERVICE",
                "DROP_POINT_SERVICE",
                "WAREHOUSE_SERVICE"
        );

        for (String serviceCode : coreServices) {
            if (serviceRepository.findByCode(serviceCode).isEmpty()) {
                Service newSvc = new Service();
                newSvc.setCode(serviceCode);
                newSvc.setDescription("Core service: " + serviceCode);
                serviceRepository.save(newSvc);
            }
        }

        // Ensure ADMIN role exists
        Role adminRole = roleRepository.findByName("ADMIN").orElseGet(() -> {
            Role r = new Role();
            r.setName("ADMIN");
            r.setGlobal(true);
            return roleRepository.save(r);
        });

        // Ensure admin user exists
        String defaultAdminUsername = "admin";
        if (userRepository.findByUsername(defaultAdminUsername).isEmpty()) {
            User admin = new User();
            admin.setUsername(defaultAdminUsername);
            admin.setFirstName("System");
            admin.setLastName("Admin");
            admin.setEmail("admin@nextbar.com");
            admin.setPasswordHash(passwordEncoder.encode("admin123"));
            admin.setEnabled(true);
            admin.setLocked(false);
            admin = userRepository.save(admin);

            // Assign ADMIN to all core services
            for (String serviceCode : coreServices) {
                Service service = serviceRepository.findByCode(serviceCode).orElseThrow();
                UserRoleAssignment assignment = new UserRoleAssignment();
                assignment.setUser(admin);
                assignment.setRole(adminRole);
                assignment.setService(service);
                assignmentRepository.save(assignment);
            }
            log.info("Default admin user created successfully (username: {}, password: admin123)", defaultAdminUsername);
        } else {
            log.info("Default admin user already exists, skipping creation");
        }
    }
}
