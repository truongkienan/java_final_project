package com.ecommerce.auth.controller;

import com.ecommerce.auth.entity.Permission;
import com.ecommerce.auth.repository.PermissionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/permissions")
public class PermissionController {

    @Autowired
    private PermissionRepository permissionRepository;

    @GetMapping
    public List<Permission> getAllPermissions() {
        return permissionRepository.findAll();
    }

    @GetMapping("/{id}")
    public Permission getPermission(@PathVariable("id") Short id) {
        return permissionRepository.findById(id).orElse(null);
    }

    @PostMapping
    public Permission createPermission(@RequestBody Permission permission) {
        return permissionRepository.save(permission);
    }

    @PutMapping("/{id}")
    public Permission updatePermission(@PathVariable("id") Short id, @RequestBody Permission permission) {
        permission.setPermissionId(id);
        return permissionRepository.save(permission);
    }

    @DeleteMapping("/{id}")
    public void deletePermission(@PathVariable("id") Short id) {
        permissionRepository.deleteById(id);
    }
}