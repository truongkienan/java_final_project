package com.ecommerce.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserSummaryDTO {
    private Long id;
    private String username;
    private Short roleId;
    private String roleName;
    private Boolean active;
}