package com.ecommerce.basket.entity;

import lombok.Data;

@Data
public class CartItem {
    private String productId;
    private String productName;
    private Double price;
    private Integer quantity;
}
