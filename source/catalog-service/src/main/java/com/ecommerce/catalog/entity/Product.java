package com.ecommerce.catalog.entity;
import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;

@Entity
@Table(name = "Products")
@Data
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer productId;
    // Thay vì dùng khóa ngoại ở cấp database ngay lúc này,
    // chúng ta giữ nguyên kiểu short để ánh xạ cho đơn giản
    private Short categoryId;
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
