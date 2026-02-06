package com.nextbar.usersservice.model;

import java.util.UUID;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "user_role_assignments")
public class UserRoleAssignment {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(optional = false)
    private User user;

    @ManyToOne(optional = false)
    private Role role;

    @ManyToOne(optional = false)
    private Service service;

    private UUID resourceId;
    /*
      Example:
      - Bartender → resourceId = barId
      - BarManager → null (all bars)
      - Admin → null + global role
    */
}
