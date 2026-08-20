package com.ecommerce.catalog.repository;

import com.ecommerce.catalog.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Integer> {
    // Custom query: Tìm toàn bộ sản phẩm dựa trên Id danh mục
    List<Product> findByCategoryId(Short categoryId);
}