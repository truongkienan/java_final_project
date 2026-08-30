package com.ecommerce.frontend.dto;

public class UnitDTO {
    private Short unitId;
    private String unitName;
    private CategoryDTO category;

    public Short getUnitId() { return unitId; }
    public void setUnitId(Short unitId) { this.unitId = unitId; }
    public String getUnitName() { return unitName; }
    public void setUnitName(String unitName) { this.unitName = unitName; }
    public CategoryDTO getCategory() { return category; }
    public void setCategory(CategoryDTO category) { this.category = category; }
}
