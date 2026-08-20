package com.ecommerce.catalog.controller;

import com.ecommerce.catalog.entity.Category;
import com.ecommerce.catalog.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {
    @Autowired
    private CategoryRepository categoryRepository;

    @GetMapping
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    // API lấy cây danh mục 2 cấp (chỉ danh mục Active) — dùng cho Navbar
    @GetMapping("/tree")
    public List<Category> getCategoryTree() {
        List<Category> roots = categoryRepository.findByActiveTrueAndParentIdIsNullOrderByPositionAsc();
        for (Category root : roots) {
            root.setChildren(categoryRepository.findByActiveTrueAndParentIdOrderByPositionAsc(root.getCategoryId()));
        }
        return roots;
    }

    @PostMapping
    public Category createCategory(@RequestBody Category category) {
        return categoryRepository.save(category);
    }

    // API Lấy thông tin 1 Danh mục theo ID
    @GetMapping("/{id}")
    public Category getCategory(@PathVariable("id") Short id) {
        return categoryRepository.findById(id).orElse(null);
    }

    // API Cập nhật Danh mục
    @PutMapping("/{id}")
    public Category updateCategory(@PathVariable("id") Short id, @RequestBody Category category) {
        // Gán lại ID để Spring Data JPA hiểu là lệnh UPDATE thay vì INSERT
        category.setCategoryId(id);
        return categoryRepository.save(category);
    }

    // API Xóa Danh mục
    @DeleteMapping("/{id}")
    public void deleteCategory(@PathVariable("id") Short id) {
        categoryRepository.deleteById(id);
    }
}