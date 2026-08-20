package com.ecommerce.frontend.service;

import com.ecommerce.frontend.dto.AuthResponse;
import com.ecommerce.frontend.dto.LoginRequest;
import com.ecommerce.frontend.dto.UserDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.RestClientException;

import java.util.Map;

@Service
public class AuthApiService {

    @Autowired
    private RestTemplate restTemplate;

    // @Value("${backend.api.gateway.url:http://localhost:8080}")
    @Value("${backend.api.gateway.url:http://localhost:8085}")

    private String gatewayUrl;

    public AuthResponse login(LoginRequest request) {
        String url = gatewayUrl + "/api/auth/login";
        try {
            ResponseEntity<AuthResponse> response = restTemplate.postForEntity(url, request, AuthResponse.class);
            if (response.getStatusCode().is2xxSuccessful()) {
                return response.getBody();
            }
        } catch (RestClientException  e) {
            System.out.println("Login Failed: " + e.getMessage());
        }
        return null;
    }

    public AuthResponse customerLogin(LoginRequest request) {
        String url = gatewayUrl + "/api/auth/customer-login";
        try {
            ResponseEntity<AuthResponse> response = restTemplate.postForEntity(url, request, AuthResponse.class);
            if (response.getStatusCode().is2xxSuccessful()) {
                return response.getBody();
            }
        } catch (RestClientException e) {
            System.out.println("Customer Login Failed: " + e.getMessage());
        }
        return null;
    }

    public boolean register(UserDto userDto) {
        String url = gatewayUrl + "/api/auth/register";
        try {
            ResponseEntity<String> response = restTemplate.postForEntity(url, userDto, String.class);
            return response.getStatusCode().is2xxSuccessful();
        } catch (RestClientException  e) {
            System.out.println("Register Failed: " + e.getMessage());
            return false;
        }
    }

    public boolean changePassword(String username, String oldPassword, String newPassword) {
        String url = gatewayUrl + "/api/auth/change-password";
        try {
            Map<String, String> body = Map.of(
                    "username", username,
                    "oldPassword", oldPassword,
                    "newPassword", newPassword);
            ResponseEntity<String> response = restTemplate.postForEntity(url, body, String.class);
            return response.getStatusCode().is2xxSuccessful();
        } catch (RestClientException e) {
            System.out.println("Change password failed: " + e.getMessage());
            return false;
        }
    }
}