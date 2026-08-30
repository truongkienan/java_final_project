package com.ecommerce.frontend.controller.client;

import com.ecommerce.frontend.dto.CartDTO;
import com.ecommerce.frontend.service.BasketApiService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    @Autowired
    private BasketApiService basketApiService;

    // Bat buoc dang nhap moi duoc dong bo gio hang len server va vao checkout -
    // khach vang lai van duyet web/them gio hang binh thuong (localStorage phia
    // client), chi bi chan dung o buoc bam "Checkout" nay.
    @PostMapping("/checkout")
    public ResponseEntity<?> checkout(@RequestBody CartDTO cartDTO, HttpSession session) {
        String username = (String) session.getAttribute("username");
        if (username == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "login_required"));
        }

        // Luon dung username that lam khoa gio hang/don hang, khong tin memberId
        // ma client gui len, tranh lan gio hang giua cac tai khoan.
        cartDTO.setMemberId(username);

        // Đồng bộ giỏ hàng lên Basket Service (Redis)
        basketApiService.updateBasket(cartDTO);

        // Trả về URL để JS redirect
        return ResponseEntity.ok("/checkout");
    }
}