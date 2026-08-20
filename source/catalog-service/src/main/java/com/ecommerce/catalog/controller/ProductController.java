package com.ecommerce.catalog.controller;

import com.ecommerce.catalog.entity.Product;
import com.ecommerce.catalog.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products") // Đây chính là đường dẫn để Frontend gọi tới
public class ProductController {

    @Autowired
    private ProductRepository productRepository;

    // Method này sẽ được kích hoạt khi có request GET tới /api/products
    @GetMapping
    public List<Product> getAllProducts() {
        // Trả về toàn bộ sản phẩm trong Database dưới dạng JSON
        return productRepository.findAll();
    }

    // API Thêm mới
    @PostMapping
    public Product createProduct(@RequestBody Product product) {
        return productRepository.save(product);
    }

    // API Lấy thông tin 1 Sản phẩm (chú ý ID của Product là Integer)
    @GetMapping("/{id}")
    public Product getProduct(@PathVariable("id") Integer id) {
        return productRepository.findById(id).orElse(null);
    }

    // API Cập nhật
    @PutMapping("/{id}")
    public Product updateProduct(@PathVariable("id") Integer id, @RequestBody Product product) {
        product.setProductId(id);
        return productRepository.save(product);
    }

    // API Xóa Sản phẩm
    @DeleteMapping("/{id}")
    public void deleteProduct(@PathVariable("id") Integer id) {
        productRepository.deleteById(id);
    }

}
