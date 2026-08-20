package com.ecommerce.frontend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class AppConfig implements WebMvcConfigurer {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    // Chặn toàn bộ /admin/** nếu chưa đăng nhập hoặc không đủ permission,
    // trừ trang đăng nhập Admin và trang thông báo từ chối truy cập.
    // (/admin/register đã bị xóa - không còn cần loại trừ nữa)
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new AdminAuthInterceptor())
                .addPathPatterns("/admin/**")
                .excludePathPatterns("/admin/login", "/admin/access-denied");
    }
}