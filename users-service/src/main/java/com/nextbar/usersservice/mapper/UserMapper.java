package com.nextbar.usersservice.mapper;

import com.nextbar.usersservice.dto.UserAdminDTO;
import com.nextbar.usersservice.dto.UserRoleAssignmentDTO;
import com.nextbar.usersservice.dto.UserRoleDTO;
import com.nextbar.usersservice.model.User;
import com.nextbar.usersservice.model.UserRoleAssignment;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * Mapper class for converting User entities to DTOs.
 */
@Component
public class UserMapper {

        /**
         * Converts a User entity to a UserAdminDTO.
         * 
         * @param user The User entity to convert.
         * @return The UserAdminDTO.
         */
        public UserAdminDTO toAdminDto(User user) {
                if (user == null)
                        return null;
                return new UserAdminDTO(
                                Objects.requireNonNull(user.getId()),
                                user.getUsername(),
                                user.getFirstName(),
                                user.getLastName(),
                                user.getEmail(),
                                user.isEnabled(),
                                toAssignmentDtos(user));
        }

        /**
         * Converts a User entity to a UserMeDTO.
         * 
         * @param user The User entity to convert.
         * @return The UserMeDTO.
         */
        public UserAdminDTO toMeDto(User user) {
                if (user == null)
                        return null;
                return toAdminDto(user);
        }

        /**
         * Converts a User entity to a list of UserRoleAssignmentDTOs.
         * 
         * @param user The User entity to convert.
         * @return The list of UserRoleAssignmentDTOs.
         */
        public List<UserRoleAssignmentDTO> toAssignmentDtos(User user) {
                if (user == null || user.getRoleAssignments() == null)
                        return Collections.emptyList();

                return user.getRoleAssignments().stream()
                                .filter(Objects::nonNull)
                                .map(this::toAssignmentDto)
                                .sorted(Comparator
                                                .comparing(UserRoleAssignmentDTO::service,
                                                                Comparator.nullsLast(String::compareTo))
                                                .thenComparing(UserRoleAssignmentDTO::role,
                                                                Comparator.nullsLast(String::compareTo)))
                                .toList();
        }

        /**
         * Converts a UserRoleAssignment entity to a UserRoleAssignmentDTO.
         * 
         * @param assignment The UserRoleAssignment entity to convert.
         * @return The UserRoleAssignmentDTO.
         */
        public UserRoleAssignmentDTO toAssignmentDto(UserRoleAssignment assignment) {
                if (assignment == null)
                        return null;

                String serviceCode = assignment.getService() != null ? assignment.getService().getCode() : null;
                String roleName = assignment.getRole() != null ? assignment.getRole().getName() : null;

                return new UserRoleAssignmentDTO(
                                assignment.getId(),
                                serviceCode,
                                roleName,
                                assignment.getResourceId());
        }

        /**
         * Converts a User entity to a list of UserRoleDTOs.
         * 
         * @param user The User entity to convert.
         * @return The list of UserRoleDTOs.
         */
        public List<UserRoleDTO> toRoleDtos(User user) {
                if (user == null || user.getRoleAssignments() == null)
                        return Collections.emptyList();

                return user.getRoleAssignments().stream()
                                .filter(Objects::nonNull)
                                .map(a -> new UserRoleDTO(
                                                a.getService() != null ? a.getService().getCode() : null,
                                                a.getRole() != null ? a.getRole().getName() : null,
                                                a.getResourceId()))
                                .sorted(Comparator
                                                .comparing(UserRoleDTO::service,
                                                                Comparator.nullsLast(String::compareTo))
                                                .thenComparing(UserRoleDTO::role,
                                                                Comparator.nullsLast(String::compareTo)))
                                .toList();
        }
}
