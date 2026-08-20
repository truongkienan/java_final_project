package com.ecommerce.auth.dto;

import lombok.Data;

// DTO tối giản để gọi sang customer-service xác thực khách hàng - chỉ cần
// username/password để so sánh, không cần các field khác (email/gender...).
@Data
public class MemberAuthDTO {
    private String memberId;
    private String username;
    private String password;
}