package com.ecommerce.frontend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

public class OrderDTO {
    @JsonProperty("id")
    private String id;

    @JsonProperty("memberId")
    private String memberId;

    @JsonProperty("orderDate")
    private String orderDate;

    @JsonProperty("totalAmount")
    private Double totalAmount;

    @JsonProperty("status")
    private String status;

    @JsonProperty("details")
    private List<OrderDetailDTO> details = new ArrayList<>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMemberId() {
        return memberId;
    }

    public void setMemberId(String memberId) {
        this.memberId = memberId;
    }

    public String getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(String orderDate) {
        this.orderDate = orderDate;
    }

    public Double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(Double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<OrderDetailDTO> getDetails() {
        return details;
    }

    public void setDetails(List<OrderDetailDTO> details) {
        this.details = details;
    }
}