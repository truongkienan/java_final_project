package com.ecommerce.auth.repository;

import com.ecommerce.auth.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PermissionRepository extends JpaRepository<Permission, Short> {
}