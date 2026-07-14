package com.github.houbb.core.identity.infrastructure.persistence;

import com.github.houbb.core.identity.application.domain.IdempotencyRecord;
import com.github.houbb.core.identity.application.port.IdempotencyRecordRepository;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

/**
 * JDBC implementation of IdempotencyRecordRepository.
 */
@Repository
public class JdbcIdempotencyRecordRepository implements IdempotencyRecordRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcIdempotencyRecordRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void save(IdempotencyRecord record) {
        jdbcTemplate.update(
                "INSERT INTO identity_idempotency_record (id, idempotency_key, scope, request_hash, " +
                "status, response_status, response_body, locked_until, expires_at, created_at, updated_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                record.getId(), record.getIdempotencyKey(), record.getScope(), record.getRequestHash(),
                record.getStatus(), record.getResponseStatus(), record.getResponseBody(),
                record.getLockedUntil(), record.getExpiresAt(),
                record.getCreatedAt(), record.getUpdatedAt()
        );
    }

    @Override
    public Optional<IdempotencyRecord> findByKey(String scope, String idempotencyKey) {
        try {
            IdempotencyRecord record = jdbcTemplate.queryForObject(
                    "SELECT * FROM identity_idempotency_record WHERE scope = ? AND idempotency_key = ?",
                    new IdempotencyRecordRowMapper(), scope, idempotencyKey
            );
            return Optional.ofNullable(record);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public void updateStatus(String id, String status, Integer responseStatus, String responseBody) {
        jdbcTemplate.update(
                "UPDATE identity_idempotency_record SET status = ?, response_status = ?, " +
                "response_body = ?, updated_at = ? WHERE id = ?",
                status, responseStatus, responseBody, System.currentTimeMillis(), id
        );
    }

    @Override
    public void deleteExpired(long beforeTimestamp) {
        jdbcTemplate.update(
                "DELETE FROM identity_idempotency_record WHERE expires_at < ?",
                beforeTimestamp
        );
    }

    static class IdempotencyRecordRowMapper implements RowMapper<IdempotencyRecord> {
        @Override
        public IdempotencyRecord mapRow(ResultSet rs, int rowNum) throws SQLException {
            IdempotencyRecord r = new IdempotencyRecord();
            r.setId(rs.getString("id"));
            r.setIdempotencyKey(rs.getString("idempotency_key"));
            r.setScope(rs.getString("scope"));
            r.setRequestHash(rs.getString("request_hash"));
            r.setStatus(rs.getString("status"));
            r.setResponseStatus(rs.getObject("response_status", Integer.class));
            r.setResponseBody(rs.getString("response_body"));
            r.setLockedUntil(rs.getObject("locked_until", Long.class));
            r.setExpiresAt(rs.getLong("expires_at"));
            r.setCreatedAt(rs.getLong("created_at"));
            r.setUpdatedAt(rs.getLong("updated_at"));
            return r;
        }
    }
}