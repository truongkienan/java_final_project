package com.ecommerce.auth.controller;

import com.ecommerce.auth.dto.AuthResponse;
import com.ecommerce.auth.dto.ChangePasswordRequest;
import com.ecommerce.auth.dto.LoginRequest;
import com.ecommerce.auth.dto.MemberAuthDTO;
import com.ecommerce.auth.dto.RegisterRequest;
import com.ecommerce.auth.entity.Role;
import com.ecommerce.auth.entity.User;
import com.ecommerce.auth.repository.RoleRepository;
import com.ecommerce.auth.repository.UserRepository;
import com.ecommerce.auth.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${CUSTOMER_SERVICE_URL:http://localhost:8082/api/members}")
    private String customerServiceUrl;

    // API Đăng nhập cho tài khoản Staff/Dashboard (bảng users)
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        Optional<User> userOpt = userRepository.findByUsername(request.getUsername());

        // Kiểm tra xem User có tồn tại và password có khớp không
        if (userOpt.isPresent() && userOpt.get().getPassword().equals(request.getPassword())) {
            User user = userOpt.get();

            if (!Boolean.TRUE.equals(user.getActive())) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Tài khoản đã bị thu hồi!");
            }

            String roleName = user.getRole().getRoleName();
            // Sinh Token
            String token = jwtUtil.generateToken(user.getUsername(), roleName);
            return ResponseEntity.ok(new AuthResponse(token, roleName,
                    user.getRole().getPermissions() == null ? List.of()
                            : user.getRole().getPermissions().stream()
                                    .map(p -> p.getPermissionName()).toList()));
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Sai tên đăng nhập hoặc mật khẩu!");
    }

    // API Đăng nhập cho tài khoản Khách hàng (bảng Members bên customer-service) -
    // auth-service vẫn là nơi duy nhất ký JWT, nhưng xác thực bằng cách gọi sang
    // customer-service thay vì tra bảng users. Khách hàng không có Role/Permission,
    // JWT gán cứng role=ROLE_USER.
    @PostMapping("/customer-login")
    public ResponseEntity<?> customerLogin(@RequestBody LoginRequest request) {
        try {
            MemberAuthDTO member = restTemplate.getForObject(
                    customerServiceUrl + "/username/" + request.getUsername(), MemberAuthDTO.class);
            if (member != null && member.getPassword() != null
                    && member.getPassword().equals(request.getPassword())) {
                String token = jwtUtil.generateToken(request.getUsername(), "ROLE_USER");
                return ResponseEntity.ok(new AuthResponse(token, "ROLE_USER", List.of()));
            }
        } catch (RestClientException e) {
            // Member không tồn tại (404 từ customer-service) hoặc service lỗi -> coi như sai thông tin đăng nhập
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Sai tên đăng nhập hoặc mật khẩu!");
    }

    // API Đăng ký - từ Bước này về sau chỉ còn ý nghĩa "tạo tài khoản Staff"
    // (khách hàng đăng ký qua customer-service, không qua endpoint này nữa)
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            return ResponseEntity.badRequest().body("Username đã tồn tại!");
        }

        // Gán Role mặc định ROLE_USER nếu chưa truyền
        String roleName = (request.getRole() == null || request.getRole().isEmpty())
                ? "ROLE_USER" : request.getRole();
        Role role = roleRepository.findByRoleName(roleName)
                .orElseGet(() -> roleRepository.findByRoleName("ROLE_USER")
                        .orElseThrow(() -> new RuntimeException("Role mặc định ROLE_USER chưa tồn tại trong DB")));

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(request.getPassword());
        user.setRole(role);
        user.setActive(true);
        userRepository.save(user);
        return ResponseEntity.ok("Đăng ký thành công!");
    }

    // API Đổi mật khẩu (tài khoản Staff/Dashboard)
    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody ChangePasswordRequest request) {
        Optional<User> userOpt = userRepository.findByUsername(request.getUsername());
        if (userOpt.isEmpty() || !userOpt.get().getPassword().equals(request.getOldPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Mật khẩu cũ không đúng!");
        }
        User user = userOpt.get();
        user.setPassword(request.getNewPassword());
        userRepository.save(user);
        return ResponseEntity.ok("Đổi mật khẩu thành công!");
    }
}