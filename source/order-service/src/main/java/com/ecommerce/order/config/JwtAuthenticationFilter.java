package com.ecommerce.order.config;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.ecommerce.order.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // 1. Nhìn xem khách có mang theo vé không?
        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7); // Cắt bỏ chữ "Bearer " để lấy đúng chuỗi JWT
            try {
                // 2. Lấy máy soi vé (JwtUtil) ra kiểm tra
                DecodedJWT decodedJWT = jwtUtil.verifyToken(token);

                // 3. Vé thật! Đọc tên và chức vụ của khách
                String username = decodedJWT.getSubject();
                String role = decodedJWT.getClaim("role").asString();

                // 4. Phát cho khách cái thẻ đeo hợp lệ (SecurityContext)
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                username,
                                null,
                                Collections.singletonList(new SimpleGrantedAuthority(role))
                        );
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (Exception e) {
                // Vé giả hoặc hết hạn! Mặc kệ nó, tí nữa sẽ có đứa đuổi ra ngoài
            }
        }

        // Cho khách đi tiếp qua cửa
        filterChain.doFilter(request, response);
    }
}
