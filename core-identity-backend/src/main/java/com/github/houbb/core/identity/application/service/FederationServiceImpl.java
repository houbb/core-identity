package com.github.houbb.core.identity.application.service;

import com.github.houbb.core.identity.application.command.AuditCommand;
import com.github.houbb.core.identity.application.command.OutboxCommand;
import com.github.houbb.core.identity.application.domain.FederationConnection;
import com.github.houbb.core.identity.application.domain.VerifiedDomain;
import com.github.houbb.core.identity.application.domain.DomainVerification;
import com.github.houbb.core.identity.application.port.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.List;
import java.util.UUID;

/**
 * Federation Service implementation — core P5 orchestration.
 */
public class FederationServiceImpl implements FederationService {

    private static final Logger log = LoggerFactory.getLogger(FederationServiceImpl.class);

    private final FederationConnectionRepository connRepo;
    private final VerifiedDomainRepository domainRepo;
    private final DomainVerificationRepository verificationRepo;
    private final OrganizationRepository orgRepo;
    private final AuditService auditService;
    private final OutboxService outboxService;

    public FederationServiceImpl(FederationConnectionRepository connRepo,
                                 VerifiedDomainRepository domainRepo,
                                 DomainVerificationRepository verificationRepo,
                                 OrganizationRepository orgRepo,
                                 AuditService auditService,
                                 OutboxService outboxService) {
        this.connRepo = connRepo;
        this.domainRepo = domainRepo;
        this.verificationRepo = verificationRepo;
        this.orgRepo = orgRepo;
        this.auditService = auditService;
        this.outboxService = outboxService;
    }

    // ====================================================================
    // Domain Verification
    // ====================================================================

    @Override
    @Transactional
    public DomainVerificationResult initiateDomainVerification(String organizationId, String domainName,
                                                                String createdBy, String requestId) {
        // Check domain uniqueness
        domainRepo.findByDomainName(domainName).ifPresent(existing -> {
            if (!"CONFLICT".equals(existing.getStatus()) && !existing.getOrganizationId().equals(organizationId)) {
                throw new FederationException("IDENTITY_DOMAIN_CONFLICT",
                        "Domain " + domainName + " is already claimed by another organization");
            }
        });

        long now = System.currentTimeMillis();

        // Create or reuse VerifiedDomain record
        VerifiedDomain domain = new VerifiedDomain();
        domain.setId(UUID.randomUUID().toString());
        domain.setOrganizationId(organizationId);
        domain.setDomainName(domainName);
        domain.setStatus("PENDING");
        domain.setVerificationMethod("DNS_TXT");
        domain.setCreatedBy(createdBy);
        domain.setCreatedAt(now);
        domain.setUpdatedAt(now);
        domain.setVersion(1);
        domainRepo.save(domain);

        // Generate challenge
        String challenge = generateChallenge();
        String challengeHash = sha256(challenge);
        String expectedRecord = "_core-identity-verification." + domainName;

        DomainVerification verification = new DomainVerification();
        verification.setId(UUID.randomUUID().toString());
        verification.setDomainId(domain.getId());
        verification.setChallengeHash(challengeHash);
        verification.setExpectedRecordName(expectedRecord);
        verification.setMethod("DNS_TXT");
        verification.setStatus("PENDING");
        verification.setAttemptCount(0);
        verification.setExpiresAt(now + 24 * 60 * 60 * 1000L); // 24h TTL
        verification.setCreatedAt(now);
        verification.setUpdatedAt(now);
        verification.setVersion(1);
        verificationRepo.save(verification);

        writeAudit("identity.domain.verification_started", "USER", createdBy, "INITIATE",
                "DOMAIN", domain.getId(), "SUCCESS", null, requestId, null, null);
        writeOutbox("identity.domain.verification_started", "DOMAIN", domain.getId(),
                "{\"organizationId\":\"" + organizationId + "\",\"domainName\":\"" + domainName + "\"}");

        log.info("Domain verification initiated: org={}, domain={}, record={}, value=core-identity-verification={}",
                organizationId, domainName, expectedRecord, challenge);

        return new DomainVerificationResult(domain.getId(), "PENDING",
                "Add DNS TXT record: " + expectedRecord + " with value: core-identity-verification=" + challenge);
    }

    @Override
    @Transactional
    public DomainVerificationResult checkDomainVerification(String organizationId, String domainId, String requestId) {
        VerifiedDomain domain = domainRepo.findById(domainId)
                .orElseThrow(() -> new FederationException("IDENTITY_DOMAIN_NOT_VERIFIED", "Domain not found"));
        if (!domain.getOrganizationId().equals(organizationId)) {
            throw new FederationException("IDENTITY_DOMAIN_NOT_VERIFIED", "Domain does not belong to this organization");
        }

        DomainVerification verification = verificationRepo.findByDomainId(domainId)
                .orElseThrow(() -> new FederationException("IDENTITY_DOMAIN_NOT_VERIFIED", "Verification not found"));

        long now = System.currentTimeMillis();

        // DNS check (stub for now — real DNS lookup via JNDI)
        boolean dnsVerified = checkDnsRecord(verification.getExpectedRecordName(), verification.getChallengeHash());

        if (dnsVerified) {
            domain.setStatus("VERIFIED");
            domain.setVerifiedAt(now);
            domain.setLastCheckedAt(now);
            domain.setUpdatedAt(now);
            domainRepo.update(domain);

            verification.setStatus("VERIFIED");
            verification.setVerifiedAt(now);
            verification.setUpdatedAt(now);
            verificationRepo.update(verification);

            writeAudit("identity.domain.verified", "USER", "system", "VERIFY",
                    "DOMAIN", domainId, "SUCCESS", "DNS verified", requestId, null, null);
            writeOutbox("identity.domain.verified", "DOMAIN", domainId,
                    "{\"domainName\":\"" + domain.getDomainName() + "\",\"organizationId\":\"" + organizationId + "\"}");

            return new DomainVerificationResult(domainId, "VERIFIED", "Domain successfully verified");
        }

        verification.setAttemptCount(verification.getAttemptCount() + 1);
        verification.setUpdatedAt(now);
        verificationRepo.update(verification);

        return new DomainVerificationResult(domainId, "PENDING",
                "DNS record not found. Please ensure the TXT record is correctly configured.");
    }

    @Override
    public void verifyDomainDns(String domainId) {
        // Direct DNS verification — used for scheduled re-checks
    }

    // ====================================================================
    // Federation Connection Management
    // ====================================================================

    @Override
    @Transactional
    public String createConnection(String organizationId, String connectionType, String name,
                                    String createdBy, String requestId) {
        long now = System.currentTimeMillis();
        String connectionKey = generateConnectionKey(organizationId, connectionType);

        FederationConnection conn = new FederationConnection();
        conn.setId(UUID.randomUUID().toString());
        conn.setConnectionKey(connectionKey);
        conn.setOrganizationId(organizationId);
        conn.setConnectionType(connectionType);
        conn.setName(name);
        conn.setStatus("DRAFT");
        conn.setLoginButtonText(extractLoginText(name, connectionType));
        conn.setPriority(0);
        conn.setJitEnabled(0);
        conn.setScimEnabled(0);
        conn.setCreatedBy(createdBy);
        conn.setCreatedAt(now);
        conn.setUpdatedAt(now);
        conn.setVersion(1);
        connRepo.save(conn);

        writeAudit("identity.federation.connection_created", "USER", createdBy, "CREATE",
                "FEDERATION_CONNECTION", conn.getId(), "SUCCESS", null, requestId, null, null);
        writeOutbox("identity.federation.connection_created", "FEDERATION_CONNECTION", conn.getId(),
                "{\"organizationId\":\"" + organizationId + "\",\"connectionType\":\"" + connectionType + "\"}");

        log.info("Federation connection created: id={}, key={}, type={}, org={}",
                conn.getId(), connectionKey, connectionType, organizationId);

        return conn.getId();
    }

    @Override
    @Transactional
    public void updateConnection(String organizationId, String connectionId, String name,
                                  String loginButtonText, long now) {
        FederationConnection conn = getAndValidateConnection(organizationId, connectionId);
        conn.setName(name);
        conn.setLoginButtonText(loginButtonText);
        conn.setUpdatedAt(now);
        connRepo.update(conn);
    }

    @Override
    @Transactional
    public void updateConnectionStatus(String organizationId, String connectionId, String status,
                                        String errorCode, String requestId) {
        FederationConnection conn = getAndValidateConnection(organizationId, connectionId);
        long now = System.currentTimeMillis();
        connRepo.updateStatus(connectionId, status, now, errorCode, now, conn.getVersion());

        writeAudit("identity.federation.connection_" + status.toLowerCase(), "SYSTEM", "system",
                "STATUS_CHANGE", "FEDERATION_CONNECTION", connectionId,
                "SUCCESS", errorCode, requestId, null, null);
    }

    @Override
    @Transactional
    public void activateConnection(String organizationId, String connectionId, String requestId) {
        FederationConnection conn = getAndValidateConnection(organizationId, connectionId);
        long now = System.currentTimeMillis();
        connRepo.updateStatus(connectionId, "ACTIVE", conn.getLastFailureAt(), conn.getLastErrorCode(),
                now, conn.getVersion());

        writeAudit("identity.federation.connection_activated", "SYSTEM", "system",
                "ACTIVATE", "FEDERATION_CONNECTION", connectionId, "SUCCESS", null, requestId, null, null);
        writeOutbox("identity.federation.connection_activated", "FEDERATION_CONNECTION", connectionId,
                "{\"organizationId\":\"" + organizationId + "\"}");
    }

    @Override
    @Transactional
    public void suspendConnection(String organizationId, String connectionId, String requestId) {
        FederationConnection conn = getAndValidateConnection(organizationId, connectionId);
        long now = System.currentTimeMillis();
        connRepo.updateStatus(connectionId, "SUSPENDED", conn.getLastFailureAt(), conn.getLastErrorCode(),
                now, conn.getVersion());

        writeAudit("identity.federation.connection_suspended", "SYSTEM", "system",
                "SUSPEND", "FEDERATION_CONNECTION", connectionId, "SUCCESS", null, requestId, null, null);
        writeOutbox("identity.federation.connection_suspended", "FEDERATION_CONNECTION", connectionId,
                "{\"organizationId\":\"" + organizationId + "\"}");
    }

    // ====================================================================
    // Identity Discovery
    // ====================================================================

    @Override
    public IdentityDiscoveryResult discoverIdentityByEmail(String email) {
        String domain = extractDomain(email);
        if (domain == null) {
            return new IdentityDiscoveryResult("LOCAL", null, null, null);
        }

        VerifiedDomain verifiedDomain = domainRepo.findByDomainName(domain).orElse(null);
        if (verifiedDomain == null || !"VERIFIED".equals(verifiedDomain.getStatus())) {
            return new IdentityDiscoveryResult("LOCAL", null, null, null);
        }

        List<FederationConnection> connections = connRepo.findByOrganizationIdAndStatus(
                verifiedDomain.getOrganizationId(), "ACTIVE");

        if (connections.isEmpty()) {
            return new IdentityDiscoveryResult("LOCAL", null, null, null);
        }

        // Return first ACTIVE connection (or highest priority)
        FederationConnection primary = connections.get(0);
        orgRepo.findById(verifiedDomain.getOrganizationId()).ifPresent(org ->
            log.debug("Identity discovery: {} -> {}, connection={}", email, org.getName(), primary.getConnectionKey())
        );

        String orgName = orgRepo.findById(verifiedDomain.getOrganizationId())
                .map(org -> org.getName()).orElse(null);

        return new IdentityDiscoveryResult("ENTERPRISE_SSO", orgName, primary.getConnectionKey(),
                verifiedDomain.getOrganizationId());
    }

    @Override
    public String getConnectionLoginRedirect(String connectionKey) {
        FederationConnection conn = connRepo.findByConnectionKey(connectionKey)
                .orElseThrow(() -> new FederationException("IDENTITY_FEDERATION_CONNECTION_NOT_FOUND",
                        "Connection not found: " + connectionKey));
        if (!"ACTIVE".equals(conn.getStatus())) {
            throw new FederationException("IDENTITY_FEDERATION_CONNECTION_NOT_ACTIVE",
                    "Connection is not active: " + conn.getStatus());
        }
        return "/api/v1/identity/federation/" + connectionKey + "/login";
    }

    @Override
    public String getOrganizationSsoRedirect(String organizationSlug) {
        orgRepo.findBySlug(organizationSlug)
                .orElseThrow(() -> new FederationException("IDENTITY_ORGANIZATION_NOT_FOUND",
                        "Organization not found: " + organizationSlug));
        List<FederationConnection> connections = connRepo.findByOrganizationIdAndStatus(
                orgRepo.findBySlug(organizationSlug).get().getId(), "ACTIVE");
        if (connections.isEmpty()) {
            throw new FederationException("IDENTITY_FEDERATION_CONNECTION_NOT_ACTIVE",
                    "No active SSO connections for this organization");
        }
        return "/api/v1/identity/federation/" + connections.get(0).getConnectionKey() + "/login";
    }

    // ====================================================================
    // Private helpers
    // ====================================================================

    private FederationConnection getAndValidateConnection(String organizationId, String connectionId) {
        FederationConnection conn = connRepo.findById(connectionId)
                .orElseThrow(() -> new FederationException("IDENTITY_FEDERATION_CONNECTION_NOT_FOUND",
                        "Connection not found: " + connectionId));
        if (!conn.getOrganizationId().equals(organizationId)) {
            throw new FederationException("IDENTITY_FEDERATION_CONNECTION_NOT_FOUND",
                    "Connection does not belong to this organization");
        }
        return conn;
    }

    private String generateConnectionKey(String organizationId, String connectionType) {
        return organizationId.substring(0, Math.min(8, organizationId.length())) + "-"
                + UUID.randomUUID().toString().substring(0, 8);
    }

    private String generateChallenge() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    private String sha256(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(input.getBytes("UTF-8"));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("SHA-256 failed", e);
        }
    }

    private String extractLoginText(String name, String connectionType) {
        if (name != null && !name.isEmpty()) return "使用 " + name + " 登录";
        return "使用企业 " + connectionType + " 账号登录";
    }

    private String extractDomain(String email) {
        if (email == null || !email.contains("@")) return null;
        return email.substring(email.indexOf('@') + 1).toLowerCase().trim();
    }

    /**
     * Stub DNS TXT record check. Replace with JNDI DNS lookup in production.
     */
    private boolean checkDnsRecord(String recordName, String challengeHash) {
        // Production: use javax.naming.directory.InitialDirContext to lookup TXT records
        // For now: return false (will require actual DNS configuration in test environments)
        log.debug("DNS check stub: record={}, hash={}", recordName, challengeHash);
        return false;
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

    // ====================================================================
    // Federation Exception
    // ====================================================================

    public static class FederationException extends RuntimeException {
        private final String errorCode;

        public FederationException(String errorCode, String message) {
            super(message);
            this.errorCode = errorCode;
        }

        public String getErrorCode() { return errorCode; }
    }
}
