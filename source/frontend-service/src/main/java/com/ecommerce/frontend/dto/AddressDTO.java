package com.ecommerce.frontend.dto;

public class AddressDTO {
    private Integer addressId;
    private String addressName;
    private String memberId;
    private String phone;
    private Boolean isDefault;

    public Integer getAddressId() { return addressId; }
    public void setAddressId(Integer addressId) { this.addressId = addressId; }
    public String getAddressName() { return addressName; }
    public void setAddressName(String addressName) { this.addressName = addressName; }
    public String getMemberId() { return memberId; }
    public void setMemberId(String memberId) { this.memberId = memberId; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public Boolean getIsDefault() { return isDefault; }
    public void setIsDefault(Boolean isDefault) { this.isDefault = isDefault; }
}