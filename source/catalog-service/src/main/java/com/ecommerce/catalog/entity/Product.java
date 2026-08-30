package com.ecommerce.catalog.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;

@Entity
@Table(name = "products")
@Data
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer productId;

    // Quan he JPA that su toi Category - Hibernate tu quan ly rang buoc khoa ngoai
    // (category_id) khi tao/tai tao schema, dam bao khong the luu san pham voi
    // danh muc khong ton tai.
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "category_id")
    private Category category;

    @Column(nullable = false, columnDefinition = "NVARCHAR(255)")
    private String productName;
    private String imageUrl;
    private BigDecimal unitPrice;
    private BigDecimal saleOfPrice;
    private Short weight;

    @Column(columnDefinition = "NVARCHAR(50)")
    private String unit;

    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String description;

    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String content;
}