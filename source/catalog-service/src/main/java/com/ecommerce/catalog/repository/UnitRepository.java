package com.ecommerce.catalog.repository;

import com.ecommerce.catalog.entity.Unit;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UnitRepository extends JpaRepository<Unit, Short> {
}
