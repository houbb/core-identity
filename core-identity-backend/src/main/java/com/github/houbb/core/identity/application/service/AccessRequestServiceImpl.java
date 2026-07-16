package com.github.houbb.core.identity.application.service;

import com.github.houbb.core.identity.application.domain.AccessPackage;
import com.github.houbb.core.identity.application.domain.AccessRequest;
import com.github.houbb.core.identity.application.port.AccessPackageRepository;
import com.github.houbb.core.identity.application.port.AccessRequestRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

public class AccessRequestServiceImpl implements AccessRequestService {

    private static final Logger log = LoggerFactory.getLogger(AccessRequestServiceImpl.class);

    private final AccessRequestRepository requestRepo;
    private final AccessPackageRepository packageRepo;

    public AccessRequestServiceImpl(AccessRequestRepository requestRepo,
                                    AccessPackageRepository packageRepo) {
        this.requestRepo = requestRepo;
        this.packageRepo = packageRepo;
    }

    @Override
    @Transactional
    public AccessRequest submit(String requesterUserId, String organizationId,
                                String accessPackageId, String businessReason,
                                String ticketReference, long requestedStartAt, long requestedEndAt) {
        long now = System.currentTimeMillis();

        // 验证套餐存在且可申请
        AccessPackage pkg = packageRepo.findById(accessPackageId)
                .orElseThrow(() -> new ServiceException("IDENTITY_PACKAGE_NOT_FOUND",
                        "访问套餐 " + accessPackageId + " 不存在"));
        if (!"ACTIVE".equals(pkg.getStatus())) {
            throw new ServiceException("IDENTITY_PACKAGE_NOT_ACTIVE",
                    "访问套餐 " + pkg.getName() + " 当前不可申请");
        }
        if (pkg.getRequestable() != 1) {
            throw new ServiceException("IDENTITY_PACKAGE_NOT_REQUESTABLE",
                    "访问套餐 " + pkg.getName() + " 不允许直接申请");
        }

        // 验证业务理由（高风险必须提供）
        if ("HIGH".equals(pkg.getRiskLevel()) || "CRITICAL".equals(pkg.getRiskLevel())) {
            if (businessReason == null || businessReason.trim().isEmpty()) {
                throw new ServiceException("IDENTITY_REQUEST_REASON_REQUIRED",
                        "高风险访问必须提供业务理由");
            }
        }

        AccessRequest request = new AccessRequest();
        request.setId(UUID.randomUUID().toString());
        request.setRequesterUserId(requesterUserId);
        request.setTargetSubjectType("USER");
        request.setTargetSubjectId(requesterUserId);
        request.setOrganizationId(organizationId);
        request.setAccessPackageId(accessPackageId);
        request.setBusinessReason(businessReason);
        request.setTicketReference(ticketReference);
        request.setRequestedStartAt(requestedStartAt > 0 ? requestedStartAt : now);
        request.setRequestedEndAt(requestedEndAt);
        request.setStatus("SUBMITTED");
        request.setRiskLevel(pkg.getRiskLevel());
        request.setSubmittedAt(now);
        request.setCreatedAt(now);
        request.setUpdatedAt(now);
        request.setVersion(1);

        requestRepo.save(request);

        log.info("Access request submitted: id={}, package={}, user={}, org={}",
                request.getId(), pkg.getName(), requesterUserId, organizationId);
        return request;
    }

    @Override
    @Transactional
    public void cancel(String requestId, String requesterUserId) {
        AccessRequest request = getById(requestId);

        if (!request.getRequesterUserId().equals(requesterUserId)) {
            throw new ServiceException("IDENTITY_REQUEST_NOT_OWNER",
                    "只能取消自己的申请");
        }
        if (!"SUBMITTED".equals(request.getStatus()) && !"IN_REVIEW".equals(request.getStatus())) {
            throw new ServiceException("IDENTITY_REQUEST_CANNOT_CANCEL",
                    "当前状态 " + request.getStatus() + " 不支持取消");
        }

        long now = System.currentTimeMillis();
        requestRepo.updateStatus(requestId, "CANCELLED", now, now, request.getVersion());
        log.info("Access request cancelled: id={}", requestId);
    }

    @Override
    public AccessRequest getById(String id) {
        return requestRepo.findById(id)
                .orElseThrow(() -> new ServiceException("IDENTITY_REQUEST_NOT_FOUND",
                        "访问申请 " + id + " 不存在"));
    }

    @Override
    public List<AccessRequest> listByRequester(String requesterUserId) {
        return requestRepo.findByRequesterId(requesterUserId);
    }

    @Override
    public List<AccessRequest> listPendingByOrg(String organizationId) {
        return requestRepo.findPendingByOrg(organizationId);
    }

    @Override
    public List<AccessRequest> listByOrganization(String organizationId) {
        return requestRepo.findByOrganizationId(organizationId);
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
