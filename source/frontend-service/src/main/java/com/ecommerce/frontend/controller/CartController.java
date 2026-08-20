package com.ecommerce.frontend.controller;

import com.ecommerce.frontend.dto.CartDTO;
import com.ecommerce.frontend.service.BasketApiService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    @Autowired
    private BasketApiService basketApiService;

    @PostMapping("/checkout")
    public ResponseEntity<String> checkout(@RequestBody CartDTO cartDTO, HttpSession session) {
        // Nếu đã đăng nhập, luôn dùng username thật làm khóa giỏ hàng/đơn hàng
        // (bỏ qua memberId do JS gửi lên, tránh lẫn giỏ hàng giữa các user)
        String loggedInUsername = (String) session.getAttribute("username");
        String memberId = (loggedInUsername != null) ? loggedInUsername : cartDTO.getMemberId();
        cartDTO.setMemberId(memberId);

        // Lưu memberId vào session để CheckoutController biết lấy giỏ hàng của ai
        session.setAttribute("memberId", memberId);

        // Đồng bộ giỏ hàng lên Basket Service (Redis)
        basketApiService.updateBasket(cartDTO);

        // Trả về URL để JS redirect
        return ResponseEntity.ok("/checkout");
    }
}