package com.nextbar.usersservice.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nextbar.usersservice.model.Service;
import com.nextbar.usersservice.model.User;
import com.nextbar.usersservice.model.UserRoleAssignment;

public interface UserRoleAssignmentRepository extends JpaRepository<UserRoleAssignment, UUID> {
    List<UserRoleAssignment> findAllByUser_Id(UUID userId);

    List<UserRoleAssignment> findByUserAndService(User user, Service service);
}

