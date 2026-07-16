package com.github.houbb.core.identity.application.service;

import com.github.houbb.core.identity.application.domain.AccessRequest;

import java.util.List;

/**
 * Access Request 管理服务 — 提交、审批和查询访问申请。
 */
public interface AccessRequestService {

    /**
     * 提交访问申请。
     */
    AccessRequest submit(String requesterUserId, String organizationId, String accessPackageId,
                         String businessReason, String ticketReference,
                         long requestedStartAt, long requestedEndAt);

    /**
     * 申请人取消申请。
     */
    void cancel(String requestId, String requesterUserId);

    /**
     * 根据 ID 查询申请。
     */
    AccessRequest getById(String id);

    /**
     * 查询用户的申请列表。
     */
    List<AccessRequest> listByRequester(String requesterUserId);

    /**
     * 查询组织下的待审批申请。
     */
    List<AccessRequest> listPendingByOrg(String organizationId);

    /**
     * 查询组织下的所有申请。
     */
    List<AccessRequest> listByOrganization(String organizationId);
}
