package com.github.houbb.core.identity.application.service;

import com.github.houbb.core.identity.application.port.PlatformOperatorRoleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * 管理员分权服务 — 管理平台操作员的细分角色。
 */
public class AdminRoleService {

    private static final Logger log = LoggerFactory.getLogger(AdminRoleService.class);

    public static final List<String> VALID_ROLES = List.of(
            "PLATFORM_IDENTITY_ADMIN", "PLATFORM_SECURITY_ADMIN",
            "PLATFORM_AUDIT_ADMIN", "PLATFORM_SUPPORT_ADMIN",
            "PLATFORM_PRIVACY_ADMIN", "PLATFORM_COMPLIANCE_ADMIN",
            "PLATFORM_APPLICATION_ADMIN", "PLATFORM_READ_ONLY_ADMIN"
    );

    private final PlatformOperatorRoleRepository roleRepo;

    public AdminRoleService(PlatformOperatorRoleRepository roleRepo) {
        this.roleRepo = roleRepo;
    }

    @Transactional
    public void assignRole(String operatorId, String roleCode, String grantedBy) {
        if (!VALID_ROLES.contains(roleCode)) {
            throw new ServiceException("IDENTITY_INVALID_ADMIN_ROLE",
                    "无效的管理员角色: " + roleCode);
        }
        long now = System.currentTimeMillis();
        String id = UUID.randomUUID().toString();
        roleRepo.save(id, operatorId, roleCode, grantedBy, now, now);
        log.info("Assigned admin role {} to operator {}", roleCode, operatorId);
    }

    @Transactional
    public void revokeRole(String operatorId, String roleCode) {
        roleRepo.delete(operatorId, roleCode);
        log.info("Revoked admin role {} from operator {}", roleCode, operatorId);
    }

    public List<String> listOperatorRoles(String operatorId) {
        return roleRepo.findRoleCodesByOperatorId(operatorId);
    }

    public boolean hasRole(String operatorId, String roleCode) {
        return roleRepo.countByOperatorAndRole(operatorId, roleCode) > 0;
    }

    public static class ServiceException extends RuntimeException {
        private final String errorCode;
        public ServiceException(String errorCode, String message) {
            super(message);
            this.errorCode = errorCode;
        }
        public String getErrorCode() { return errorCode; }
    }
}
