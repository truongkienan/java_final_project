package com.ecommerce.auth.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

// auth-service trước đây chưa từng gọi sang service khác - cần bean này
// để gọi customer-service xác thực đăng nhập khách hàng (customer-login).
@Configuration
public class RestTemplateConfig {
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}