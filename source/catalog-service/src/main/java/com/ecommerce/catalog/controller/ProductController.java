package com.ecommerce.catalog.controller;

import com.ecommerce.catalog.entity.Product;
import com.ecommerce.catalog.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    @Autowired
    private ProductRepository productRepository;

    @GetMapping
    public List<Product> getProducts() {
        return productRepository.findAll();
    }

    @PostMapping
    public ResponseEntity<?> createProduct(@RequestBody Product product) {
        String error = validate(product);
        if (error != null) {
            return ResponseEntity.badRequest().body(Map.of("error", error));
        }
        return ResponseEntity.ok(productRepository.save(product));
    }

    @GetMapping("/{id}")
    public Product getProduct(@PathVariable("id") Integer id) {
        return productRepository.findById(id).orElse(null);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateProduct(@PathVariable("id") Integer id, @RequestBody Product product) {
        String error = validate(product);
        if (error != null) {
            return ResponseEntity.badRequest().body(Map.of("error", error));
        }
        product.setProductId(id);
        return ResponseEntity.ok(productRepository.save(product));
    }

    private String validate(Product product) {
        if (product.getCategory() == null || product.getCategory().getCategoryId() == null) {
            return "Category is required";
        }
        if (product.getUnit() == null || product.getUnit().getUnitId() == null) {
            return "Unit is required";
        }
        return null;
    }

    @DeleteMapping("/{id}")
    public void deleteProduct(@PathVariable("id") Integer id) {
        productRepository.deleteById(id);
    }

}
