package com.nextbar.eventPlanner.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.nextbar.eventPlanner.model.AssignedStaff;

@Repository
public interface AssignedStaffRepository extends JpaRepository<AssignedStaff, UUID> {
    Optional<AssignedStaff> findByUserId(String userId);
}
