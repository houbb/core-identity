package com.github.houbb.core.identity.application.service;

import com.github.houbb.core.identity.application.domain.AccessGrant;

import java.util.List;

/**
 * Grant 管理服务 — 创建、续期、撤销和到期处理访问授权。
 */
public interface GrantService {

    /**
     * 创建 Grant（由审批完成后调用）。
     */
    AccessGrant createGrant(String subjectType, String subjectId, String organizationId,
                             String entitlementId, String sourceType, String sourceId,
                             String grantType, long validFrom, long expiresAt, String grantedBy);

    /**
     * 查询主体所有活跃的 Grant。
     */
    List<AccessGrant> listActiveBySubject(String subjectId);

    /**
     * 查询主体在指定组织下的 Grant。
     */
    List<AccessGrant> listBySubjectAndOrg(String subjectId, String organizationId);

    /**
     * 按 ID 查询 Grant。
     */
    AccessGrant getById(String id);

    /**
     * 续期 Grant。
     */
    AccessGrant renew(String id, long newExpiresAt, String operatorId);

    /**
     * 撤销 Grant。
     */
    void revoke(String id, String revokedBy, String reason);

    /**
     * 标记 Grant 为已过期。
     */
    void markExpired(String id);

    /**
     * 查询即将到期的 Grant（用于定时任务处理）。
     */
    List<AccessGrant> findExpiringGrants(long beforeTimestamp);

    /**
     * 处理过期的 Grant — 撤销关联的角色/权限，增加 authorization_version。
     */
    void processExpiredGrant(String grantId);

    /**
     * 记录 Grant 被使用（更新 last_used_at）。
     */
    void recordUsage(String grantId);

    /**
     * 查询指定来源的 Grant。
     */
    List<AccessGrant> listBySource(String sourceType, String sourceId);
}
