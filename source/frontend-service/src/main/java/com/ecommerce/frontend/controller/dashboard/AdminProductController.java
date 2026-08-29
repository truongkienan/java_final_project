package com.ecommerce.frontend.controller.dashboard;

import com.ecommerce.frontend.dto.ProductDTO;
import com.ecommerce.frontend.service.CatalogApiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/dashboard/products")
public class AdminProductController {

    @Autowired
    private CatalogApiService catalogApiService;

    @GetMapping
    public String manageProducts(Model model) {
        model.addAttribute("products", catalogApiService.getAllProducts());
        return "dashboard/products";
    }

    @GetMapping("/new")
    public String createProductForm(Model model) {
        model.addAttribute("product", new ProductDTO());
        return "dashboard/product-form";
    }

    @GetMapping("/edit/{id}")
    public String editProductForm(@PathVariable("id") Integer id, Model model) {
        model.addAttribute("product", catalogApiService.getProductById(id));
        return "dashboard/product-form";
    }

    @PostMapping("/save")
    public String saveProduct(@ModelAttribute ProductDTO productDTO) {
        catalogApiService.saveProduct(productDTO);
        return "redirect:/dashboard/products";
    }

    @PostMapping("/delete/{id}")
    public String deleteProduct(@PathVariable("id") Integer id) {
        catalogApiService.deleteProduct(id);
        return "redirect:/dashboard/products";
    }
}