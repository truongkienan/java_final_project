package com.ecommerce.auth.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "permissions")
@Data
public class Permission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Short permissionId;

    @Column(nullable = false, unique = true, length = 50)
    private String permissionName;

    private String description;
}