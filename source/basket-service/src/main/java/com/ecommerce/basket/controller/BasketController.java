package com.ecommerce.basket.controller;

import com.ecommerce.basket.entity.Cart;
import com.ecommerce.basket.repository.CartRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/baskets")
public class BasketController {
    @Autowired
    private CartRepository cartRepository;

    // Lấy giỏ hàng của user
    @GetMapping("/{memberId}")
    public ResponseEntity<Cart> getBasket(@PathVariable("memberId") String memberId) {
        Cart cart = cartRepository.findById(memberId).orElseGet(() -> {
            Cart newCart = new Cart();
            newCart.setMemberId(memberId);
            return newCart; // Trả về giỏ hàng trống nếu user chưa có gì
        });
        return ResponseEntity.ok(cart);
    }

    // Cập nhật giỏ hàng (khi thêm hoặc sửa số lượng món)
    @PostMapping
    public ResponseEntity<Cart> updateBasket(@RequestBody Cart cart) {
        Cart updatedCart = cartRepository.save(cart); // Lưu thẳng vào Redis
        return ResponseEntity.ok(updatedCart);
    }
    // Xóa toàn bộ giỏ hàng (thường gọi khi checkout xong)
    @DeleteMapping("/{memberId}")
    public ResponseEntity<Void> deleteBasket(@PathVariable("memberId") String memberId) {
        cartRepository.deleteById(memberId);
        return ResponseEntity.noContent().build();
    }

}

