package com.ecommerce.frontend.dto;

import java.math.BigDecimal;

public class ProductDTO {
    private Integer productId;
    private CategoryDTO category;
    private String productName;
    private BigDecimal unitPrice;
    private BigDecimal saleOfPrice;
    private String imageUrl;
    private String unit;
    private String description;

    public Integer getProductId() {
        return productId;
    }

    public void setProductId(Integer productId) {
        this.productId = productId;
    }

    public CategoryDTO getCategory() {
        return category;
    }

    public void setCategory(CategoryDTO category) {
        this.category = category;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public BigDecimal getSaleOfPrice() {
        return saleOfPrice;
    }

    public void setSaleOfPrice(BigDecimal saleOfPrice) {
        this.saleOfPrice = saleOfPrice;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}