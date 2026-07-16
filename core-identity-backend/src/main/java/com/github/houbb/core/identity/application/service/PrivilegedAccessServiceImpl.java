package com.github.houbb.core.identity.application.service;

import com.github.houbb.core.identity.application.domain.PrivilegedActivation;
import com.github.houbb.core.identity.application.port.PrivilegedActivationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

public class PrivilegedAccessServiceImpl implements PrivilegedAccessService {

    private static final Logger log = LoggerFactory.getLogger(PrivilegedAccessServiceImpl.class);

    private static final long MAX_DURATION_SECONDS = 4 * 3600; // 最长 4 小时

    private final PrivilegedActivationRepository activationRepo;

    public PrivilegedAccessServiceImpl(PrivilegedActivationRepository activationRepo) {
        this.activationRepo = activationRepo;
    }

    @Override
    @Transactional
    public PrivilegedActivation activate(String userId, String organizationId, String roleId,
                                          String reason, String ticketReference,
                                          String authenticationLevel, long durationSeconds) {
        long now = System.currentTimeMillis();

        // 验证理由
        if (reason == null || reason.trim().isEmpty()) {
            throw new ServiceException("IDENTITY_PRIV_ACT_REASON_REQUIRED",
                    "特权激活必须提供业务理由");
        }

        // 限制最大时长
        if (durationSeconds <= 0) {
            durationSeconds = 3600; // 默认 1 小时
        }
        if (durationSeconds > MAX_DURATION_SECONDS) {
            throw new ServiceException("IDENTITY_PRIV_ACT_DURATION_EXCEEDED",
                    "特权激活最长 " + (MAX_DURATION_SECONDS / 3600) + " 小时");
        }

        // 检查是否已有活跃的特权（同一用户+同一角色）
        activationRepo.findActiveByUserIdAndRole(userId, roleId).ifPresent(existing -> {
            throw new ServiceException("IDENTITY_PRIV_ACT_ALREADY_ACTIVE",
                    "该特权已激活，请先结束现有激活");
        });

        PrivilegedActivation activation = new PrivilegedActivation();
        activation.setId(UUID.randomUUID().toString());
        activation.setUserId(userId);
        activation.setOrganizationId(organizationId);
        activation.setRoleId(roleId);
        activation.setReason(reason.trim());
        activation.setTicketReference(ticketReference);
        activation.setStatus("ACTIVE");
        activation.setAuthenticationLevel(authenticationLevel != null ? authenticationLevel : "AUTH_LEVEL_1");
        activation.setActivatedAt(now);
        activation.setExpiresAt(now + durationSeconds * 1000);
        activation.setCreatedAt(now);
        activation.setUpdatedAt(now);
        activation.setVersion(1);

        activationRepo.save(activation);
        log.info("Privileged access activated: user={}, role={}, until={}, reason={}",
                userId, roleId, activation.getExpiresAt(), reason);
        return activation;
    }

    @Override
    @Transactional
    public void end(String activationId, String userId) {
        PrivilegedActivation activation = getById(activationId);
        if (!activation.getUserId().equals(userId)) {
            throw new ServiceException("IDENTITY_PRIV_ACT_NOT_OWNER",
                    "只能结束自己的特权激活");
        }
        if (!"ACTIVE".equals(activation.getStatus())) {
            return;
        }
        long now = System.currentTimeMillis();
        activationRepo.end(activationId, now, now, activation.getVersion());
        log.info("Privileged access ended: id={}, user={}", activationId, userId);
    }

    @Override
    @Transactional
    public void processExpiredActivations() {
        long now = System.currentTimeMillis();
        List<PrivilegedActivation> expired = activationRepo.findExpiringActivations(now);
        for (PrivilegedActivation act : expired) {
            activationRepo.end(act.getId(), now, now, act.getVersion());
            log.info("Privileged access auto-expired: id={}, user={}, role={}",
                    act.getId(), act.getUserId(), act.getRoleId());
        }
    }

    @Override
    public List<PrivilegedActivation> listActiveByUser(String userId) {
        return activationRepo.findActiveByUserId(userId);
    }

    @Override
    public List<PrivilegedActivation> listByUser(String userId) {
        return activationRepo.findByUserId(userId);
    }

    @Override
    public PrivilegedActivation getById(String id) {
        return activationRepo.findById(id)
                .orElseThrow(() -> new ServiceException("IDENTITY_PRIV_ACT_NOT_FOUND",
                        "特权激活 " + id + " 不存在"));
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
