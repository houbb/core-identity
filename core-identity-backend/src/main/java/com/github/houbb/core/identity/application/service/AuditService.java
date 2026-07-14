package com.github.houbb.core.identity.application.service;

import com.github.houbb.core.identity.application.command.AuditCommand;

/**
 * Audit service.
 */
public interface AuditService {

    /**
     * Record an audit event.
     *
     * @return the audit event ID
     */
    String record(AuditCommand command);
}