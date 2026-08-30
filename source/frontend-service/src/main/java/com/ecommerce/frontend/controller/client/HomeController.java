package com.ecommerce.frontend.controller.client;

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

@Controller
@RequestMapping("/")
public class HomeController {

    @Autowired
    private CatalogApiService catalogApiService;

    @GetMapping
    public String index(Model model) {

        List<ProductDTO> products = catalogApiService.getProducts();
        model.addAttribute("products", products);

        Map<String, List<ProductDTO>> productsByCategory = new LinkedHashMap<>();
        for (ProductDTO product : products) {
            String categoryName = product.getCategory() != null ? product.getCategory().getCategoryName() : "Khác";
            productsByCategory.computeIfAbsent(categoryName, k -> new ArrayList<>()).add(product);
        }
        model.addAttribute("productsByCategory", productsByCategory);

        return "client/index";
    }

    @GetMapping("/product/{id}")
    public String productDetail(@PathVariable("id") Integer id, Model model) {
        ProductDTO product = catalogApiService.getProductById(id);
        if (product == null) {
            return "redirect:/";
        }
        model.addAttribute("product", product);

        List<ProductDTO> related = catalogApiService.getProducts().stream()
                .filter(p -> !id.equals(p.getProductId()))
                .filter(p -> product.getCategory() != null && p.getCategory() != null
                        && product.getCategory().getCategoryId().equals(p.getCategory().getCategoryId()))
                .toList();
        model.addAttribute("relatedProducts", related);

        return "client/single";
    }
}