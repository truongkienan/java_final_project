package com.ecommerce.frontend.dto;

import java.util.List;

public class AuthResponse {
    private String token;
    private String role;
    private List<String> permissions;

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public List<String> getPermissions() { return permissions; }
    public void setPermissions(List<String> permissions) { this.permissions = permissions; }
}