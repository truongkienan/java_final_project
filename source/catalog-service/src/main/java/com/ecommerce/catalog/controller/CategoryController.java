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
    public List<Category> getCategories() {
        return categoryRepository.findAll();
    }

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

    @GetMapping("/{id}")
    public Category getCategory(@PathVariable("id") Short id) {
        return categoryRepository.findById(id).orElse(null);
    }

    @PutMapping("/{id}")
    public Category updateCategory(@PathVariable("id") Short id, @RequestBody Category category) {
        category.setCategoryId(id);
        return categoryRepository.save(category);
    }

    @DeleteMapping("/{id}")
    public void deleteCategory(@PathVariable("id") Short id) {
        categoryRepository.deleteById(id);
    }
}