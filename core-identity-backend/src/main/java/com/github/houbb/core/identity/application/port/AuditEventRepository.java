package com.github.houbb.core.identity.application.port;

import com.github.houbb.core.identity.application.domain.AuditEvent;

/**
 * Repository for audit events.
 */
public interface AuditEventRepository {

    void save(AuditEvent event);
}