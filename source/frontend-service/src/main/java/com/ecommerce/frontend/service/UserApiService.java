package com.ecommerce.frontend.service;

import com.ecommerce.frontend.dto.UserSummaryDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Service
public class UserApiService {

    @Autowired
    private RestTemplate restTemplate;

    @Value("${USER_SERVICE_URL:http://localhost:8085/api/users}")
    private String userServiceUrl;

    public List<UserSummaryDTO> getAllUsers() {
        ResponseEntity<UserSummaryDTO[]> response = restTemplate.getForEntity(userServiceUrl, UserSummaryDTO[].class);
        UserSummaryDTO[] users = response.getBody();
        return users != null ? Arrays.asList(users) : Arrays.asList();
    }

    public UserSummaryDTO getUserById(Long id) {
        return getAllUsers().stream()
                .filter(u -> u.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    public void updateUserRole(Long id, Short roleId) {
        restTemplate.put(userServiceUrl + "/" + id + "/role", Map.of("roleId", roleId));
    }

    // Bật/tắt tài khoản Staff - dùng cho tính năng thu hồi/cấp lại
    public void updateActive(Long id, boolean active) {
        restTemplate.put(userServiceUrl + "/" + id + "/active", Map.of("active", active));
    }
}