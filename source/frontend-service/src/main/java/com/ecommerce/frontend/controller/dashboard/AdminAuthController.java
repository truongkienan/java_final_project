package com.ecommerce.frontend.controller.dashboard;

import com.ecommerce.frontend.dto.AuthResponse;
import com.ecommerce.frontend.dto.LoginRequest;
import com.ecommerce.frontend.service.AuthApiService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashSet;

@Controller
@RequestMapping("/dashboard")
public class AdminAuthController {

    @Autowired
    private AuthApiService authApiService;

    @GetMapping("/login")
    public String showLoginForm(Model model) {
        model.addAttribute("loginRequest", new LoginRequest());
        return "dashboard/login";
    }

    @PostMapping("/login")
    public String processLogin(@ModelAttribute LoginRequest loginRequest, HttpSession session, Model model) {
        AuthResponse auth = authApiService.login(loginRequest);
        if (auth != null && auth.getPermissions() != null && !auth.getPermissions().isEmpty()) {
            // Dùng namespace "admin..." riêng - tách biệt hoàn toàn khỏi session của tài khoản khách hàng
            // (client AuthController dùng key "username"/"role"/"jwtToken" trần, không namespace),
            // vì 2 controller này cùng chạy trong 1 app nên chung 1 HttpSession vật lý.
            session.setAttribute("adminUsername", loginRequest.getUsername());
            session.setAttribute("adminRole", auth.getRole());
            session.setAttribute("adminJwtToken", auth.getToken());
            session.setAttribute("permissions", new HashSet<>(auth.getPermissions()));
            return "redirect:/dashboard";
        }
        model.addAttribute("error", "Invalid username/password, or this account does not have access to the Admin Dashboard!");
        return "dashboard/login";
    }

    // Đăng ký công khai đã bị khóa - tài khoản Staff giờ chỉ tạo được qua /dashboard/users/new
    // (yêu cầu đăng nhập + quyền ROLE_MANAGE), tránh việc ai cũng tự phong quyền Admin được.

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        // Chỉ xóa đúng các key của phiên Admin - không dùng session.invalidate() để tránh
        // xóa luôn phiên đăng nhập khách hàng nếu 2 loại tài khoản cùng mở trên 1 trình duyệt.
        session.removeAttribute("adminUsername");
        session.removeAttribute("adminRole");
        session.removeAttribute("adminJwtToken");
        session.removeAttribute("permissions");
        return "redirect:/dashboard/login";
    }

    // Form đổi mật khẩu cho chính tài khoản Admin/Staff đang đăng nhập - tái sử dụng
    // AuthApiService.changePassword() (gọi auth-service, xác thực bằng username trong session "adminUsername").
    @GetMapping("/change-password")
    public String showChangePasswordForm() {
        return "dashboard/change-password";
    }

    @PostMapping("/change-password")
    public String changePassword(@RequestParam("oldPassword") String oldPassword,
                                 @RequestParam("newPassword") String newPassword,
                                 HttpSession session, RedirectAttributes redirectAttributes) {
        String username = (String) session.getAttribute("adminUsername");
        boolean success = authApiService.changePassword(username, oldPassword, newPassword);
        redirectAttributes.addFlashAttribute(success ? "passwordMessage" : "passwordError",
                success ? "Password changed successfully!" : "Incorrect current password!");
        return "redirect:/dashboard/change-password";
    }
}