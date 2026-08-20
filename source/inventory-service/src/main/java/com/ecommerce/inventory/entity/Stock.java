package com.ecommerce.inventory.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.Date;

@Entity
@Table(name = "Stocks")
@Data
public class Stock {
    @Id
    private String productId;

    private Integer quantity;

    private Date updatedAt = new Date();
}