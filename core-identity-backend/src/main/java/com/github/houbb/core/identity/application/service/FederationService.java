package com.github.houbb.core.identity.application.service;

/**
 * Federation Service — core P5 service for domain verification, identity provider connection management,
 * and identity discovery (Home Realm Discovery).
 *
 * This is the central orchestration service for all enterprise SSO operations.
 */
public interface FederationService {

    record DomainVerificationResult(String domainId, String status, String message) {}
    record IdentityDiscoveryResult(String loginType, String organizationName, String connectionKey, String organizationId) {}
    record ConnectionTestResult(boolean success, String message, String externalSubject, String email, java.util.List<String> groups) {}

    // === Domain Verification ===
    DomainVerificationResult initiateDomainVerification(String organizationId, String domainName, String createdBy, String requestId);
    DomainVerificationResult checkDomainVerification(String organizationId, String domainId, String requestId);
    void verifyDomainDns(String domainId);

    // === Federation Connection Management ===
    String createConnection(String organizationId, String connectionType, String name, String createdBy, String requestId);
    void updateConnection(String organizationId, String connectionId, String name, String loginButtonText, long now);
    void updateConnectionStatus(String organizationId, String connectionId, String status, String errorCode, String requestId);
    void activateConnection(String organizationId, String connectionId, String requestId);
    void suspendConnection(String organizationId, String connectionId, String requestId);

    // === Identity Discovery (Home Realm Discovery) ===
    IdentityDiscoveryResult discoverIdentityByEmail(String email);

    // === Federation Login Entry ===
    String getConnectionLoginRedirect(String connectionKey);
    String getOrganizationSsoRedirect(String organizationSlug);
}
