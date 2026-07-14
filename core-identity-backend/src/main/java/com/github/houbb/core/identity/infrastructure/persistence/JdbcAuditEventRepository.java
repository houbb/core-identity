package com.github.houbb.core.identity.infrastructure.persistence;

import com.github.houbb.core.identity.application.domain.AuditEvent;
import com.github.houbb.core.identity.application.port.AuditEventRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

/**
 * JDBC implementation of AuditEventRepository.
 */
@Repository
public class JdbcAuditEventRepository implements AuditEventRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcAuditEventRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void save(AuditEvent event) {
        jdbcTemplate.update(
                "INSERT INTO identity_audit_event (id, event_type, actor_type, actor_id, action, " +
                "target_type, target_id, result, reason, request_id, source_service, source_ip, " +
                "user_agent, metadata_json, occurred_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                event.getId(), event.getEventType(), event.getActorType(), event.getActorId(),
                event.getAction(), event.getTargetType(), event.getTargetId(), event.getResult(),
                event.getReason(), event.getRequestId(), event.getSourceService(), event.getSourceIp(),
                event.getUserAgent(), event.getMetadataJson(), event.getOccurredAt()
        );
    }
}