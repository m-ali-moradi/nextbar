package com.nextbar.usersservice.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nextbar.usersservice.dto.AssignRoleToUserRequestDTO;
import com.nextbar.usersservice.dto.RoleResponseDTO;
import com.nextbar.usersservice.dto.ServiceResponseDTO;
import com.nextbar.usersservice.service.RoleManagmentService;

import jakarta.validation.Valid;

/*
 * Controller for managing user roles and permissions.
 * This controller provides endpoints for creating, updating, and deleting
 * roles,
 * as well as assigning permissions to roles and users.
 */
@RestController
@RequestMapping("/api/v1/users/management")
public class RoleManagmentController {

    private final RoleManagmentService roleManagmentService;

    public RoleManagmentController(RoleManagmentService roleManagmentService) {
        this.roleManagmentService = roleManagmentService;
    }

    /**
     * Get all roles.
     * 
     * @return A list of all roles.
     */
    @GetMapping("/roles")
    public ResponseEntity<List<RoleResponseDTO>> getAllRoles() {
        return ResponseEntity.ok(roleManagmentService.getAllRoles());
    }

    /**
     * Get a role by name.
     * 
     * @param roleName The name of the role.
     * @return The role.
     */
    @GetMapping("/roles/{roleName}")
    public ResponseEntity<?> getRoleByName(@PathVariable String roleName) {
        return ResponseEntity.ok(roleManagmentService.getRoleByName(roleName));
    }

    /**
     * Get all services.
     * 
     * @return A list of all services.
     */
    @GetMapping("/services")
    public ResponseEntity<List<ServiceResponseDTO>> getAllServices() {
        return ResponseEntity.ok(roleManagmentService.getAllServices());
    }

    /**
     * Assign a role to a user.
     * 
     * @param request The role assignment request.
     * @return The role assignment.
     */
    @PostMapping("/assignments")
    public ResponseEntity<?> assignRoleToUser(@Valid @RequestBody AssignRoleToUserRequestDTO request) {
        return ResponseEntity.ok(roleManagmentService.assignRoleToUser(request));
    }

    /**
     * Remove a role from a user.
     * 
     * @param assignmentId The ID of the role assignment.
     * @return A success response.
     */
    @DeleteMapping("/assignments/{assignmentId}")
    public ResponseEntity<?> removeRoleFromUser(@PathVariable UUID assignmentId) {
        roleManagmentService.removeRoleFromUser(assignmentId);
        return ResponseEntity.ok(new SuccessResponse("Assignment removed successfully"));
    }

    /**
     * Get all roles of a user.
     * 
     * @param userId The ID of the user.
     * @return A list of all roles of the user.
     */
    @GetMapping("/users/{userId}/roles")
    public ResponseEntity<?> getRolesOfUser(@PathVariable UUID userId) {
        return ResponseEntity.ok(roleManagmentService.getRolesOfUser(userId));
    }

    /**
     * Get all services of a role.
     * 
     * @param roleName The name of the role.
     * @return A list of all services of the role.
     */
    @GetMapping("/roles/{roleName}/assignments")
    public ResponseEntity<?> getAssignmentsOfRole(@PathVariable String roleName) {
        return ResponseEntity.ok(roleManagmentService.getRolesOfRole(roleName));
    }

    /**
     * Success response record.
     * 
     * @param message The success message.
     */
    record SuccessResponse(String message) {
    }

}
