package com.ecommerce.order.dto;

import java.util.List;

public class StockEventDTO {
    private String orderId;
    private List<StockItemDTO> items;

    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }
    public List<StockItemDTO> getItems() { return items; }
    public void setItems(List<StockItemDTO> items) { this.items = items; }
}