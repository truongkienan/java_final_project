package com.ecommerce.catalog.repository;

import com.ecommerce.catalog.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Short> {
    // Danh mục cấp 1 đang Active, sắp theo Position
    List<Category> findByActiveTrueAndParentIdIsNullOrderByPositionAsc();

    // Danh mục con (cấp 2) đang Active của 1 danh mục cha, sắp theo Position
    List<Category> findByActiveTrueAndParentIdOrderByPositionAsc(Short parentId);
}