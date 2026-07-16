package com.github.houbb.core.identity.application.service;

import com.github.houbb.core.identity.application.domain.ExternalIdentity;

import java.util.List;

/**
 * External Identity Service — manages the binding between external IdP subjects and local Core Identity users.
 *
 * P5: Critical security boundary. Account linking requires re-authentication. Never auto-bind by email alone.
 */
public interface ExternalIdentityService {

    record LinkResult(String externalIdentityId, String userId, boolean created, String message) {}

    // === Identity Binding ===
    LinkResult linkExternalIdentity(String connectionId, String externalSubject, String externalEmail,
                                    String externalUsername, String userId, String organizationId,
                                    String managementSource, long now);

    void unlinkExternalIdentity(String externalIdentityId, String userId, long now);

    // === Account Conflict Resolution ===
    String createAccountLinkRequest(String connectionId, String externalSubject, String externalEmail,
                                    String candidateUserId, long now);

    void confirmAccountLink(String requestId, String userId, long now);

    void rejectAccountLink(String requestId, long now);

    // === Lookups ===
    ExternalIdentity findByConnectionAndSubject(String connectionId, String externalSubject);

    List<ExternalIdentity> getUserExternalIdentities(String userId);

    boolean canUnlink(String externalIdentityId, String userId);
}
