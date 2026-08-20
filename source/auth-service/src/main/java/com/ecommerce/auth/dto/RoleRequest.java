package com.ecommerce.auth.dto;

import java.util.List;

// Dùng khi tạo/sửa Role: nhận danh sách permissionId từ checkbox trên form,
// thay vì nhận nguyên object Permission (frontend chỉ cần gửi ID được tick).
public class RoleRequest {
    private String roleName;
    private String description;
    private List<Short> permissionIds;

    public String getRoleName() { return roleName; }
    public void setRoleName(String roleName) { this.roleName = roleName; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public List<Short> getPermissionIds() { return permissionIds; }
    public void setPermissionIds(List<Short> permissionIds) { this.permissionIds = permissionIds; }
}