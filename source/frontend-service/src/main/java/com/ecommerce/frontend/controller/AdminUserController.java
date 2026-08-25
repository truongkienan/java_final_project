package com.ecommerce.frontend.controller;

import com.ecommerce.frontend.dto.UserDto;
import com.ecommerce.frontend.dto.UserSummaryDTO;
import com.ecommerce.frontend.service.AuthApiService;
import com.ecommerce.frontend.service.RoleApiService;
import com.ecommerce.frontend.service.UserApiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin/users")
public class AdminUserController {

    @Autowired
    private UserApiService userApiService;
    @Autowired
    private RoleApiService roleApiService;
    @Autowired
    private AuthApiService authApiService;

    // Hien thi danh sach tai khoan Admin/Staff kem Role hien tai
    @GetMapping
    public String manageUsers(Model model) {
        model.addAttribute("users", userApiService.getAllUsers());
        return "admin/users";
    }

    @GetMapping("/edit/{id}")
    public String editUserRoleForm(@PathVariable("id") Long id, Model model) {
        model.addAttribute("user", userApiService.getUserById(id));
        model.addAttribute("allRoles", roleApiService.getAllRoles());
        return "admin/user-role-form";
    }

    @PostMapping("/save")
    public String saveUserRole(@ModelAttribute UserSummaryDTO userSummaryDTO) {
        userApiService.updateUserRole(userSummaryDTO.getId(), userSummaryDTO.getRoleId());
        userApiService.updateActive(userSummaryDTO.getId(), Boolean.TRUE.equals(userSummaryDTO.getActive()));
        return "redirect:/admin/users";
    }

    // Mo form tao tai khoan moi (username/password/role) - tai su dung UserDto va AuthApiService.register()
    // da co san tu luong /admin/register, khac cho Role duoc chon tu do thay vi hardcode ROLE_ADMIN.
    @GetMapping("/new")
    public String createUserForm(Model model) {
        model.addAttribute("newUser", new UserDto());
        model.addAttribute("allRoles", roleApiService.getAllRoles());
        return "admin/user-form";
    }

    @PostMapping("/create")
    public String createUser(@ModelAttribute("newUser") UserDto userDto, Model model) {
        boolean success = authApiService.register(userDto);
        if (!success) {
            model.addAttribute("error", "Tạo tài khoản thất bại, username có thể đã tồn tại!");
            model.addAttribute("allRoles", roleApiService.getAllRoles());
            return "admin/user-form";
        }
        return "redirect:/admin/users";
    }
}