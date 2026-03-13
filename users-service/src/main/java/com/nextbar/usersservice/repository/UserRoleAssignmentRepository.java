package com.nextbar.usersservice.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nextbar.usersservice.model.Service;
import com.nextbar.usersservice.model.User;
import com.nextbar.usersservice.model.UserRoleAssignment;

/**
 * Repository for managing UserRoleAssignment entities.
 * Provides standard CRUD operations and custom query methods for
 * UserRoleAssignment.
 */
public interface UserRoleAssignmentRepository extends JpaRepository<UserRoleAssignment, UUID> {
    List<UserRoleAssignment> findAllByUser_Id(UUID userId);

    List<UserRoleAssignment> findAllByRole_Id(UUID roleId);

    List<UserRoleAssignment> findByUserAndService(User user, Service service);

    void deleteByUser_Id(UUID userId);
}
