package com.ecommerce.frontend.dto;

public class MemberDTO {
    private String memberId;
    private String username;
    private String password;
    private String email;
    private Boolean gender;

    public String getMemberId() { return memberId; }
    public void setMemberId(String memberId) { this.memberId = memberId; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public Boolean getGender() { return gender; }
    public void setGender(Boolean gender) { this.gender = gender; }
}