package com.ecommerce.catalog.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Entity
@Table(name = "Categories")
@Data
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Short categoryId;

    @Column(nullable = false, columnDefinition = "NVARCHAR(255)")
    private String categoryName;

    private String imageUrl;

    @Column(nullable = false, length = 128)
    private String slug;

    @Column(columnDefinition = "NVARCHAR(400)")
    private String description;

    @Column(nullable = false)
    private Integer position = 0;

    @Column(nullable = false)
    private Boolean active = true;

    // Danh mục cha — null nghĩa là danh mục cấp 1
    private Short parentId;

    // Cột có DEFAULT GETDATE() ở DB, không cho JPA ghi đè lúc insert/update
    @Column(insertable = false, updatable = false)
    private Date createdTime;

    // Không lưu DB — chỉ dùng để trả về cây 2 cấp qua API /tree
    @Transient
    private List<Category> children;
}