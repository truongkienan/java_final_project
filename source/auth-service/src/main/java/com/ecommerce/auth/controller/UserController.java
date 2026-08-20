package com.ecommerce.auth.controller;

import com.ecommerce.auth.dto.UserSummaryDTO;
import com.ecommerce.auth.entity.Role;
import com.ecommerce.auth.entity.User;
import com.ecommerce.auth.repository.RoleRepository;
import com.ecommerce.auth.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @GetMapping
    public List<UserSummaryDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(u -> new UserSummaryDTO(u.getId(), u.getUsername(), u.getRole().getRoleId(), u.getRole().getRoleName(), u.getActive()))
                .collect(Collectors.toList());
    }

    @PutMapping("/{id}/role")
    public UserSummaryDTO updateUserRole(@PathVariable("id") Long id, @RequestBody Map<String, Short> body) {
        User user = userRepository.findById(id).orElseThrow();
        Role role = roleRepository.findById(body.get("roleId")).orElseThrow();
        user.setRole(role);
        userRepository.save(user);
        return new UserSummaryDTO(user.getId(), user.getUsername(), role.getRoleId(), role.getRoleName(), user.getActive());
    }

    // Bật/tắt tài khoản Staff - dùng cho tính năng "thu hồi/cấp lại" trên trang Admin Users
    @PutMapping("/{id}/active")
    public UserSummaryDTO updateActive(@PathVariable("id") Long id, @RequestBody Map<String, Boolean> body) {
        User user = userRepository.findById(id).orElseThrow();
        user.setActive(body.get("active"));
        userRepository.save(user);
        return new UserSummaryDTO(user.getId(), user.getUsername(), user.getRole().getRoleId(), user.getRole().getRoleName(), user.getActive());
    }
}