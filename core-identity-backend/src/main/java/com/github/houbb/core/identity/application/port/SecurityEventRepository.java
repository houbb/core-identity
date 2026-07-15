package com.github.houbb.core.identity.application.port;

import com.github.houbb.core.identity.application.domain.SecurityEvent;

import java.util.List;

/**
 * Repository for identity_security_event.
 */
public interface SecurityEventRepository {

    void save(SecurityEvent event);

    List<SecurityEvent> findByUserId(String userId, int limit);

    List<SecurityEvent> findByOrganizationId(String organizationId, int limit);

    List<SecurityEvent> findRecentHighSeverity(int limit);
}
