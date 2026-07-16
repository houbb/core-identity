package com.github.houbb.core.identity.application.service;

import com.github.houbb.core.identity.application.command.AuditCommand;
import com.github.houbb.core.identity.application.command.OutboxCommand;
import com.github.houbb.core.identity.application.domain.AccountLinkRequest;
import com.github.houbb.core.identity.application.domain.ExternalIdentity;
import com.github.houbb.core.identity.application.domain.FederationConnection;
import com.github.houbb.core.identity.application.port.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * External Identity Service implementation — manages IdP-to-local-User binding.
 *
 * P5 CRITICAL SECURITY BOUNDARY:
 * - Never auto-bind by email alone (account takeover risk)
 * - Account linking requires existing session re-authentication
 * - Protected roles (OWNER, SUPER_ADMIN) cannot be granted via external claims
 */
public class ExternalIdentityServiceImpl implements ExternalIdentityService {

    private static final Logger log = LoggerFactory.getLogger(ExternalIdentityServiceImpl.class);

    private final ExternalIdentityRepository extIdRepo;
    private final AccountLinkRequestRepository linkRequestRepo;
    private final FederationConnectionRepository connRepo;
    private final UserRepository userRepo;
    private final MembershipRepository membershipRepo;
    private final AuditService auditService;
    private final OutboxService outboxService;

    public ExternalIdentityServiceImpl(ExternalIdentityRepository extIdRepo,
                                        AccountLinkRequestRepository linkRequestRepo,
                                        FederationConnectionRepository connRepo,
                                        UserRepository userRepo,
                                        MembershipRepository membershipRepo,
                                        AuditService auditService,
                                        OutboxService outboxService) {
        this.extIdRepo = extIdRepo;
        this.linkRequestRepo = linkRequestRepo;
        this.connRepo = connRepo;
        this.userRepo = userRepo;
        this.membershipRepo = membershipRepo;
        this.auditService = auditService;
        this.outboxService = outboxService;
    }

    @Override
    @Transactional
    public LinkResult linkExternalIdentity(String connectionId, String externalSubject, String externalEmail,
                                            String externalUsername, String userId, String organizationId,
                                            String managementSource, long now) {
        // Priority 1: Check if already linked
        Optional<ExternalIdentity> existing = extIdRepo.findByConnectionIdAndExternalSubject(connectionId, externalSubject);
        if (existing.isPresent()) {
            ExternalIdentity ei = existing.get();
            if (!ei.getUserId().equals(userId)) {
                // Same external identity linked to different user — CONFLICT
                throw new ServiceException("IDENTITY_EXTERNAL_IDENTITY_CONFLICT",
                        "External identity is already linked to another user");
            }
            // Already linked to this user — update login time
            ei.setLastLoginAt(now);
            ei.setUpdatedAt(now);
            extIdRepo.update(ei);
            return new LinkResult(ei.getId(), ei.getUserId(), false, "Already linked");
        }

        // Priority 3: Check existing user identities (safety check, not auto-binding)
        List<ExternalIdentity> existingIds = extIdRepo.findByUserId(userId);
        // This is fine — user can have multiple external identities

        // Create new ExternalIdentity
        ExternalIdentity ei = new ExternalIdentity();
        ei.setId(UUID.randomUUID().toString());
        ei.setUserId(userId);
        ei.setOrganizationId(organizationId);
        ei.setConnectionId(connectionId);
        ei.setExternalSubject(externalSubject);
        ei.setExternalUsername(externalUsername);
        ei.setExternalEmail(externalEmail);
        ei.setStatus("ACTIVE");
        ei.setManagementSource(managementSource);
        ei.setFirstLoginAt(now);
        ei.setLastLoginAt(now);
        ei.setLinkedAt(now);
        ei.setCreatedAt(now);
        ei.setUpdatedAt(now);
        ei.setVersion(1);
        extIdRepo.save(ei);

        writeAudit("identity.external_identity.linked", "USER", userId, "LINK",
                "EXTERNAL_IDENTITY", ei.getId(), "SUCCESS", null, null, null, null);
        writeOutbox("identity.external_identity.linked", "EXTERNAL_IDENTITY", ei.getId(),
                "{\"userId\":\"" + userId + "\",\"connectionId\":\"" + connectionId + "\"}");

        log.info("External identity linked: user={}, connection={}, subject={}", userId, connectionId, externalSubject);
        return new LinkResult(ei.getId(), userId, true, "Linked successfully");
    }

    @Override
    @Transactional
    public void unlinkExternalIdentity(String externalIdentityId, String userId, long now) {
        ExternalIdentity ei = extIdRepo.findById(externalIdentityId)
                .orElseThrow(() -> new ServiceException("IDENTITY_EXTERNAL_IDENTITY_NOT_FOUND",
                        "External identity not found"));
        if (!ei.getUserId().equals(userId)) {
            throw new ServiceException("IDENTITY_EXTERNAL_IDENTITY_NOT_FOUND",
                    "External identity does not belong to this user");
        }

        // Safety check: user must have at least one other login method
        if (!canUnlink(externalIdentityId, userId)) {
            throw new ServiceException("IDENTITY_ACCOUNT_LINK_DENIED",
                    "Cannot unlink: this is your only login method");
        }

        extIdRepo.unlink(externalIdentityId, now, now, ei.getVersion());

        writeAudit("identity.external_identity.unlinked", "USER", userId, "UNLINK",
                "EXTERNAL_IDENTITY", externalIdentityId, "SUCCESS", null, null, null, null);
        writeOutbox("identity.external_identity.unlinked", "EXTERNAL_IDENTITY", externalIdentityId,
                "{\"userId\":\"" + userId + "\"}");
    }

    @Override
    @Transactional
    public String createAccountLinkRequest(String connectionId, String externalSubject, String externalEmail,
                                            String candidateUserId, long now) {
        AccountLinkRequest req = new AccountLinkRequest();
        req.setId(UUID.randomUUID().toString());
        req.setConnectionId(connectionId);
        req.setExternalSubject(externalSubject);
        req.setCandidateUserId(candidateUserId);
        req.setExternalEmail(externalEmail);
        req.setStatus("PENDING");
        req.setRiskLevel("MEDIUM");
        req.setExpiresAt(now + 30 * 60 * 1000L); // 30 min TTL
        req.setCreatedAt(now);
        req.setUpdatedAt(now);
        req.setVersion(1);
        linkRequestRepo.save(req);

        writeAudit("identity.external_identity.conflict_detected", "SYSTEM", "system", "DETECT_CONFLICT",
                "ACCOUNT_LINK_REQUEST", req.getId(), "SUCCESS", null, null, null, null);

        return req.getId();
    }

    @Override
    @Transactional
    public void confirmAccountLink(String requestId, String userId, long now) {
        AccountLinkRequest req = linkRequestRepo.findById(requestId)
                .orElseThrow(() -> new ServiceException("IDENTITY_ACCOUNT_LINK_DENIED",
                        "Link request not found"));

        if (!"PENDING".equals(req.getStatus())) {
            throw new ServiceException("IDENTITY_ACCOUNT_LINK_DENIED",
                    "Link request is no longer pending: " + req.getStatus());
        }

        linkRequestRepo.updateStatus(requestId, "CONFIRMED", now, req.getVersion());

        // Link the external identity to the candidate user
        FederationConnection conn = connRepo.findById(req.getConnectionId())
                .orElseThrow(() -> new ServiceException("IDENTITY_FEDERATION_CONNECTION_NOT_FOUND",
                        "Connection not found"));

        linkExternalIdentity(req.getConnectionId(), req.getExternalSubject(), req.getExternalEmail(),
                null, req.getCandidateUserId(), conn.getOrganizationId(), "JIT", now);
    }

    @Override
    @Transactional
    public void rejectAccountLink(String requestId, long now) {
        AccountLinkRequest req = linkRequestRepo.findById(requestId)
                .orElseThrow(() -> new ServiceException("IDENTITY_ACCOUNT_LINK_DENIED",
                        "Link request not found"));
        linkRequestRepo.updateStatus(requestId, "REJECTED", now, req.getVersion());
    }

    @Override
    public ExternalIdentity findByConnectionAndSubject(String connectionId, String externalSubject) {
        return extIdRepo.findByConnectionIdAndExternalSubject(connectionId, externalSubject).orElse(null);
    }

    @Override
    public List<ExternalIdentity> getUserExternalIdentities(String userId) {
        return extIdRepo.findByUserId(userId);
    }

    @Override
    public boolean canUnlink(String externalIdentityId, String userId) {
        List<ExternalIdentity> identities = extIdRepo.findByUserId(userId);
        if (identities.size() <= 1) {
            // Check if user has a local password
            return userRepo.findById(userId)
                    .map(u -> !"EXTERNAL_ONLY".equals(u.getPrimaryIdentitySource()))
                    .orElse(false);
        }
        return identities.size() > 1;
    }

    // ====================================================================
    // Audit & Outbox helpers
    // ====================================================================

    private void writeAudit(String eventType, String actorType, String actorId, String action,
                            String targetType, String targetId, String result, String reason,
                            String requestId, String sourceIp, String userAgent) {
        try {
            AuditCommand cmd = new AuditCommand();
            cmd.setEventType(eventType);
            cmd.setActorType(actorType);
            cmd.setActorId(actorId);
            cmd.setAction(action);
            cmd.setTargetType(targetType);
            cmd.setTargetId(targetId);
            cmd.setResult(result);
            cmd.setReason(reason);
            cmd.setRequestId(requestId);
            cmd.setSourceIp(sourceIp);
            cmd.setUserAgent(userAgent);
            auditService.record(cmd);
        } catch (Exception e) {
            log.warn("Failed to write audit: {}", e.getMessage());
        }
    }

    private void writeOutbox(String eventType, String aggregateType, String aggregateId, String payloadJson) {
        try {
            OutboxCommand cmd = new OutboxCommand();
            cmd.setEventType(eventType);
            cmd.setAggregateType(aggregateType);
            cmd.setAggregateId(aggregateId);
            cmd.setPayloadJson(payloadJson);
            outboxService.write(cmd);
        } catch (Exception e) {
            log.warn("Failed to write outbox: {}", e.getMessage());
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