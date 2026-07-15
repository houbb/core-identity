package com.github.houbb.core.identity.application.service;

import com.github.houbb.core.identity.application.domain.AccountRecovery;
import com.github.houbb.core.identity.application.domain.SecurityEvent;
import com.github.houbb.core.identity.application.domain.User;
import com.github.houbb.core.identity.application.port.AccountRecoveryRepository;
import com.github.houbb.core.identity.application.port.SecurityEventRepository;
import com.github.houbb.core.identity.application.port.SessionRepository;
import com.github.houbb.core.identity.application.port.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Account recovery service — handles tiered recovery flows.
 */
public class AccountRecoveryService {

    private static final Logger log = LoggerFactory.getLogger(AccountRecoveryService.class);
    private static final long COOLING_OFF_MS = 30 * 60 * 1000L;

    private final AccountRecoveryRepository recoveryRepo;
    private final UserRepository userRepo;
    private final SessionRepository sessionRepo;
    private final SecurityEventRepository securityEventRepo;

    public AccountRecoveryService(AccountRecoveryRepository recoveryRepo, UserRepository userRepo,
                                  SessionRepository sessionRepo, SecurityEventRepository securityEventRepo) {
        this.recoveryRepo = recoveryRepo;
        this.userRepo = userRepo;
        this.sessionRepo = sessionRepo;
        this.securityEventRepo = securityEventRepo;
    }

    @Transactional
    public AccountRecovery initiate(String userId, String recoveryType, String initiatedIp, String initiatedDeviceId) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        long now = System.currentTimeMillis();
        AccountRecovery recovery = new AccountRecovery();
        recovery.setId(UUID.randomUUID().toString());
        recovery.setUserId(userId);
        recovery.setRecoveryType(recoveryType);
        recovery.setStatus("PENDING_VERIFICATION");
        recovery.setInitiatedIp(initiatedIp);
        recovery.setInitiatedDeviceId(initiatedDeviceId);
        recovery.setCreatedAt(now);
        recovery.setUpdatedAt(now);
        recovery.setVersion(1);
        recoveryRepo.save(recovery);

        log.info("Account recovery initiated for user {}: type={}", userId, recoveryType);
        return recovery;
    }

    @Transactional
    public AccountRecovery verify(String recoveryId, String userId) {
        AccountRecovery recovery = recoveryRepo.findById(recoveryId)
                .orElseThrow(() -> new RuntimeException("Recovery not found"));

        if (!recovery.getUserId().equals(userId)) {
            throw new RuntimeException("Recovery does not belong to user");
        }
        if (!"PENDING_VERIFICATION".equals(recovery.getStatus())) {
            throw new RuntimeException("Recovery is not in pending verification status");
        }

        long now = System.currentTimeMillis();
        recovery.setStatus("COOLING_OFF");
        recovery.setCoolingOffUntil(now + COOLING_OFF_MS);
        recovery.setUpdatedAt(now);
        recoveryRepo.update(recovery);

        log.info("Account recovery verified, entering cooling-off: {}", recoveryId);
        return recovery;
    }

    @Transactional
    public void cancel(String recoveryId, String userId) {
        AccountRecovery recovery = recoveryRepo.findById(recoveryId)
                .orElseThrow(() -> new RuntimeException("Recovery not found"));

        if (!recovery.getUserId().equals(userId)) {
            throw new RuntimeException("Recovery does not belong to user");
        }

        long now = System.currentTimeMillis();
        recovery.setStatus("CANCELLED");
        recovery.setCancelledAt(now);
        recovery.setUpdatedAt(now);
        recoveryRepo.update(recovery);
    }

    @Transactional
    public void complete(String recoveryId, String userId) {
        AccountRecovery recovery = recoveryRepo.findById(recoveryId)
                .orElseThrow(() -> new RuntimeException("Recovery not found"));

        if (!recovery.getUserId().equals(userId)) {
            throw new RuntimeException("Recovery does not belong to user");
        }

        long now = System.currentTimeMillis();
        if ("COOLING_OFF".equals(recovery.getStatus())) {
            if (recovery.getCoolingOffUntil() != null && recovery.getCoolingOffUntil() > now) {
                throw new RuntimeException("Recovery is still in cooling-off period");
            }
        }

        User user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setSecurityVersion(user.getSecurityVersion() + 1);
        user.setRecoveryState("COMPLETED");
        user.setUpdatedAt(now);
        userRepo.update(user);

        sessionRepo.revokeByUserId(userId, "ACCOUNT_RECOVERY", now);

        recovery.setStatus("COMPLETED");
        recovery.setCompletedAt(now);
        recovery.setUpdatedAt(now);
        recoveryRepo.update(recovery);

        createSecurityEvent(userId, "ACCOUNT_RECOVERY_COMPLETED", "HIGH");
        log.info("Account recovery completed for user {}, all sessions revoked", userId);
    }

    public List<AccountRecovery> findPendingRecoveries() {
        return recoveryRepo.findPending();
    }

    public Optional<AccountRecovery> findById(String id) {
        return recoveryRepo.findById(id);
    }

    private void createSecurityEvent(String userId, String eventType, String severity) {
        try {
            long now = System.currentTimeMillis();
            SecurityEvent event = new SecurityEvent();
            event.setId(UUID.randomUUID().toString());
            event.setUserId(userId);
            event.setEventType(eventType);
            event.setSeverity(severity);
            event.setStatus("OPEN");
            event.setSource("RECOVERY");
            event.setDetectedAt(now);
            event.setCreatedAt(now);
            event.setUpdatedAt(now);
            event.setVersion(1);
            securityEventRepo.save(event);
        } catch (Exception ignored) {}
    }
}
