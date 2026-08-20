package com.ecommerce.basket.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import java.util.ArrayList;
import java.util.List;

@Data
@RedisHash("Cart") // Annotation này nói cho Spring biết hãy lưu object này vào Redis với tiền tố là "Cart"
public class Cart {

    @Id
    private String memberId; // Dùng ID của user làm khóa chính (mỗi user 1 giỏ hàng)

    private List<CartItem> items = new ArrayList<>();
    // Hàm phụ trợ tự động tính tổng tiền
    public Double getTotalPrice() {
        return items.stream()
                .mapToDouble(item -> item.getPrice() * item.getQuantity())
                .sum();
    }
}