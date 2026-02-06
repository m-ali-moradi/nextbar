package com.nextbar.usersservice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "permissions")
public class Permission {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false, unique = true)
    private String code;
    // BAR_READ, BAR_WRITE, BAR_DELETE, BAR_OPERATE, STOCK_UPDATE, etc.

    private String description;
}
