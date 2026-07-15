package com.github.houbb.core.identity.api.publicapi.controller;

import com.github.houbb.core.identity.application.domain.Role;
import com.github.houbb.core.identity.application.port.RolePermissionRepository;
import com.github.houbb.core.identity.application.service.RoleService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * Public API for role management within an organization.
 */
@RestController
@RequestMapping("/api/v1/identity/organizations/{organizationId}")
public class RoleController {

    private final RoleService roleService;
    private final RolePermissionRepository rolePermissionRepo;

    public RoleController(RoleService roleService, RolePermissionRepository rolePermissionRepo) {
        this.roleService = roleService;
        this.rolePermissionRepo = rolePermissionRepo;
    }

    @GetMapping("/roles")
    public ResponseEntity<Map<String, Object>> getRoles(@PathVariable String organizationId) {
        List<Role> roles = roleService.getRoles(organizationId);
        List<Map<String, Object>> result = new ArrayList<>();
        for (Role role : roles) {
            result.add(toRoleMap(role));
        }
        return ResponseEntity.ok(Map.of("roles", result, "total", result.size()));
    }

    @PostMapping("/roles")
    public ResponseEntity<Map<String, Object>> createRole(
            @PathVariable String organizationId,
            @RequestBody Map<String, String> body) {
        String name = body.get("name");
        String description = body.getOrDefault("description", "");
        String creatorId = body.getOrDefault("createdBy", "system");

        Role role = roleService.createCustomRole(organizationId, name, description, creatorId);
        return ResponseEntity.status(201).body(toRoleMap(role));
    }

    @GetMapping("/roles/{roleId}")
    public ResponseEntity<Map<String, Object>> getRole(
            @PathVariable String organizationId,
            @PathVariable String roleId) {
        Role role = roleService.getRole(organizationId, roleId);
        return ResponseEntity.ok(toRoleMap(role));
    }

    @PatchMapping("/roles/{roleId}")
    public ResponseEntity<Map<String, Object>> updateRole(
            @PathVariable String organizationId,
            @PathVariable String roleId,
            @RequestBody Map<String, String> body) {
        Role role = roleService.updateRole(organizationId, roleId,
                body.get("name"), body.get("description"));
        return ResponseEntity.ok(toRoleMap(role));
    }

    @DeleteMapping("/roles/{roleId}")
    public ResponseEntity<Map<String, Object>> deleteRole(
            @PathVariable String organizationId,
            @PathVariable String roleId) {
        roleService.deleteRole(organizationId, roleId);
        return ResponseEntity.ok(Map.of("message", "角色已删除", "roleId", roleId));
    }

    @PutMapping("/roles/{roleId}/permissions")
    public ResponseEntity<Map<String, Object>> assignPermissions(
            @PathVariable String organizationId,
            @PathVariable String roleId,
            @RequestBody Map<String, Object> body) {
        @SuppressWarnings("unchecked")
        List<String> permissionIds = (List<String>) body.get("permissionIds");
        if (permissionIds == null) {
            permissionIds = Collections.emptyList();
        }
        String operatorId = (String) body.getOrDefault("operatorId", "system");

        roleService.assignPermissions(organizationId, roleId, permissionIds, operatorId);

        return ResponseEntity.ok(Map.of(
                "message", "权限已更新",
                "roleId", roleId,
                "permissionCount", permissionIds.size()
        ));
    }

    private Map<String, Object> toRoleMap(Role role) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", role.getId());
        map.put("organizationId", role.getOrganizationId());
        map.put("roleKey", role.getRoleKey());
        map.put("name", role.getName());
        map.put("description", role.getDescription());
        map.put("roleType", role.getRoleType());
        map.put("status", role.getStatus());
        map.put("systemProtected", role.getSystemProtected() == 1);
        map.put("sortOrder", role.getSortOrder());
        map.put("createdBy", role.getCreatedBy());

        // Attach permission count
        List<String> permIds = rolePermissionRepo.findPermissionIdsByRoleId(role.getId());
        map.put("permissionCount", permIds.size());
        map.put("permissionIds", permIds);

        map.put("createdAt", role.getCreatedAt());
        map.put("updatedAt", role.getUpdatedAt());
        return map;
    }
}