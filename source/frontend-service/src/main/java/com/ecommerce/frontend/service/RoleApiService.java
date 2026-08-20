package com.ecommerce.frontend.service;

import com.ecommerce.frontend.dto.PermissionDTO;
import com.ecommerce.frontend.dto.RoleDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class RoleApiService {

    @Autowired
    private RestTemplate restTemplate;

    @Value("${ROLE_SERVICE_URL:http://localhost:8085/api/roles}")
    private String roleServiceUrl;

    @Value("${PERMISSION_SERVICE_URL:http://localhost:8085/api/permissions}")
    private String permissionServiceUrl;

    public List<RoleDTO> getAllRoles() {
        ResponseEntity<RoleDTO[]> response = restTemplate.getForEntity(roleServiceUrl, RoleDTO[].class);
        RoleDTO[] roles = response.getBody();
        return roles != null ? Arrays.asList(roles) : Arrays.asList();
    }

    public RoleDTO getRoleById(Short id) {
        return restTemplate.getForObject(roleServiceUrl + "/" + id, RoleDTO.class);
    }

    public List<PermissionDTO> getAllPermissions() {
        ResponseEntity<PermissionDTO[]> response = restTemplate.getForEntity(permissionServiceUrl, PermissionDTO[].class);
        PermissionDTO[] permissions = response.getBody();
        return permissions != null ? Arrays.asList(permissions) : Arrays.asList();
    }

    // Gửi đúng shape RoleRequest{roleName, description, permissionIds} mà RoleController (auth-service) đang nhận,
    // không gửi thẳng RoleDTO vì field "permissions" (List<PermissionDTO> đầy đủ) không khớp kiểu RoleController mong đợi.
    public void saveRole(RoleDTO roleDTO) {
        Map<String, Object> body = new HashMap<>();
        body.put("roleName", roleDTO.getRoleName());
        body.put("description", roleDTO.getDescription());
        body.put("permissionIds", roleDTO.getPermissionIds());

        if (roleDTO.getRoleId() == null) {
            restTemplate.postForObject(roleServiceUrl, body, RoleDTO.class);
        } else {
            restTemplate.put(roleServiceUrl + "/" + roleDTO.getRoleId(), body);
        }
    }

    public void deleteRole(Short id) {
        restTemplate.delete(roleServiceUrl + "/" + id);
    }
}