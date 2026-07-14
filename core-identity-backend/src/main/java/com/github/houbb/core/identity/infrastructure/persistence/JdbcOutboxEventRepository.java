package com.github.houbb.core.identity.infrastructure.persistence;

import com.github.houbb.core.identity.application.domain.OutboxEvent;
import com.github.houbb.core.identity.application.port.OutboxEventRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * JDBC implementation of OutboxEventRepository.
 */
@Repository
public class JdbcOutboxEventRepository implements OutboxEventRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcOutboxEventRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void save(OutboxEvent event) {
        jdbcTemplate.update(
                "INSERT INTO identity_outbox_event (id, event_type, event_version, aggregate_type, " +
                "aggregate_id, payload_json, headers_json, status, attempt_count, next_attempt_at, " +
                "published_at, last_error, created_at, updated_at, version) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                event.getId(), event.getEventType(), event.getEventVersion(),
                event.getAggregateType(), event.getAggregateId(), event.getPayloadJson(),
                event.getHeadersJson(), event.getStatus(), event.getAttemptCount(),
                event.getNextAttemptAt(), event.getPublishedAt(), event.getLastError(),
                event.getCreatedAt(), event.getUpdatedAt(), event.getVersion()
        );
    }

    @Override
    public List<OutboxEvent> findPendingEvents(int limit) {
        return jdbcTemplate.query(
                "SELECT * FROM identity_outbox_event WHERE status = 'PENDING' AND next_attempt_at <= ? ORDER BY created_at LIMIT ?",
                new OutboxEventRowMapper(),
                System.currentTimeMillis(), limit
        );
    }

    @Override
    public void updateStatus(String eventId, String status, String lastError) {
        jdbcTemplate.update(
                "UPDATE identity_outbox_event SET status = ?, last_error = ?, updated_at = ?, " +
                "published_at = CASE WHEN ? = 'PUBLISHED' THEN ? ELSE published_at END " +
                "WHERE id = ?",
                status, lastError, System.currentTimeMillis(),
                status, System.currentTimeMillis(), eventId
        );
    }

    @Override
    public void incrementAttempt(String eventId) {
        jdbcTemplate.update(
                "UPDATE identity_outbox_event SET attempt_count = attempt_count + 1, updated_at = ? WHERE id = ?",
                System.currentTimeMillis(), eventId
        );
    }

    static class OutboxEventRowMapper implements RowMapper<OutboxEvent> {
        @Override
        public OutboxEvent mapRow(ResultSet rs, int rowNum) throws SQLException {
            OutboxEvent e = new OutboxEvent();
            e.setId(rs.getString("id"));
            e.setEventType(rs.getString("event_type"));
            e.setEventVersion(rs.getInt("event_version"));
            e.setAggregateType(rs.getString("aggregate_type"));
            e.setAggregateId(rs.getString("aggregate_id"));
            e.setPayloadJson(rs.getString("payload_json"));
            e.setHeadersJson(rs.getString("headers_json"));
            e.setStatus(rs.getString("status"));
            e.setAttemptCount(rs.getInt("attempt_count"));
            e.setNextAttemptAt(rs.getObject("next_attempt_at", Long.class));
            e.setPublishedAt(rs.getObject("published_at", Long.class));
            e.setLastError(rs.getString("last_error"));
            e.setCreatedAt(rs.getLong("created_at"));
            e.setUpdatedAt(rs.getLong("updated_at"));
            e.setVersion(rs.getLong("version"));
            return e;
        }
    }
}