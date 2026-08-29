package com.ecommerce.frontend.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Set;

public class AdminAuthInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        Object permissionsObj = request.getSession().getAttribute("permissions");
        // Chưa đăng nhập, hoặc Role không có quyền Admin nào (vd. ROLE_USER) -> về trang login
        if (!(permissionsObj instanceof Set<?> permissions) || permissions.isEmpty()) {
            response.sendRedirect("/dashboard/login");
            return false;
        }

        String requiredPermission = resolveRequiredPermission(request.getRequestURI());
        // Có đăng nhập nhưng thiếu đúng permission cho route này -> chặn, không phải logout
        if (requiredPermission != null && !permissions.contains(requiredPermission)) {
            response.sendRedirect("/dashboard/access-denied");
            return false;
        }
        return true;
    }

    // Ánh xạ tiền tố route -> permission cần có. Route nào không nằm trong danh sách
    // (vd. "/dashboard" trang chủ Dashboard) chỉ cần đã đăng nhập với ít nhất 1 quyền, không cần quyền cụ thể.
    private String resolveRequiredPermission(String path) {
        if (path.startsWith("/dashboard/products")) return "PRODUCT_MANAGE";
        if (path.startsWith("/dashboard/categories")) return "CATEGORY_MANAGE";
        if (path.startsWith("/dashboard/orders")) return "ORDER_MANAGE";
        if (path.startsWith("/dashboard/members")) return "MEMBER_MANAGE";
        if (path.startsWith("/dashboard/roles")) return "ROLE_MANAGE";
        if (path.startsWith("/dashboard/users")) return "ROLE_MANAGE";
        if (path.startsWith("/dashboard/inventory")) return "PRODUCT_MANAGE";
        return null;
    }
}