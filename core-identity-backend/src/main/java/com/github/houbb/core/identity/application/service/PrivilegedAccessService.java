package com.github.houbb.core.identity.application.service;

import com.github.houbb.core.identity.application.domain.PrivilegedActivation;

import java.util.List;

/**
 * Privileged Access 管理服务 — 激活、管理和终止特权访问。
 */
public interface PrivilegedAccessService {

    /**
     * 激活特权（Eligible → Active）。
     */
    PrivilegedActivation activate(String userId, String organizationId, String roleId,
                                   String reason, String ticketReference,
                                   String authenticationLevel, long durationSeconds);

    /**
     * 提前结束特权访问。
     */
    void end(String activationId, String userId);

    /**
     * 处理过期的特权激活（由定时任务调用）。
     */
    void processExpiredActivations();

    /**
     * 查询用户当前活跃的特权激活。
     */
    List<PrivilegedActivation> listActiveByUser(String userId);

    /**
     * 查询用户的所有特权激活记录。
     */
    List<PrivilegedActivation> listByUser(String userId);

    /**
     * 根据 ID 查询。
     */
    PrivilegedActivation getById(String id);
}
