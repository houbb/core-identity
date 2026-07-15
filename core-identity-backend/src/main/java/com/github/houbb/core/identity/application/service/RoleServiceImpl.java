package com.github.houbb.core.identity.application.service;

import com.github.houbb.core.identity.application.domain.Permission;
import com.github.houbb.core.identity.application.domain.Role;
import com.github.houbb.core.identity.application.port.MembershipRoleRepository;
import com.github.houbb.core.identity.application.port.PermissionRepository;
import com.github.houbb.core.identity.application.port.RolePermissionRepository;
import com.github.houbb.core.identity.application.port.RoleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

public class RoleServiceImpl implements RoleService {

    private static final Logger log = LoggerFactory.getLogger(RoleServiceImpl.class);

    private static final Set<String> RESERVED_ROLE_KEYS = Set.of("OWNER", "ADMIN", "MEMBER", "VIEWER");

    private final RoleRepository roleRepo;
    private final RolePermissionRepository rolePermissionRepo;
    private final MembershipRoleRepository membershipRoleRepo;
    private final PermissionRepository permissionRepo;

    public RoleServiceImpl(RoleRepository roleRepo,
                           RolePermissionRepository rolePermissionRepo,
                           MembershipRoleRepository membershipRoleRepo,
                           PermissionRepository permissionRepo) {
        this.roleRepo = roleRepo;
        this.rolePermissionRepo = rolePermissionRepo;
        this.membershipRoleRepo = membershipRoleRepo;
        this.permissionRepo = permissionRepo;
    }

    @Override
    @Transactional
    public List<Role> initializeBuiltInRoles(String organizationId, long now) {
        List<Role> roles = new ArrayList<>();
        List<BuiltInRoleDef> defs = getBuiltInRoleDefs();

        for (BuiltInRoleDef def : defs) {
            Role role = new Role();
            role.setId(UUID.randomUUID().toString());
            role.setOrganizationId(organizationId);
            role.setRoleKey(def.roleKey());
            role.setName(def.name());
            role.setDescription(def.description());
            role.setRoleType("BUILT_IN");
            role.setStatus("ACTIVE");
            role.setSystemProtected(1);
            role.setSortOrder(def.sortOrder());
            role.setCreatedBy("system");
            role.setCreatedAt(now);
            role.setUpdatedAt(now);
            role.setVersion(1);
            roleRepo.save(role);
            roles.add(role);

            // Assign core permissions
            if (def.corePermissionCodes() != null && !def.corePermissionCodes().isEmpty()) {
                for (String permCode : def.corePermissionCodes()) {
                    permissionRepo.findByCode(permCode).ifPresent(perm -> {
                        var rp = new com.github.houbb.core.identity.application.domain.RolePermission();
                        rp.setRoleId(role.getId());
                        rp.setPermissionId(perm.getId());
                        rp.setGrantedBy("system");
                        rp.setCreatedAt(now);
                        rolePermissionRepo.save(rp);
                    });
                }
            }
        }

        log.info("Initialized {} built-in roles for organization {}", roles.size(), organizationId);
        return roles;
    }

    @Override
    @Transactional
    public Role createCustomRole(String organizationId, String name, String description, String creatorId) {
        long now = System.currentTimeMillis();

        // Validate name
        if (name == null || name.trim().isEmpty()) {
            throw new ServiceException("IDENTITY_ROLE_NAME_REQUIRED", "角色名称不能为空");
        }
        name = name.trim();
        if (name.length() > 120) {
            throw new ServiceException("IDENTITY_ROLE_NAME_TOO_LONG", "角色名称最长120个字符");
        }

        // Check name uniqueness
        if (roleRepo.existsByNameInOrg(organizationId, name, null)) {
            throw new ServiceException("IDENTITY_ROLE_NAME_CONFLICT", "角色名称 " + name + " 已存在");
        }

        // Generate role_key from name
        String roleKey = generateRoleKey(name, organizationId);

        Role role = new Role();
        role.setId(UUID.randomUUID().toString());
        role.setOrganizationId(organizationId);
        role.setRoleKey(roleKey);
        role.setName(name);
        role.setDescription(description);
        role.setRoleType("CUSTOM");
        role.setStatus("ACTIVE");
        role.setSystemProtected(0);
        role.setSortOrder(10);
        role.setCreatedBy(creatorId);
        role.setCreatedAt(now);
        role.setUpdatedAt(now);
        role.setVersion(1);
        roleRepo.save(role);

        log.info("Custom role created: {} ({}) in org {}", name, roleKey, organizationId);
        return role;
    }

    @Override
    @Transactional
    public Role updateRole(String organizationId, String roleId, String name, String description) {
        Role role = findAndValidateRole(organizationId, roleId);

        if (role.getSystemProtected() == 1) {
            // Protected roles: only allow description updates
            if (name != null && !name.equals(role.getName())) {
                throw new ServiceException("IDENTITY_ROLE_PROTECTED", "内置角色的名称不可修改");
            }
        }

        long now = System.currentTimeMillis();
        if (name != null && !name.trim().isEmpty() && !name.equals(role.getName())) {
            name = name.trim();
            if (roleRepo.existsByNameInOrg(organizationId, name, roleId)) {
                throw new ServiceException("IDENTITY_ROLE_NAME_CONFLICT", "角色名称 " + name + " 已存在");
            }
            role.setName(name);
        }
        if (description != null) {
            role.setDescription(description);
        }
        role.setUpdatedAt(now);
        roleRepo.update(role);

        return role;
    }

    @Override
    @Transactional
    public void deleteRole(String organizationId, String roleId) {
        Role role = findAndValidateRole(organizationId, roleId);

        if (role.getSystemProtected() == 1) {
            throw new ServiceException("IDENTITY_ROLE_PROTECTED", "内置角色不可删除");
        }

        int memberCount = membershipRoleRepo.countByRoleId(roleId);
        if (memberCount > 0) {
            throw new ServiceException("IDENTITY_ROLE_IN_USE",
                    "角色正在被 " + memberCount + " 名成员使用，请先移除或迁移成员角色");
        }

        // Delete role permissions first
        rolePermissionRepo.deleteAllByRoleId(roleId);
        // Delete the role
        roleRepo.deleteById(roleId, role.getVersion());

        log.info("Role {} deleted from org {}", role.getName(), organizationId);
    }

    @Override
    @Transactional
    public void assignPermissions(String organizationId, String roleId,
                                   List<String> permissionIds, String operatorId) {
        Role role = findAndValidateRole(organizationId, roleId);
        long now = System.currentTimeMillis();

        // Validate all permission IDs are valid and not deprecated
        for (String permId : permissionIds) {
            Permission perm = permissionRepo.findById(permId)
                    .orElseThrow(() -> new ServiceException("IDENTITY_PERMISSION_NOT_FOUND",
                            "权限 " + permId + " 不存在"));
            if ("DEPRECATED".equals(perm.getStatus()) || "DISABLED".equals(perm.getStatus())) {
                throw new ServiceException("IDENTITY_PERMISSION_DEPRECATED",
                        "权限 " + perm.getPermissionCode() + " 已停用");
            }
        }

        rolePermissionRepo.replacePermissions(roleId, permissionIds, operatorId, now);
        log.info("Assigned {} permissions to role {} in org {}", permissionIds.size(), role.getName(), organizationId);
    }

    @Override
    public List<Role> getRoles(String organizationId) {
        return roleRepo.findByOrgId(organizationId);
    }

    @Override
    public Role getRole(String organizationId, String roleId) {
        return findAndValidateRole(organizationId, roleId);
    }

    @Override
    public List<BuiltInRoleDef> getBuiltInRoleDefs() {
        return List.of(
                new BuiltInRoleDef("OWNER", "Owner", "组织所有者 - 拥有完全管理权限", 0,
                        List.of("identity.organization.read", "identity.organization.update",
                                "identity.organization.delete", "identity.organization.transfer_ownership",
                                "identity.member.read", "identity.member.invite", "identity.member.update",
                                "identity.member.remove", "identity.role.read", "identity.role.create",
                                "identity.role.update", "identity.role.delete", "identity.invitation.read",
                                "identity.invitation.create", "identity.invitation.revoke")),
                new BuiltInRoleDef("ADMIN", "Administrator", "管理员 - 管理成员、角色和设置", 1,
                        List.of("identity.organization.read", "identity.member.read", "identity.member.invite",
                                "identity.member.update", "identity.member.remove", "identity.role.read",
                                "identity.role.create", "identity.role.update", "identity.role.delete",
                                "identity.invitation.read", "identity.invitation.create",
                                "identity.invitation.revoke")),
                new BuiltInRoleDef("MEMBER", "Member", "成员 - 基础访问权限", 2,
                        List.of("identity.organization.read", "identity.member.read", "identity.role.read",
                                "identity.invitation.read")),
                new BuiltInRoleDef("VIEWER", "Viewer", "观察者 - 只读访问权限", 3,
                        List.of("identity.organization.read", "identity.member.read"))
        );
    }

    private Role findAndValidateRole(String organizationId, String roleId) {
        Role role = roleRepo.findById(roleId)
                .orElseThrow(() -> new ServiceException("IDENTITY_ROLE_NOT_FOUND", "角色不存在"));
        if (!role.getOrganizationId().equals(organizationId)) {
            throw new ServiceException("IDENTITY_ROLE_ORGANIZATION_MISMATCH", "角色不属于当前组织");
        }
        return role;
    }

    private String generateRoleKey(String name, String organizationId) {
        // Simple slug generation: lowercase, replace non-alphanumeric with dash
        String slug = name.toLowerCase().replaceAll("[^a-z0-9\\u4e00-\\u9fff]+", "-")
                .replaceAll("^-|-$", "");
        if (slug.isEmpty() || slug.length() > 60) {
            slug = "role-" + System.currentTimeMillis() % 100000;
        }
        // Check uniqueness
        String baseSlug = slug;
        int suffix = 1;
        while (roleRepo.findByOrgAndKey(organizationId, slug).isPresent()) {
            slug = baseSlug + "-" + suffix;
            suffix++;
        }
        return slug;
    }

    public static class ServiceException extends RuntimeException {
        private final String errorCode;

        public ServiceException(String errorCode, String message) {
            super(message);
            this.errorCode = errorCode;
        }

        public String getErrorCode() {
            return errorCode;
        }
    }
}