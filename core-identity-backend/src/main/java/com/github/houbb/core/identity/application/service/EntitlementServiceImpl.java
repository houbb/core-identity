package com.github.houbb.core.identity.application.service;

import com.github.houbb.core.identity.application.domain.Entitlement;
import com.github.houbb.core.identity.application.port.EntitlementRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class EntitlementServiceImpl implements EntitlementService {

    private static final Logger log = LoggerFactory.getLogger(EntitlementServiceImpl.class);

    private static final List<String> VALID_ENTITLEMENT_TYPES = List.of(
            "ROLE", "PERMISSION", "SCOPE", "SERVICE_ACCOUNT", "ADMIN_CONSOLE"
    );

    private final EntitlementRepository entitlementRepo;

    public EntitlementServiceImpl(EntitlementRepository entitlementRepo) {
        this.entitlementRepo = entitlementRepo;
    }

    @Override
    @Transactional
    public Entitlement register(String organizationId, String entitlementType, String targetId,
                                String code, String name, String riskLevel, String ownerUserId) {
        validateEntitlementType(entitlementType);
        long now = System.currentTimeMillis();

        // 检查 Code 唯一性
        if (entitlementRepo.findByCode(code).isPresent()) {
            throw new ServiceException("IDENTITY_ENTITLEMENT_CODE_CONFLICT",
                    "权益代码 " + code + " 已存在");
        }

        Entitlement entitlement = new Entitlement();
        entitlement.setId(UUID.randomUUID().toString());
        entitlement.setOrganizationId(organizationId);
        entitlement.setEntitlementType(entitlementType);
        entitlement.setTargetId(targetId);
        entitlement.setCode(code);
        entitlement.setName(name);
        entitlement.setRiskLevel(riskLevel != null ? riskLevel : "LOW");
        entitlement.setOwnerUserId(ownerUserId);
        entitlement.setStatus("ACTIVE");
        entitlement.setReviewFrequencyDays(180);
        entitlement.setCreatedAt(now);
        entitlement.setUpdatedAt(now);
        entitlement.setVersion(1);

        entitlementRepo.save(entitlement);
        log.info("Registered entitlement: code={}, type={}, org={}", code, entitlementType, organizationId);
        return entitlement;
    }

    @Override
    @Transactional
    public List<Entitlement> registerBatch(List<EntitlementDef> defs) {
        List<Entitlement> result = new ArrayList<>();
        for (EntitlementDef def : defs) {
            // 如果已存在则跳过
            if (entitlementRepo.findByCode(def.code()).isPresent()) {
                log.debug("Entitlement {} already registered, skipping", def.code());
                continue;
            }
            try {
                result.add(register(def.organizationId(), def.entitlementType(), def.targetId(),
                        def.code(), def.name(), def.riskLevel(), def.ownerUserId()));
            } catch (ServiceException e) {
                log.warn("Failed to register entitlement {}: {}", def.code(), e.getMessage());
            }
        }
        return result;
    }

    @Override
    public Entitlement getById(String id) {
        return entitlementRepo.findById(id)
                .orElseThrow(() -> new ServiceException("IDENTITY_ENTITLEMENT_NOT_FOUND",
                        "权益 " + id + " 不存在"));
    }

    @Override
    public Entitlement getByCode(String code) {
        return entitlementRepo.findByCode(code)
                .orElseThrow(() -> new ServiceException("IDENTITY_ENTITLEMENT_NOT_FOUND",
                        "权益代码 " + code + " 不存在"));
    }

    @Override
    public List<Entitlement> listByOrganization(String organizationId) {
        return entitlementRepo.findByOrganizationId(organizationId);
    }

    @Override
    public List<Entitlement> listByOrganizationAndType(String organizationId, String entitlementType) {
        return entitlementRepo.findByOrganizationIdAndType(organizationId, entitlementType);
    }

    @Override
    public List<Entitlement> listByTarget(String targetId) {
        return entitlementRepo.findByTargetId(targetId);
    }

    @Override
    public List<Entitlement> listAllActive() {
        return entitlementRepo.findAllActive();
    }

    @Override
    @Transactional
    public Entitlement update(String id, String name, String riskLevel, String ownerUserId, int reviewFrequencyDays) {
        Entitlement entitlement = getById(id);
        long now = System.currentTimeMillis();

        if (name != null && !name.trim().isEmpty()) {
            entitlement.setName(name.trim());
        }
        if (riskLevel != null) {
            entitlement.setRiskLevel(riskLevel);
        }
        if (ownerUserId != null) {
            entitlement.setOwnerUserId(ownerUserId);
        }
        if (reviewFrequencyDays > 0) {
            entitlement.setReviewFrequencyDays(reviewFrequencyDays);
        }
        entitlement.setUpdatedAt(now);

        entitlementRepo.update(entitlement);
        log.info("Updated entitlement: id={}", id);
        return entitlement;
    }

    @Override
    @Transactional
    public void disable(String id) {
        Entitlement entitlement = getById(id);
        if ("DISABLED".equals(entitlement.getStatus())) {
            return;
        }
        entitlementRepo.updateStatus(id, "DISABLED", System.currentTimeMillis(), entitlement.getVersion());
        log.info("Disabled entitlement: id={}", id);
    }

    @Override
    @Transactional
    public void enable(String id) {
        Entitlement entitlement = getById(id);
        if ("ACTIVE".equals(entitlement.getStatus())) {
            return;
        }
        entitlementRepo.updateStatus(id, "ACTIVE", System.currentTimeMillis(), entitlement.getVersion());
        log.info("Enabled entitlement: id={}", id);
    }

    private void validateEntitlementType(String type) {
        if (type == null || !VALID_ENTITLEMENT_TYPES.contains(type)) {
            throw new ServiceException("IDENTITY_ENTITLEMENT_INVALID_TYPE",
                    "无效的权益类型: " + type + "，有效值: " + String.join(", ", VALID_ENTITLEMENT_TYPES));
        }
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
