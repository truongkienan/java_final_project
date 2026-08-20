package com.ecommerce.inventory.dto;

public class StockItemDTO {
    private String productId;
    private Integer quantity;

    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
}