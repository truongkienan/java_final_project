package com.ecommerce.frontend.dto;

public class UserSummaryDTO {
    private Long id;
    private String username;
    private Short roleId;
    private String roleName;
    private Boolean active;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public Short getRoleId() { return roleId; }
    public void setRoleId(Short roleId) { this.roleId = roleId; }
    public String getRoleName() { return roleName; }
    public void setRoleName(String roleName) { this.roleName = roleName; }
    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
}