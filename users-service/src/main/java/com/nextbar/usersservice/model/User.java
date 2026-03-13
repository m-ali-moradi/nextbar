package com.nextbar.usersservice.model;

import java.util.Set;
import java.util.UUID;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Represents a user in the system.
 * This entity is used to store and manage user information in the database.
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = "roleAssignments")
@ToString(exclude = "roleAssignments")
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;
    
    @Column(nullable = false)
    private String passwordHash;

    private boolean enabled;
    private boolean locked;

    @Column(name = "mfa_enabled", nullable = false, columnDefinition = "TINYINT(1) DEFAULT 0")
    private boolean mfaEnabled = false;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private Set<UserRoleAssignment> roleAssignments;
}
