package com.ecommerce.customer.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.util.UUID;

@Entity
@Table(name = "Members")
@Data
public class Member {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID memberId;
    @Column(nullable = false, length = 50)
    private String username;
    @Column(nullable = false)
    private String password;
    private String email;
    private Boolean gender;
}