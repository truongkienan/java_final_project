package com.ecommerce.customer.repository;

import com.ecommerce.customer.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface MemberRepository extends JpaRepository<Member, UUID> {
    Member findByUsername(String username);
    boolean existsByUsername(String username);
}