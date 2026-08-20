package com.ecommerce.frontend.controller;

import com.ecommerce.frontend.dto.AuthResponse;
import com.ecommerce.frontend.dto.LoginRequest;
import com.ecommerce.frontend.dto.UserDto;
import com.ecommerce.frontend.service.AuthApiService;
import com.ecommerce.frontend.service.CustomerApiService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class AuthController {

    @Autowired
    private AuthApiService authApiService;

    @Autowired
    private CustomerApiService customerApiService;

    // --- ĐĂNG NHẬP (Khách hàng) ---
    @GetMapping("/login")
    public String showLoginForm(Model model) {
        model.addAttribute("loginRequest", new LoginRequest());
        return "client/login";
    }

    @PostMapping("/login")
    public String processLogin(@ModelAttribute LoginRequest loginRequest, HttpSession session, Model model) {
        // Khách hàng xác thực qua Members (customer-service), không còn dùng chung
        // endpoint /api/auth/login với tài khoản Staff nữa.
        AuthResponse auth = authApiService.customerLogin(loginRequest);
        if (auth != null) {
            // Lưu token, username, role vào session
            session.setAttribute("jwtToken", auth.getToken());
            session.setAttribute("username", loginRequest.getUsername());
            session.setAttribute("role", auth.getRole());
            return "redirect:/"; // Về trang chủ sau khi đăng nhập thành công
        } else {
            model.addAttribute("error", "Sai tên đăng nhập hoặc mật khẩu!");
            return "client/login";
        }
    }

    // --- ĐĂNG KÝ (Khách hàng) ---
    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        model.addAttribute("userDto", new UserDto());
        return "client/register";
    }

    @PostMapping("/register")
    public String processRegister(@ModelAttribute UserDto userDto, Model model) {
        // Khách hàng chỉ tạo hồ sơ Member (customer-service) - không còn tạo kèm dòng
        // trong users (auth-service) nữa, vì users giờ chỉ dành cho tài khoản Staff/Admin.
        customerApiService.createMember(userDto.getUsername(), userDto.getPassword());
        return "redirect:/login?registered=true";
    }

    // --- ĐĂNG XUẤT ---
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        // Chỉ xóa đúng các key của phiên khách hàng - không dùng session.invalidate() để tránh
        // xóa luôn phiên đăng nhập nhân viên nội bộ nếu 2 loại tài khoản cùng mở trên 1 trình duyệt.
        session.removeAttribute("jwtToken");
        session.removeAttribute("username");
        session.removeAttribute("role");
        session.removeAttribute("memberId");
        return "redirect:/";
    }
}