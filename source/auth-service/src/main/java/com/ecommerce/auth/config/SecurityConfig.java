package com.ecommerce.auth.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // Tắt bảo vệ CSRF (vì chúng ta dùng JWT không lưu Session)
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        // Cho phép tất cả mọi người (kể cả chưa đăng nhập) được gọi các API trong auth
                        .requestMatchers("/api/auth/**").permitAll()
                        // Role/Permission/Users: việc kiểm tra quyền thực sự nằm ở AdminAuthInterceptor
                        // bên frontend-service (giống Category/Product), không lặp lại ở đây.
                        .requestMatchers("/api/roles/**", "/api/permissions/**", "/api/users/**").permitAll()
                        // Tất cả các request khác (nếu có) phải có xác thực
                        .anyRequest().authenticated()
                );

        return http.build();
    }
}