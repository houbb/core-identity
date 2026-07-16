package com.github.houbb.core.identity.application.service;

import com.github.houbb.core.identity.application.domain.Entitlement;

import java.util.List;

/**
 * Entitlement 管理服务 — 注册、查询和治理可被审计的访问权益。
 */
public interface EntitlementService {

    /**
     * 注册一个新的 Entitlement（将现有 Role/Permission/Scope 注册为可治理权益）。
     */
    Entitlement register(String organizationId, String entitlementType, String targetId,
                         String code, String name, String riskLevel, String ownerUserId);

    /**
     * 批量注册 Entitlement（用于 P5→P6 数据迁移）。
     */
    List<Entitlement> registerBatch(List<EntitlementDef> defs);

    /**
     * 按 ID 查询。
     */
    Entitlement getById(String id);

    /**
     * 按 Code 查询。
     */
    Entitlement getByCode(String code);

    /**
     * 查询组织下所有 Entitlement。
     */
    List<Entitlement> listByOrganization(String organizationId);

    /**
     * 按类型查询组织下的 Entitlement。
     */
    List<Entitlement> listByOrganizationAndType(String organizationId, String entitlementType);

    /**
     * 查询与指定 target（Role/Permission 等）关联的 Entitlement。
     */
    List<Entitlement> listByTarget(String targetId);

    /**
     * 查询所有活跃的 Entitlement。
     */
    List<Entitlement> listAllActive();

    /**
     * 更新 Entitlement 属性和所有者。
     */
    Entitlement update(String id, String name, String riskLevel, String ownerUserId, int reviewFrequencyDays);

    /**
     * 禁用 Entitlement。
     */
    void disable(String id);

    /**
     * 启用 Entitlement。
     */
    void enable(String id);

    /**
     * 注册定义。
     */
    record EntitlementDef(String organizationId, String entitlementType, String targetId,
                          String code, String name, String riskLevel, String ownerUserId) {
    }
}
