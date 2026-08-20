package com.ecommerce.frontend.dto;

import java.util.List;

public class CategoryDTO {
    private Short categoryId;
    private String categoryName;
    private String imageUrl;
    private String slug;
    private String description;
    private Integer position;
    private Boolean active;
    private Short parentId;
    private String parentName; // Không map từ backend, chỉ dùng để hiển thị tên Danh mục cha ở trang quản lý Admin
    private List<CategoryDTO> children; // Map từ /api/categories/tree (chỉ có ở danh mục cấp 1)

    public Short getCategoryId() { return categoryId; }
    public void setCategoryId(Short categoryId) { this.categoryId = categoryId; }
    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public String getSlug() { return slug; }
    public void setSlug(String slug) { this.slug = slug; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Integer getPosition() { return position; }
    public void setPosition(Integer position) { this.position = position; }
    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
    public Short getParentId() { return parentId; }
    public void setParentId(Short parentId) { this.parentId = parentId; }
    public String getParentName() { return parentName; }
    public void setParentName(String parentName) { this.parentName = parentName; }
    public List<CategoryDTO> getChildren() { return children; }
    public void setChildren(List<CategoryDTO> children) { this.children = children; }
}