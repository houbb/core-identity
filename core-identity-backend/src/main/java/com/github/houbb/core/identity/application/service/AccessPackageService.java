package com.github.houbb.core.identity.application.service;

import com.github.houbb.core.identity.application.domain.AccessPackage;

import java.util.List;

/**
 * Access Package 管理服务 — 创建、查询和管理访问套餐。
 */
public interface AccessPackageService {

    /**
     * 创建访问套餐。
     */
    AccessPackage createPackage(String organizationId, String name, String description,
                                String packageType, String riskLevel, String ownerUserId,
                                long defaultDurationSeconds, long maxDurationSeconds,
                                String requiredAuthLevel, List<String> entitlementIds);

    /**
     * 更新访问套餐。
     */
    AccessPackage updatePackage(String packageId, String name, String description,
                                String packageType, String riskLevel, String ownerUserId,
                                long defaultDurationSeconds, long maxDurationSeconds,
                                String requiredAuthLevel);

    /**
     * 管理套餐包含的权益（原子替换）。
     */
    void setEntitlements(String packageId, List<String> entitlementIds);

    /**
     * 获取套餐包含的权益 ID 列表。
     */
    List<String> getEntitlementIds(String packageId);

    /**
     * 删除套餐。
     */
    void deletePackage(String packageId);

    /**
     * 启用/禁用套餐。
     */
    void setPackageStatus(String packageId, String status);

    /**
     * 根据 ID 查询套餐。
     */
    AccessPackage getById(String id);

    /**
     * 查询组织下的所有套餐。
     */
    List<AccessPackage> listByOrganization(String organizationId);

    /**
     * 查询组织下可申请的活跃套餐。
     */
    List<AccessPackage> listRequestable(String organizationId);

    /**
     * 按类型查询组织下的套餐。
     */
    List<AccessPackage> listByOrganizationAndType(String organizationId, String packageType);
}
