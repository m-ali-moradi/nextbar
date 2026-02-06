package com.nextbar.usersservice.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import com.nextbar.usersservice.dto.AdminCreateUserDTO;
import com.nextbar.usersservice.dto.AssignRoleDTO;
import com.nextbar.usersservice.dto.CreateUserDTO;
import com.nextbar.usersservice.dto.LoginRequestDTO;
import com.nextbar.usersservice.dto.LoginResponseDTO;
import com.nextbar.usersservice.dto.UserAdminDTO;
import com.nextbar.usersservice.mapper.UserMapper;
import com.nextbar.usersservice.model.Role;
import com.nextbar.usersservice.model.Service;
import com.nextbar.usersservice.model.User;
import com.nextbar.usersservice.model.UserRoleAssignment;
import com.nextbar.usersservice.repository.RoleRepository;
import com.nextbar.usersservice.repository.ServiceRepository;
import com.nextbar.usersservice.repository.UserRepository;
import com.nextbar.usersservice.repository.UserRoleAssignmentRepository;
import com.nextbar.usersservice.util.JwtUtil;

import lombok.RequiredArgsConstructor;

@org.springframework.stereotype.Service
@RequiredArgsConstructor 
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final ServiceRepository serviceRepository;
    private final UserRoleAssignmentRepository assignmentRepository;

    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final UserMapper userMapper;


    @Transactional
    public LoginResponseDTO register(CreateUserDTO request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new IllegalArgumentException("Username already exists");
        }
        if (userRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("Email already exists");
        }

        User user = new User();
        user.setUsername(request.username());
        user.setEmail(request.email());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setEnabled(true);
        user.setLocked(false);

        User saved = userRepository.save(user);

        // Ensure assignments are present for mapping and token enrichment.
        saved.setRoleAssignments(new HashSet<>(assignmentRepository.findAllByUser_Id(saved.getId())));

        String token = createTokenForUser(saved);
        return new LoginResponseDTO(token, userMapper.toMeDto(saved));
    }

    @Transactional
    public UserAdminDTO createUserAdmin(AdminCreateUserDTO request) {
        if (request == null) {
            throw new IllegalArgumentException("Request is required");
        }
        if (request.username() == null || request.username().isBlank()) {
            throw new IllegalArgumentException("Username is required");
        }
        if (request.email() == null || request.email().isBlank()) {
            throw new IllegalArgumentException("Email is required");
        }
        if (request.password() == null || request.password().isBlank()) {
            throw new IllegalArgumentException("Password is required");
        }

        if (userRepository.existsByUsername(request.username())) {
            throw new IllegalArgumentException("Username already exists");
        }
        if (userRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("Email already exists");
        }

        User user = new User();
        user.setUsername(request.username());
        user.setEmail(request.email());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setEnabled(true);
        user.setLocked(false);

        User saved = userRepository.save(user);

        // Optional initial assignment (used by admin UI).
        if (request.roleName() != null && !request.roleName().isBlank()
                && request.serviceCode() != null && !request.serviceCode().isBlank()) {
            String normalizedServiceCode = normalizeServiceCodeForDb(request.serviceCode());
            Service service = serviceRepository.findByCode(normalizedServiceCode)
                    .orElseThrow(() -> new IllegalArgumentException("Service not found: " + normalizedServiceCode));

            String normalizedRoleName = normalizeRoleNameForDb(request.roleName(), normalizedServiceCode);
            Role role = roleRepository.findByName(normalizedRoleName)
                    .orElseThrow(() -> new IllegalArgumentException("Role not found: " + normalizedRoleName));

            UserRoleAssignment assignment = new UserRoleAssignment();
            assignment.setUser(saved);
            assignment.setRole(role);
            assignment.setService(service);
            assignment.setResourceId(request.resourceId());
            assignmentRepository.save(assignment);
        }

        // Ensure assignments are present for the admin DTO mapping.
        saved.setRoleAssignments(new HashSet<>(assignmentRepository.findAllByUser_Id(saved.getId())));
        return userMapper.toAdminDto(saved);
    }

    @Transactional(readOnly = true)
    public LoginResponseDTO login(LoginRequestDTO request) {
        User user = userRepository.findByUsername(request.username())
                .orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));

        if (!user.isEnabled() || user.isLocked()) {
            throw new IllegalArgumentException("User is disabled or locked");
        }

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid credentials");
        }

        // Ensure assignments are present for mapping and token enrichment.
        user.setRoleAssignments(new HashSet<>(assignmentRepository.findAllByUser_Id(user.getId())));

        String token = createTokenForUser(user);
        return new LoginResponseDTO(token, userMapper.toMeDto(user));
    }

    @Transactional(readOnly = true)
    public List<UserAdminDTO> getAllUsers() {
        List<User> users = userRepository.findAll();

        users.forEach(user -> 
            user.setRoleAssignments(new HashSet<>(assignmentRepository.findAllByUser_Id(user.getId())))
        );
        return users.stream().map(userMapper::toAdminDto).toList();
    }

    @Transactional(readOnly = true)
    public UserAdminDTO getUserById(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        user.setRoleAssignments(new HashSet<>(assignmentRepository.findAllByUser_Id(user.getId())));
    }

    @Transactional(readOnly = true)
    public UserAdminDTO getUserByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return userMapper.toAdminDto(user);
    }

    @Transactional
    public UserAdminDTO updateUser(UUID id, UserAdminDTO userDTO) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        user.setUsername(userDTO.username());
        user.setEmail(userDTO.email());
        user.setEnabled(userDTO.enabled());

        return userMapper.toAdminDto(userRepository.save(user));
    }

    @Transactional
    public void deleteUser(UUID id) {
        if (!userRepository.existsById(id)) {
            throw new IllegalArgumentException("User not found");
        }
        userRepository.deleteById(id);
    }

    @Transactional
    public UserAdminDTO assignRole(AssignRoleDTO dto) {
        User user = userRepository.findById(dto.userId())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Role role = roleRepository.findByName(dto.roleName())
                .orElseThrow(() -> new IllegalArgumentException("Role not found"));

        Service service = serviceRepository.findByCode(dto.serviceCode())
                .orElseThrow(() -> new IllegalArgumentException("Service not found"));

        UserRoleAssignment assignment = new UserRoleAssignment();
        assignment.setUser(user);
        assignment.setRole(role);
        assignment.setService(service);
        assignment.setResourceId(dto.resourceId());

        assignmentRepository.save(assignment);

        User reloaded = userRepository.findById(user.getId()).orElseThrow();
        reloaded.setRoleAssignments(new HashSet<>(assignmentRepository.findAllByUser_Id(reloaded.getId())));
        return userMapper.toAdminDto(reloaded);
    }
    @Transactional(readOnly = true)
    // assign permission to user for a specific service and resource
    public boolean hasPermission(UUID userId, String serviceCode, String permission, UUID resourceId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Service service = serviceRepository.findByCode(serviceCode)
                .orElseThrow(() -> new IllegalArgumentException("Service not found"));

        List<UserRoleAssignment> assignments = assignmentRepository.findByUserAndService(user, service);

        for (UserRoleAssignment assignment : assignments) {
            Role role = assignment.getRole();
            boolean hasPermission = role.getPermissions() != null
                    && role.getPermissions().stream().anyMatch(p -> p != null && permission.equalsIgnoreCase(p.getCode()));

            if (hasPermission) {
                // If resourceId is specified, check if it matches
                if (resourceId == null || resourceId.equals(assignment.getResourceId())) {
                    return true;
                }
            }
        }
        return false;
    }

    private String createTokenForUser(User user) {
        List<UserRoleAssignment> assignments = new ArrayList<>(
                assignmentRepository.findAllByUser_Id(user.getId())
        );

        List<String> assignmentClaims = assignments.stream()
                .map(a -> normalizeService(a.getService() != null ? a.getService().getCode() : null)
                        + ":" + normalizeRole(a.getRole() != null ? a.getRole().getName() : null)
                        + ":" + (a.getResourceId() == null ? "*" : a.getResourceId().toString()))
                .distinct()
                .toList();

        List<String> roles = assignments.stream()
                .map(a -> normalizeRole(a.getRole() != null ? a.getRole().getName() : null))
                .distinct()
                .toList();

        return jwtUtil.generateToken(user.getUsername(), roles, assignmentClaims);
    }

    private static String normalizeService(String serviceCode) {
        if (serviceCode == null) return "";
        String s = serviceCode.trim().toUpperCase();
        if (s.endsWith("_SERVICE")) s = s.substring(0, s.length() - "_SERVICE".length());
        if (s.endsWith("-SERVICE")) s = s.substring(0, s.length() - "-SERVICE".length());
        if (s.equals("DROPPOINT") || s.equals("DROP_POINT")) return "DROP_POINT";
        if (s.equals("DROPPPOINTS") || s.equals("DROPPOINTS")) return "DROP_POINT";
        return s;
    }

    private static String normalizeServiceCodeForDb(String input) {
        if (input == null) return "";
        String s = input.trim().toUpperCase();
        if (s.isEmpty()) return "";

        // Allow frontend to send short codes.
        if (s.equals("BAR") || s.equals("BAR_SERVICE")) return "BAR_SERVICE";
        if (s.equals("EVENT") || s.equals("EVENT_SERVICE")) return "EVENT_SERVICE";
        if (s.equals("WAREHOUSE") || s.equals("WAREHOUSE_SERVICE")) return "WAREHOUSE_SERVICE";
        if (s.equals("DROPPOINT") || s.equals("DROPPOINTS") || s.equals("DROP_POINT") || s.equals("DROP_POINT_SERVICE")) {
            return "DROP_POINT_SERVICE";
        }

        // Fallback: append _SERVICE if it looks like a short code.
        if (!s.endsWith("_SERVICE")) return s + "_SERVICE";
        return s;
    }

    private static String normalizeRoleNameForDb(String roleName, String normalizedServiceCodeForDb) {
        if (roleName == null) return "";
        String r = roleName.trim().toUpperCase();
        if (r.isEmpty()) return "";

        if (r.equals("ADMIN")) return "ADMIN";

        // The UI uses MANAGER/OPERATOR but the DB uses service-specific roles.
        String servicePrefix = normalizedServiceCodeForDb;
        if (servicePrefix.endsWith("_SERVICE")) {
            servicePrefix = servicePrefix.substring(0, servicePrefix.length() - "_SERVICE".length());
        }

        if (r.equals("MANAGER")) {
            return servicePrefix + "_MANAGER";
        }

        if (r.equals("OPERATOR") || r.equals("BARTENDER")) {
            if (servicePrefix.equals("BAR")) return "BARTENDER"; // keep existing seeded role name
            return servicePrefix + "_OPERATOR";
        }

        // Allow callers to pass DB role names directly (e.g., BAR_MANAGER)
        return r;
    }

    private static String normalizeRole(String roleName) {
        if (roleName == null) return "";
        String r = roleName.trim().toUpperCase();
        if (r.equals("ADMIN")) return "ADMIN";
        if (r.contains("MANAGER")) return "MANAGER";
        if (r.contains("BARTENDER") || r.contains("OPERATOR")) return "OPERATOR";
        return r;
    }
}
