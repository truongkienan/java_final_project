package com.ecommerce.frontend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class OrderDetailDTO {
    @JsonProperty("productId")
    private String productId;

    @JsonProperty("productName")
    private String productName;

    @JsonProperty("price")
    private Double price;

    @JsonProperty("quantity")
    private Integer quantity;

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
}