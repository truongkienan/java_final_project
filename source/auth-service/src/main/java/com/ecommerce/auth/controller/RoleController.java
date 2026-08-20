package com.ecommerce.auth.controller;

import com.ecommerce.auth.dto.RoleRequest;
import com.ecommerce.auth.entity.Permission;
import com.ecommerce.auth.entity.Role;
import com.ecommerce.auth.repository.PermissionRepository;
import com.ecommerce.auth.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/roles")
public class RoleController {

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PermissionRepository permissionRepository;

    @GetMapping
    public List<Role> getAllRoles() {
        return roleRepository.findAll();
    }

    @GetMapping("/{id}")
    public Role getRole(@PathVariable("id") Short id) {
        return roleRepository.findById(id).orElse(null);
    }

    @PostMapping
    public Role createRole(@RequestBody RoleRequest request) {
        Role role = new Role();
        role.setRoleName(request.getRoleName());
        role.setDescription(request.getDescription());
        role.setPermissions(resolvePermissions(request.getPermissionIds()));
        return roleRepository.save(role);
    }

    @PutMapping("/{id}")
    public Role updateRole(@PathVariable("id") Short id, @RequestBody RoleRequest request) {
        Role role = roleRepository.findById(id).orElseThrow();
        role.setRoleName(request.getRoleName());
        role.setDescription(request.getDescription());
        role.setPermissions(resolvePermissions(request.getPermissionIds()));
        return roleRepository.save(role);
    }

    @DeleteMapping("/{id}")
    public void deleteRole(@PathVariable("id") Short id) {
        roleRepository.deleteById(id);
    }

    private Set<Permission> resolvePermissions(List<Short> permissionIds) {
        if (permissionIds == null || permissionIds.isEmpty()) {
            return new HashSet<>();
        }
        return new HashSet<>(permissionRepository.findAllById(permissionIds));
    }
}