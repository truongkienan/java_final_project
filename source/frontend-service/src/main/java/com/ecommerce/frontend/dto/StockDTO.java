package com.ecommerce.frontend.dto;

public class StockDTO {
    private String productId;
    private Integer quantity;
    private String productName; // Chỉ dùng ở frontend để hiển thị - inventory-service không có field này

    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
}