package com.ecommerce.frontend.controller;

import com.ecommerce.frontend.dto.CategoryDTO;
import com.ecommerce.frontend.dto.ProductDTO;
import com.ecommerce.frontend.service.CatalogApiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/")
public class ClientController {

    // 1. Nhúng Service gọi API vào Controller
    @Autowired
    private CatalogApiService catalogApiService;

    // 2. Thêm đối tượng Model của Spring vào tham số hàm
    @GetMapping
    public String home(Model model) {

        // 3. Gọi Service để lấy danh sách sản phẩm thực tế từ Backend
        List<ProductDTO> products = catalogApiService.getAllProducts();

        // 4. Đóng gói danh sách sản phẩm vào một cái biến tên là "products"
        // Thymeleaf sẽ bắt lấy biến này để vẽ ra HTML
        model.addAttribute("products", products);

        // 5. Nhóm sản phẩm theo tên Danh mục (chỉ những danh mục thực sự có sản phẩm)
        // để vẽ khối tab "Special Offers" theo category ở đầu trang chủ.
        Map<Short, String> categoryNameById = catalogApiService.getAllCategories().stream()
                .collect(Collectors.toMap(CategoryDTO::getCategoryId, CategoryDTO::getCategoryName));

        Map<String, List<ProductDTO>> productsByCategory = new LinkedHashMap<>();
        for (ProductDTO product : products) {
            String categoryName = categoryNameById.getOrDefault(product.getCategoryId(), "Khác");
            productsByCategory.computeIfAbsent(categoryName, k -> new ArrayList<>()).add(product);
        }
        model.addAttribute("productsByCategory", productsByCategory);

        return "client/index";
    }

    // Trang chi tiết 1 sản phẩm
    @GetMapping("/product/{id}")
    public String productDetail(@PathVariable("id") Integer id, Model model) {
        ProductDTO product = catalogApiService.getProductById(id);
        if (product == null) {
            return "redirect:/";
        }
        model.addAttribute("product", product);

        // Danh sách sản phẩm khác cùng category để gợi ý (loại trừ chính sản phẩm đang xem)
        List<ProductDTO> related = catalogApiService.getAllProducts().stream()
                .filter(p -> !id.equals(p.getProductId()))
                .filter(p -> product.getCategoryId() != null && product.getCategoryId().equals(p.getCategoryId()))
                .toList();
        model.addAttribute("relatedProducts", related);

        return "client/single";
    }
}