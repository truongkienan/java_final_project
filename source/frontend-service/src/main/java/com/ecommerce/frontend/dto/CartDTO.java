package com.ecommerce.frontend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;

public class CartDTO {
    @JsonProperty("memberId")
    private String memberId;

    @JsonProperty("items")
    private List<CartItemDTO> items = new ArrayList<>();

    public String getMemberId() {
        return memberId;
    }

    public void setMemberId(String memberId) {
        this.memberId = memberId;
    }

    public List<CartItemDTO> getItems() {
        return items;
    }

    public void setItems(List<CartItemDTO> items) {
        this.items = items;
    }
}
