package com.nextbar.usersservice.service;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.springframework.lang.NonNull;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import com.nextbar.usersservice.dto.AdminCreateUserDTO;
import com.nextbar.usersservice.dto.AssignRoleDTO;
import com.nextbar.usersservice.dto.ChangePasswordRequestDTO;
import com.nextbar.usersservice.dto.LoginRequestDTO;
import com.nextbar.usersservice.dto.UpdateProfileRequestDTO;
import com.nextbar.usersservice.dto.UpdateUserRequestDTO;
import com.nextbar.usersservice.dto.UserAdminDTO;
import com.nextbar.usersservice.dto.UserAssignmentRequestDTO;
import com.nextbar.usersservice.dto.UserProfileDTO;
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

/**
 * Service for managing user-related operations.
 * Handles user registration, login, role management, and other user-specific
 * functionalities.
 */
@org.springframework.stereotype.Service
@RequiredArgsConstructor
public class UserService {

    public record AuthSession(String accessToken, String refreshToken, UserAdminDTO user) {
    }

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final ServiceRepository serviceRepository;
    private final UserRoleAssignmentRepository assignmentRepository;

    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final TokenLifecycleService tokenLifecycleService;
    private final LoginAttemptService loginAttemptService;
    private final UserMapper userMapper;

    /**
     * Creates a new user with admin privileges.
     * 
     * @param request The admin user creation request.
     * @return The created user DTO.
     * @throws IllegalArgumentException if the request is invalid or user already
     *                                  exists.
     */
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
        if (request.firstName() == null || request.firstName().isBlank()) {
            throw new IllegalArgumentException("First name is required");
        }
        if (request.lastName() == null || request.lastName().isBlank()) {
            throw new IllegalArgumentException("Last name is required");
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
        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setEmail(request.email());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setEnabled(true);
        user.setLocked(false);

        User saved = userRepository.save(user);

        // Optional initial assignment (used by admin UI).
        if (request.roleName() != null && !request.roleName().isBlank()) {
            List<UserAssignmentRequestDTO> assignments = List.of(
                    new UserAssignmentRequestDTO(
                            request.serviceCode(),
                            request.roleName(),
                            request.resourceId()));
            replaceAssignments(saved, assignments);
        }

        // Ensure assignments are present for the admin DTO mapping.
        saved.setRoleAssignments(new HashSet<>(assignmentRepository.findAllByUser_Id(saved.getId())));
        return userMapper.toAdminDto(saved);
    }

    /**
     * Logs in an existing user.
     * 
     * @param request The login request containing username and password.
     * @return The login response with access token, refresh token, and user DTO.
     * @throws IllegalArgumentException if the credentials are invalid or user is
     *                                  disabled/locked.
     */
    @Transactional(readOnly = true)
    public AuthSession login(LoginRequestDTO request) {
        String normalizedUsername = normalizeUsernameForLogin(request.username());
        loginAttemptService.verifyAllowed(normalizedUsername);

        User user = userRepository.findByUsername(normalizedUsername)
                .orElseThrow(() -> invalidCredentials(normalizedUsername));

        if (!user.isEnabled() || user.isLocked()) {
            throw invalidCredentials(normalizedUsername);
        }

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw invalidCredentials(normalizedUsername);
        }

        loginAttemptService.recordSuccess(normalizedUsername);

        // Ensure assignments are present for mapping and token enrichment.
        user.setRoleAssignments(new HashSet<>(assignmentRepository.findAllByUser_Id(user.getId())));

        String accessToken = createTokenForUser(user);
        String refreshToken = tokenLifecycleService.createRefreshToken(user);
        return new AuthSession(accessToken, refreshToken, userMapper.toMeDto(user));
    }

    private String normalizeUsernameForLogin(String username) {
        if (username == null) {
            return "";
        }
        return username.trim();
    }

    private IllegalArgumentException invalidCredentials(String username) {
        loginAttemptService.recordFailure(username);
        return new IllegalArgumentException("Invalid username or password");
    }

    /**
     * Refreshes the access token using a refresh token.
     * 
     * @param refreshToken The refresh token to use for token rotation.
     * @return The new login response with updated tokens and user DTO.
     * @throws IllegalArgumentException if the refresh token is invalid.
     */
    @Transactional
    public AuthSession refresh(String refreshToken) {
        User user = tokenLifecycleService.validateRefreshTokenAndResolveUser(refreshToken);
        user.setRoleAssignments(new HashSet<>(assignmentRepository.findAllByUser_Id(user.getId())));

        String accessToken = createTokenForUser(user);
        String rotatedRefreshToken = tokenLifecycleService.rotateRefreshToken(user, refreshToken);

        return new AuthSession(accessToken, rotatedRefreshToken, userMapper.toMeDto(user));
    }

    /**
     * Logs out the current user by revoking their tokens.
     * 
     * @param accessToken  The access token to revoke.
     * @param refreshToken The refresh token to revoke.
     */
    @Transactional
    public void logout(String accessToken, String refreshToken) {
        tokenLifecycleService.revokeAccessToken(accessToken);
        tokenLifecycleService.revokeRefreshToken(refreshToken);
        tokenLifecycleService.cleanupExpiredTokens();
    }

    /**
     * Checks if an access token has been revoked.
     * 
     * @param accessToken The access token to check.
     * @return true if the access token is revoked, false otherwise.
     */
    @Transactional(readOnly = true)
    public boolean isAccessTokenRevoked(String accessToken) {
        if (accessToken == null || accessToken.isBlank()) {
            return true;
        }
        if (!jwtUtil.validateToken(accessToken) || !jwtUtil.isAccessToken(accessToken)) {
            return true;
        }
        return tokenLifecycleService.isAccessTokenRevoked(accessToken);
    }

    /**
     * Retrieves all users.
     * 
     * @return A list of all users.
     */
    @Transactional(readOnly = true)
    public List<UserAdminDTO> getAllUsers() {
        List<User> users = userRepository.findAll();

        users.forEach(
                user -> user.setRoleAssignments(new HashSet<>(assignmentRepository.findAllByUser_Id(user.getId()))));
        return users.stream().map(userMapper::toAdminDto).toList();
    }

    /**
     * Retrieves a user by their ID.
     * 
     * @param id The ID of the user to retrieve.
     * @return The user DTO.
     * @throws IllegalArgumentException if the user is not found.
     */
    @Transactional(readOnly = true)
    public UserAdminDTO getUserById(@NonNull UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        user.setRoleAssignments(new HashSet<>(assignmentRepository.findAllByUser_Id(user.getId())));
        return userMapper.toAdminDto(user);
    }

    /**
     * Retrieves a user by their username.
     * 
     * @param username The username of the user to retrieve.
     * @return The user DTO.
     * @throws IllegalArgumentException if the user is not found.
     */
    @Transactional(readOnly = true)
    public UserAdminDTO getUserByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        user.setRoleAssignments(new HashSet<>(assignmentRepository.findAllByUser_Id(user.getId())));
        return userMapper.toAdminDto(user);
    }

    /**
     * Updates an existing user.
     * 
     * @param id      The ID of the user to update.
     * @param userDTO The updated user data.
     * @return The updated user DTO.
     * @throws IllegalArgumentException if the user is not found.
     */
    @Transactional
    public UserAdminDTO updateUser(@NonNull UUID id, UpdateUserRequestDTO userDTO) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (userDTO.username() != null) {
            user.setUsername(userDTO.username());
        }
        if (userDTO.firstName() != null) {
            user.setFirstName(userDTO.firstName());
        }
        if (userDTO.lastName() != null) {
            user.setLastName(userDTO.lastName());
        }
        if (userDTO.email() != null) {
            user.setEmail(userDTO.email());
        }
        if (userDTO.enabled() != null) {
            user.setEnabled(userDTO.enabled());
        }

        if (userDTO.assignments() != null) {
            replaceAssignments(user, userDTO.assignments());
        }

        User saved = userRepository.save(user);
        saved.setRoleAssignments(new HashSet<>(assignmentRepository.findAllByUser_Id(saved.getId())));
        return userMapper.toAdminDto(saved);
    }

    @Transactional(readOnly = true)
    public String issueWebSocketTicket(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return createWebSocketTicketForUser(user);
    }

    /**
     * Deletes a user by their ID.
     * 
     * @param id The ID of the user to delete.
     * @throws IllegalArgumentException if the user is not found.
     */
    @Transactional
    public void deleteUser(@NonNull UUID id) {
        if (!userRepository.existsById(id)) {
            throw new IllegalArgumentException("User not found");
        }
        userRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public UserProfileDTO getCurrentUserProfile(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        return new UserProfileDTO(
                Objects.requireNonNull(user.getId()),
                user.getUsername(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.isEnabled(),
                user.isLocked());
    }

    @Transactional
    public UserProfileDTO updateCurrentUserProfile(String username, UpdateProfileRequestDTO request) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        String normalizedEmail = request.email().trim().toLowerCase();
        userRepository.findByEmail(normalizedEmail)
                .filter(existing -> !existing.getId().equals(user.getId()))
                .ifPresent(existing -> {
                    throw new IllegalArgumentException("Email already exists");
                });

        user.setEmail(normalizedEmail);
        user.setFirstName(request.firstName().trim());
        user.setLastName(request.lastName().trim());
        User saved = userRepository.save(user);

        return new UserProfileDTO(
                Objects.requireNonNull(saved.getId()),
                saved.getUsername(),
                saved.getFirstName(),
                saved.getLastName(),
                saved.getEmail(),
                saved.isEnabled(),
                saved.isLocked());
    }

    @Transactional
    public void changeCurrentUserPassword(String username, ChangePasswordRequestDTO request) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (!passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Current password is incorrect");
        }

        validateNewPassword(request.newPassword(), request.confirmNewPassword());

        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
    }

    @Transactional
    public void resetUserPassword(@NonNull UUID userId, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        validateNewPassword(newPassword, newPassword);
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    /**
     * Assigns a role to a user.
     * 
     * @param dto The role assignment request.
     * @return The updated user DTO.
     * @throws IllegalArgumentException if the user is not found.
     */
    @Transactional
    public UserAdminDTO assignRole(AssignRoleDTO dto) {
        User user = userRepository.findById(dto.userId())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        String normalizedServiceCode = normalizeServiceCodeForDb(dto.serviceCode());
        String normalizedRoleName = normalizeRoleNameForDb(dto.roleName(), normalizedServiceCode);

        if ("ADMIN".equalsIgnoreCase(normalizedRoleName)) {
            replaceAssignments(user, List.of(new UserAssignmentRequestDTO(null, "ADMIN", null)));
        } else {
            Role role = roleRepository.findByName(normalizedRoleName)
                    .orElseThrow(() -> new IllegalArgumentException("Role not found: " + normalizedRoleName));

            Service service = serviceRepository.findByCode(normalizedServiceCode)
                    .orElseThrow(() -> new IllegalArgumentException("Service not found: " + normalizedServiceCode));

            UserRoleAssignment assignment = new UserRoleAssignment();
            assignment.setUser(user);
            assignment.setRole(role);
            assignment.setService(service);
            assignment.setResourceId(dto.resourceId());

            assignmentRepository.save(assignment);
        }

        User reloaded = userRepository.findById(user.getId()).orElseThrow();
        reloaded.setRoleAssignments(new HashSet<>(assignmentRepository.findAllByUser_Id(reloaded.getId())));
        return userMapper.toAdminDto(reloaded);
    }

    /**
     * Replaces all role assignments for a user.
     * 
     * @param userId      The ID of the user.
     * @param assignments The new list of role assignments.
     * @return The updated user DTO.
     * @throws IllegalArgumentException if the user is not found.
     */
    @Transactional
    public UserAdminDTO replaceAssignments(@NonNull UUID userId, List<UserAssignmentRequestDTO> assignments) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        replaceAssignments(user, assignments);

        User reloaded = userRepository.findById(user.getId()).orElseThrow();
        reloaded.setRoleAssignments(new HashSet<>(assignmentRepository.findAllByUser_Id(reloaded.getId())));
        return userMapper.toAdminDto(reloaded);
    }

    /**
     * Removes a role assignment for a user.
     * 
     * @param userId       The ID of the user.
     * @param assignmentId The ID of the assignment to remove.
     * @return The updated user DTO.
     * @throws IllegalArgumentException if the user or assignment is not found, or
     *                                  if the assignment does not belong to the
     *                                  user.
     */
    @Transactional
    public UserAdminDTO removeAssignment(@NonNull UUID userId, @NonNull UUID assignmentId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        UserRoleAssignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new IllegalArgumentException("Assignment not found"));

        if (!assignment.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("Assignment does not belong to user");
        }

        assignmentRepository.delete(assignment);

        User reloaded = userRepository.findById(user.getId()).orElseThrow();
        reloaded.setRoleAssignments(new HashSet<>(assignmentRepository.findAllByUser_Id(reloaded.getId())));
        return userMapper.toAdminDto(reloaded);
    }

    /**
     * Checks if a user has a specific permission.
     * 
     * @param userId      The ID of the user.
     * @param serviceCode The code of the service.
     * @param permission  The permission to check.
     * @param resourceId  The ID of the resource.
     * @return true if the user has the permission, false otherwise.
     * @throws IllegalArgumentException if the user or service is not found.
     */
    @Transactional(readOnly = true)
    public boolean hasPermission(@NonNull UUID userId, String serviceCode, String permission, UUID resourceId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Service service = serviceRepository.findByCode(serviceCode)
                .orElseThrow(() -> new IllegalArgumentException("Service not found"));

        List<UserRoleAssignment> assignments = assignmentRepository.findByUserAndService(user, service);

        for (UserRoleAssignment assignment : assignments) {
            Role role = assignment.getRole();
            boolean hasPermission = role.getPermissions() != null
                    && role.getPermissions().stream()
                            .anyMatch(p -> p != null && permission.equalsIgnoreCase(p.getCode()));

            if (hasPermission) {
                // If resourceId is specified, check if it matches
                if (resourceId == null || resourceId.equals(assignment.getResourceId())) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Creates a token for a user.
     * 
     * @param user The user to create a token for.
     * @return The token.
     */
    private String createTokenForUser(User user) {
        List<UserRoleAssignment> assignments = new ArrayList<>(
                assignmentRepository.findAllByUser_Id(user.getId()));

        List<String> assignmentClaims = assignments.stream()
                .map(a -> {
                    String serviceCode = a.getService() != null ? a.getService().getCode() : null;
                    String roleName = a.getRole() != null ? a.getRole().getName() : null;
                    String resourceId = a.getResourceId() != null ? a.getResourceId().toString() : "*";
                    return normalizeService(serviceCode) + ":" + normalizeRole(roleName) + ":" + resourceId;
                })
                .distinct()
                .sorted()
                .toList();

        List<String> roles = assignments.stream()
                .map(a -> normalizeRole(a.getRole() != null ? a.getRole().getName() : null))
                .distinct()
                .sorted()
                .toList();

        return jwtUtil.generateToken(user.getUsername(), roles, assignmentClaims);
    }

    private String createWebSocketTicketForUser(User user) {
        List<UserRoleAssignment> assignments = new ArrayList<>(
                assignmentRepository.findAllByUser_Id(user.getId()));

        List<String> assignmentClaims = assignments.stream()
                .map(a -> {
                    String serviceCode = a.getService() != null ? a.getService().getCode() : null;
                    String roleName = a.getRole() != null ? a.getRole().getName() : null;
                    String resourceId = a.getResourceId() != null ? a.getResourceId().toString() : "*";
                    return normalizeService(serviceCode) + ":" + normalizeRole(roleName) + ":" + resourceId;
                })
                .distinct()
                .sorted()
                .toList();

        List<String> roles = assignments.stream()
                .map(a -> normalizeRole(a.getRole() != null ? a.getRole().getName() : null))
                .distinct()
                .sorted()
                .toList();

        return jwtUtil.generateWebSocketTicket(user.getUsername(), roles, assignmentClaims);
    }

    private static String normalizeService(String serviceCode) {
        if (serviceCode == null)
            return "";
        String s = serviceCode.trim().toUpperCase();
        if (s.endsWith("_SERVICE"))
            s = s.substring(0, s.length() - "_SERVICE".length());
        if (s.endsWith("-SERVICE"))
            s = s.substring(0, s.length() - "-SERVICE".length());
        if (s.equals("DROPPOINT") || s.equals("DROP_POINT"))
            return "DROP_POINT";
        if (s.equals("DROPPPOINTS") || s.equals("DROPPOINTS"))
            return "DROP_POINT";
        return s;
    }

    private static String normalizeServiceCodeForDb(String input) {
        if (input == null)
            return "";
        String s = input.trim().toUpperCase();
        if (s.isEmpty())
            return "";

        if (s.equals("BAR") || s.equals("BAR_SERVICE"))
            return "BAR_SERVICE";
        if (s.equals("EVENT") || s.equals("EVENT_SERVICE"))
            return "EVENT_SERVICE";
        if (s.equals("WAREHOUSE") || s.equals("WAREHOUSE_SERVICE"))
            return "WAREHOUSE_SERVICE";
        if (s.equals("DROPPOINT") || s.equals("DROPPOINTS") || s.equals("DROP_POINT")
                || s.equals("DROP_POINT_SERVICE")) {
            return "DROP_POINT_SERVICE";
        }

        if (!s.endsWith("_SERVICE"))
            return s + "_SERVICE";
        return s;
    }

    private static String normalizeRoleNameForDb(String roleName, String normalizedServiceCodeForDb) {
        if (roleName == null)
            return "";
        String r = roleName.trim().toUpperCase();
        if (r.isEmpty())
            return "";

        if (r.equals("ADMIN"))
            return "ADMIN";

        String servicePrefix = normalizedServiceCodeForDb;
        if (servicePrefix.endsWith("_SERVICE")) {
            servicePrefix = servicePrefix.substring(0, servicePrefix.length() - "_SERVICE".length());
        }

        if (r.equals("MANAGER")) {
            return servicePrefix + "_MANAGER";
        }

        if (r.equals("OPERATOR") || r.equals("BARTENDER")) {
            if (servicePrefix.equals("BAR"))
                return "BARTENDER";
            return servicePrefix + "_OPERATOR";
        }

        return r;
    }

    private static String normalizeRole(String roleName) {
        if (roleName == null)
            return "";
        String r = roleName.trim().toUpperCase();
        if (r.equals("ADMIN"))
            return "ADMIN";
        if (r.contains("MANAGER"))
            return "MANAGER";
        if (r.contains("BARTENDER") || r.contains("OPERATOR"))
            return "OPERATOR";
        return r;
    }

    private void validateNewPassword(String newPassword, String confirmNewPassword) {
        if (newPassword == null || newPassword.isBlank()) {
            throw new IllegalArgumentException("New password is required");
        }
        if (newPassword.length() < 6) {
            throw new IllegalArgumentException("New password must be at least 6 characters");
        }
        if (confirmNewPassword == null || !newPassword.equals(confirmNewPassword)) {
            throw new IllegalArgumentException("New password and confirmation do not match");
        }
    }

    @Transactional
    public void syncStaffResourceAssignment(String staffId, String serviceCode, Long resourceId) {
        if (staffId == null || staffId.isBlank() || serviceCode == null || serviceCode.isBlank()
                || resourceId == null) {
            return;
        }

        UUID userId;
        try {
            userId = UUID.fromString(staffId.trim());
        } catch (IllegalArgumentException ex) {
            return;
        }

        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return;
        }

        String normalizedServiceCode = normalizeServiceCodeForDb(serviceCode);
        if (normalizedServiceCode.isBlank()) {
            return;
        }

        Service service = serviceRepository.findByCode(normalizedServiceCode).orElse(null);
        if (service == null) {
            return;
        }

        String targetRole = normalizeRoleNameForDb("OPERATOR", normalizedServiceCode);
        UUID mappedResourceId = toStableUuid(resourceId);

        List<UserRoleAssignment> assignments = assignmentRepository.findByUserAndService(user, service);
        for (UserRoleAssignment assignment : assignments) {
            if (assignment.getRole() != null && targetRole.equalsIgnoreCase(assignment.getRole().getName())) {
                assignment.setResourceId(mappedResourceId);
                assignmentRepository.save(assignment);
                return;
            }
        }

        Role role = roleRepository.findByName(targetRole).orElse(null);
        if (role == null) {
            return;
        }

        UserRoleAssignment assignment = new UserRoleAssignment();
        assignment.setUser(user);
        assignment.setService(service);
        assignment.setRole(role);
        assignment.setResourceId(mappedResourceId);
        assignmentRepository.save(assignment);
    }

    @Transactional
    public void clearStaffResourceAssignment(String staffId, String serviceCode, Long resourceId) {
        if (staffId == null || staffId.isBlank() || serviceCode == null || serviceCode.isBlank()
                || resourceId == null) {
            return;
        }

        UUID userId;
        try {
            userId = UUID.fromString(staffId.trim());
        } catch (IllegalArgumentException ex) {
            return;
        }

        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return;
        }

        String normalizedServiceCode = normalizeServiceCodeForDb(serviceCode);
        if (normalizedServiceCode.isBlank()) {
            return;
        }

        Service service = serviceRepository.findByCode(normalizedServiceCode).orElse(null);
        if (service == null) {
            return;
        }

        UUID mappedResourceId = toStableUuid(resourceId);

        List<UserRoleAssignment> assignments = assignmentRepository.findByUserAndService(user, service);
        for (UserRoleAssignment assignment : assignments) {
            if (mappedResourceId.equals(assignment.getResourceId())) {
                assignment.setResourceId(null);
                assignmentRepository.save(assignment);
            }
        }
    }

    private UUID toStableUuid(Long sourceId) {
        return UUID.nameUUIDFromBytes(("int-" + sourceId).getBytes(StandardCharsets.UTF_8));
    }

    private void replaceAssignments(User user, List<UserAssignmentRequestDTO> assignments) {
        assignmentRepository.deleteByUser_Id(user.getId());

        if (assignments == null || assignments.isEmpty()) {
            return;
        }

        boolean hasAdmin = assignments.stream()
                .anyMatch(a -> a != null && "ADMIN".equalsIgnoreCase(a.roleName()));

        if (hasAdmin) {
            List<String> adminServices = List.of(
                    "BAR_SERVICE",
                    "EVENT_SERVICE",
                    "DROP_POINT_SERVICE",
                    "WAREHOUSE_SERVICE");

            for (String serviceCode : adminServices) {
                Service service = serviceRepository.findByCode(serviceCode)
                        .orElseThrow(() -> new IllegalArgumentException("Service not found: " + serviceCode));

                Role role = roleRepository.findByName("ADMIN")
                        .orElseThrow(() -> new IllegalArgumentException("Role not found: ADMIN"));

                UserRoleAssignment assignment = new UserRoleAssignment();
                assignment.setUser(user);
                assignment.setService(service);
                assignment.setRole(role);
                assignment.setResourceId(null);
                assignmentRepository.save(assignment);
            }
            return;
        }

        for (UserAssignmentRequestDTO assignmentDto : assignments) {
            if (assignmentDto == null)
                continue;

            String normalizedServiceCode = normalizeServiceCodeForDb(assignmentDto.serviceCode());
            String normalizedRoleName = normalizeRoleNameForDb(assignmentDto.roleName(), normalizedServiceCode);

            if (normalizedServiceCode.isBlank() || normalizedRoleName.isBlank()) {
                continue;
            }

            Service service = serviceRepository.findByCode(normalizedServiceCode)
                    .orElseThrow(() -> new IllegalArgumentException("Service not found: " + normalizedServiceCode));

            Role role = roleRepository.findByName(normalizedRoleName)
                    .orElseThrow(() -> new IllegalArgumentException("Role not found: " + normalizedRoleName));

            UserRoleAssignment assignment = new UserRoleAssignment();
            assignment.setUser(user);
            assignment.setService(service);
            assignment.setRole(role);
            assignment.setResourceId(assignmentDto.resourceId());
            assignmentRepository.save(assignment);
        }
    }
}
