package com.ecommerce.frontend.dto;

import java.util.List;

public class RoleDTO {
    private Short roleId;
    private String roleName;
    private String description;
    private List<PermissionDTO> permissions; // Danh sách permission đầy đủ - dùng để hiển thị (list, badge)
    private List<Short> permissionIds; // ID được tick trên form - dùng để bind checkbox khi tạo/sửa

    public Short getRoleId() { return roleId; }
    public void setRoleId(Short roleId) { this.roleId = roleId; }
    public String getRoleName() { return roleName; }
    public void setRoleName(String roleName) { this.roleName = roleName; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public List<PermissionDTO> getPermissions() { return permissions; }
    public void setPermissions(List<PermissionDTO> permissions) { this.permissions = permissions; }
    public List<Short> getPermissionIds() { return permissionIds; }
    public void setPermissionIds(List<Short> permissionIds) { this.permissionIds = permissionIds; }
}