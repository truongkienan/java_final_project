package com.ecommerce.frontend.controller;

import com.ecommerce.frontend.dto.PermissionDTO;
import com.ecommerce.frontend.dto.RoleDTO;
import com.ecommerce.frontend.service.RoleApiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/roles")
public class AdminRoleController {

    @Autowired
    private RoleApiService roleApiService;

    // Hien thi danh sach Role kem permission da gan
    @GetMapping
    public String manageRoles(Model model) {
        model.addAttribute("roles", roleApiService.getAllRoles());
        return "admin/roles";
    }

    @GetMapping("/new")
    public String createRoleForm(Model model) {
        model.addAttribute("role", new RoleDTO());
        model.addAttribute("allPermissions", roleApiService.getAllPermissions());
        return "admin/role-form";
    }

    @GetMapping("/edit/{id}")
    public String editRoleForm(@PathVariable("id") Short id, Model model) {
        RoleDTO role = roleApiService.getRoleById(id);
        // Suy ra permissionIds tu danh sach permissions day du de checkbox tren form tick dung
        if (role.getPermissions() != null) {
            role.setPermissionIds(role.getPermissions().stream()
                    .map(PermissionDTO::getPermissionId)
                    .collect(Collectors.toList()));
        }
        model.addAttribute("role", role);
        model.addAttribute("allPermissions", roleApiService.getAllPermissions());
        return "admin/role-form";
    }

    @PostMapping("/save")
    public String saveRole(@ModelAttribute RoleDTO roleDTO) {
        roleApiService.saveRole(roleDTO);
        return "redirect:/admin/roles";
    }

    @PostMapping("/delete/{id}")
    public String deleteRole(@PathVariable("id") Short id) {
        roleApiService.deleteRole(id);
        return "redirect:/admin/roles";
    }
}