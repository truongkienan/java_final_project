package com.ecommerce.frontend.controller.client;

import com.ecommerce.frontend.dto.CartDTO;
import com.ecommerce.frontend.dto.CategoryDTO;
import com.ecommerce.frontend.service.BasketApiService;
import com.ecommerce.frontend.service.CatalogApiService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.List;

// Nạp sẵn cây danh mục (categoryTree) vào mọi trang Client để _header.html dùng chung,
// không cần khai báo lặp lại ở từng Controller. Chỉ áp dụng cho các Controller phía Client
// (không áp dụng cho Admin vì Admin có menu/layout riêng, không dùng _header.html này).
@ControllerAdvice(assignableTypes = {
        HomeController.class,
        AuthController.class,
        AccountController.class,
        CartController.class,
        CheckoutController.class
})
public class ClientGlobalAdvice {

    @Autowired
    private CatalogApiService catalogApiService;

    @Autowired
    private BasketApiService basketApiService;

    @ModelAttribute("categoryTree")
    public List<CategoryDTO> categoryTree() {
        return catalogApiService.getCategoryTree();
    }

    // Inject giỏ hàng từ Redis vào model để layout script có thể restore localStorage
    // sau khi user login. Chỉ gọi Redis khi user đang đăng nhập.
    @ModelAttribute("sessionCart")
    public CartDTO sessionCart(HttpSession session) {
        String username = (String) session.getAttribute("username");
        if (username == null) return null;
        return basketApiService.getBasket(username);
    }
}