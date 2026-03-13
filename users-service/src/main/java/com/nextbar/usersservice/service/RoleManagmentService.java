package com.nextbar.usersservice.service;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.lang.NonNull;
import org.springframework.transaction.annotation.Transactional;

import com.nextbar.usersservice.dto.AssignRoleToUserRequestDTO;
import com.nextbar.usersservice.dto.AssignmentResponseDTO;
import com.nextbar.usersservice.dto.RoleResponseDTO;
import com.nextbar.usersservice.dto.ServiceResponseDTO;
import com.nextbar.usersservice.model.Permission;
import com.nextbar.usersservice.model.Role;
import com.nextbar.usersservice.model.Service;
import com.nextbar.usersservice.model.User;
import com.nextbar.usersservice.model.UserRoleAssignment;
import com.nextbar.usersservice.repository.RoleRepository;
import com.nextbar.usersservice.repository.ServiceRepository;
import com.nextbar.usersservice.repository.UserRepository;
import com.nextbar.usersservice.repository.UserRoleAssignmentRepository;

import lombok.RequiredArgsConstructor;

/**
 * Service for managing roles and permissions in the system.
 * This class handles role creation, updates, deletions, and role assignments to
 * users.
 */
@org.springframework.stereotype.Service
@RequiredArgsConstructor
public class RoleManagmentService {

	private final RoleRepository roleRepository;
	private final ServiceRepository serviceRepository;
	private final UserRepository userRepository;
	private final UserRoleAssignmentRepository assignmentRepository;

	/**
	 * Retrieves all roles in the system.
	 * 
	 * @return A list of RoleResponse objects representing all roles.
	 */
	@Transactional(readOnly = true)
	public List<RoleResponseDTO> getAllRoles() {
		return roleRepository.findAll().stream()
				.map(this::toRoleResponse)
				.toList();
	}

	/**
	 * Retrieves a role by its name.
	 * 
	 * @param roleName The name of the role to retrieve.
	 * @return A RoleResponse object representing the role.
	 * @throws IllegalArgumentException if the role is not found.
	 */
	@Transactional(readOnly = true)
	public RoleResponseDTO getRoleByName(String roleName) {
		String normalizedRoleName = requireText(roleName, "roleName").toUpperCase();
		Role role = roleRepository.findByName(normalizedRoleName)
				.orElseThrow(() -> new IllegalArgumentException("Role not found: " + normalizedRoleName));
		return toRoleResponse(role);
	}

	/**
	 * Retrieves all services in the system.
	 * 
	 * @return A list of ServiceResponse objects representing all services.
	 */
	@Transactional(readOnly = true)
	public List<ServiceResponseDTO> getAllServices() {
		return serviceRepository.findAll().stream()
				.map(this::toServiceResponse)
				.toList();
	}

	/**
	 * Assigns a role to a user for a specific service.
	 * 
	 * @param request The AssignRoleToUserRequest containing the user ID, role name,
	 *                and service code.
	 * @return An AssignmentResponse object representing the role assignment.
	 * @throws IllegalArgumentException if the user, role, or service is not found,
	 *                                  or if the role is already assigned to the
	 *                                  user for the service.
	 */
	@Transactional
	public AssignmentResponseDTO assignRoleToUser(AssignRoleToUserRequestDTO request) {
		UUID userId = requireUuid(request.userId(), "userId");
		User user = userRepository.findById(userId)
				.orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

		String roleName = requireText(request.roleName(), "roleName").toUpperCase();
		Role role = roleRepository.findByName(roleName)
				.orElseThrow(() -> new IllegalArgumentException("Role not found: " + roleName));

		String serviceCode = normalizeServiceCode(request.serviceCode());
		Service service = serviceRepository.findByCode(serviceCode)
				.orElseThrow(() -> new IllegalArgumentException("Service not found: " + serviceCode));

		boolean duplicate = assignmentRepository.findByUserAndService(user, service).stream()
				.anyMatch(existing -> existing.getRole() != null
						&& role.getId().equals(existing.getRole().getId())
						&& equalResource(existing.getResourceId(), request.resourceId()));
		if (duplicate) {
			throw new IllegalArgumentException("Assignment already exists for this user/role/service/resource");
		}

		UserRoleAssignment assignment = new UserRoleAssignment();
		assignment.setUser(user);
		assignment.setRole(role);
		assignment.setService(service);
		assignment.setResourceId(request.resourceId());

		return toAssignmentResponse(assignmentRepository.save(assignment));
	}

	/**
	 * Removes a role assignment from a user.
	 * 
	 * @param assignmentId The ID of the role assignment to remove.
	 * @throws IllegalArgumentException if the assignment is not found.
	 */
	@Transactional
	public void removeRoleFromUser(UUID assignmentId) {
		UUID id = requireUuid(assignmentId, "assignmentId");
		UserRoleAssignment assignment = assignmentRepository.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("Assignment not found: " + id));
		assignmentRepository.delete(assignment);
	}

	/**
	 * Retrieves all role assignments for a specific user.
	 * 
	 * @param userId The ID of the user to retrieve role assignments for.
	 * @return A list of AssignmentResponse objects representing the user's role
	 *         assignments.
	 * @throws IllegalArgumentException if the user is not found.
	 */
	@Transactional(readOnly = true)
	public List<AssignmentResponseDTO> getRolesOfUser(UUID userId) {
		UUID id = requireUuid(userId, "userId");
		if (!userRepository.existsById(id)) {
			throw new IllegalArgumentException("User not found: " + id);
		}

		return assignmentRepository.findAllByUser_Id(id).stream()
				.map(this::toAssignmentResponse)
				.toList();
	}

	/**
	 * Retrieves all role assignments for a specific role.
	 * 
	 * @param roleName The name of the role to retrieve role assignments for.
	 * @return A list of AssignmentResponse objects representing the role's role
	 *         assignments.
	 * @throws IllegalArgumentException if the role is not found.
	 */
	@Transactional(readOnly = true)
	public List<AssignmentResponseDTO> getRolesOfRole(String roleName) {
		String normalizedRoleName = requireText(roleName, "roleName").toUpperCase();
		Role role = roleRepository.findByName(normalizedRoleName)
				.orElseThrow(() -> new IllegalArgumentException("Role not found: " + normalizedRoleName));

		return assignmentRepository.findAllByRole_Id(role.getId()).stream()
				.map(this::toAssignmentResponse)
				.toList();
	}

	private RoleResponseDTO toRoleResponse(Role role) {
		Set<String> permissions = role.getPermissions() == null
				? Set.of()
				: role.getPermissions().stream()
						.map(Permission::getCode)
						.collect(java.util.stream.Collectors.toSet());

		return new RoleResponseDTO(role.getId(), role.getName(), role.isGlobal(), permissions);
	}

	private ServiceResponseDTO toServiceResponse(Service service) {
		return new ServiceResponseDTO(service.getId(), service.getCode(), service.getDescription());
	}

	private AssignmentResponseDTO toAssignmentResponse(UserRoleAssignment assignment) {
		return new AssignmentResponseDTO(
				assignment.getId(),
				assignment.getUser().getId(),
				assignment.getUser().getUsername(),
				assignment.getUser().getFirstName(),
				assignment.getUser().getLastName(),
				assignment.getRole().getName(),
				assignment.getService().getCode(),
				assignment.getResourceId());
	}

	private String requireText(String value, String fieldName) {
		if (value == null || value.isBlank()) {
			throw new IllegalArgumentException(fieldName + " is required");
		}
		return value.trim();
	}

	private UUID requireUuid(UUID value, String fieldName) {
		if (value == null) {
			throw new IllegalArgumentException(fieldName + " is required");
		}
		return value;
	}

	private String normalizeServiceCode(String value) {
		String normalized = requireText(value, "serviceCode").toUpperCase();
		if (!normalized.endsWith("_SERVICE")) {
			normalized = normalized + "_SERVICE";
		}
		return normalized;
	}

	private boolean equalResource(UUID left, UUID right) {
		if (left == null && right == null) {
			return true;
		}
		if (left == null || right == null) {
			return false;
		}
		return left.equals(right);
	}

}
