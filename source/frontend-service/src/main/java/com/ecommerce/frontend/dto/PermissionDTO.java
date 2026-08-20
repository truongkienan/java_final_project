package com.ecommerce.frontend.dto;

public class PermissionDTO {
    private Short permissionId;
    private String permissionName;
    private String description;

    public Short getPermissionId() { return permissionId; }
    public void setPermissionId(Short permissionId) { this.permissionId = permissionId; }
    public String getPermissionName() { return permissionName; }
    public void setPermissionName(String permissionName) { this.permissionName = permissionName; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}