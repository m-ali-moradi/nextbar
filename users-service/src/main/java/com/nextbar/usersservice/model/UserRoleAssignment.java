package com.coditects.usersservice.model;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
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
