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

    // Chặn toàn bộ /dashboard/** nếu chưa đăng nhập hoặc không đủ permission,
    // trừ trang đăng nhập Admin, trang thông báo từ chối truy cập,
    // và các thư mục tài nguyên tĩnh /admin/css, /admin/js, /admin/assets (thư mục static
    // này không đổi tên theo route /dashboard/**) - phải luôn truy cập được kể cả khi chưa
    // đăng nhập (nếu không, ngay cả CSS/JS của chính trang login cũng bị chặn theo).
    // (/dashboard/register đã bị xóa - không còn cần loại trừ nữa)
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new AdminAuthInterceptor())
                .addPathPatterns("/dashboard/**")
                .excludePathPatterns(
                        "/dashboard/login", "/dashboard/access-denied",
                        "/admin/css/**", "/admin/js/**", "/admin/assets/**");
    }
}