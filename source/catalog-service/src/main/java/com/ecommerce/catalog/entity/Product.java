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
    // danh muc khong ton tai. optional=false + nullable=false: moi san pham BAT
    // BUOC phai co category, khong con the luu san pham voi category null nua.
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(nullable = false, columnDefinition = "NVARCHAR(255)")
    private String productName;
    private String imageUrl;
    private BigDecimal unitPrice;
    private BigDecimal saleOfPrice;
    private Short weight;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "unit_id", nullable = false)
    private Unit unit;

    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String description;

    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String content;
}