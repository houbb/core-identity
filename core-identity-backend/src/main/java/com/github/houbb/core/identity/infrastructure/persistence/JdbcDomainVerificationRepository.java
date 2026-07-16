package com.github.houbb.core.identity.infrastructure.persistence;

import com.github.houbb.core.identity.application.domain.DomainVerification;
import com.github.houbb.core.identity.application.port.DomainVerificationRepository;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

@Repository
public class JdbcDomainVerificationRepository implements DomainVerificationRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcDomainVerificationRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void save(DomainVerification verification) {
        jdbcTemplate.update(
                "INSERT INTO identity_domain_verification (id, domain_id, challenge_hash, expected_record_name, " +
                "method, status, attempt_count, expires_at, verified_at, created_at, updated_at, version) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                verification.getId(), verification.getDomainId(), verification.getChallengeHash(),
                verification.getExpectedRecordName(), verification.getMethod(), verification.getStatus(),
                verification.getAttemptCount(), verification.getExpiresAt(), verification.getVerifiedAt(),
                verification.getCreatedAt(), verification.getUpdatedAt(), verification.getVersion()
        );
    }

    @Override
    public Optional<DomainVerification> findById(String id) {
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(
                    "SELECT * FROM identity_domain_verification WHERE id = ?",
                    new DomainVerificationRowMapper(), id));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<DomainVerification> findByDomainId(String domainId) {
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(
                    "SELECT * FROM identity_domain_verification WHERE domain_id = ?",
                    new DomainVerificationRowMapper(), domainId));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public void update(DomainVerification verification) {
        jdbcTemplate.update(
                "UPDATE identity_domain_verification SET domain_id = ?, challenge_hash = ?, " +
                "expected_record_name = ?, method = ?, status = ?, attempt_count = ?, " +
                "expires_at = ?, verified_at = ?, updated_at = ?, " +
                "version = version + 1 WHERE id = ? AND version = ?",
                verification.getDomainId(), verification.getChallengeHash(),
                verification.getExpectedRecordName(), verification.getMethod(),
                verification.getStatus(), verification.getAttemptCount(),
                verification.getExpiresAt(), verification.getVerifiedAt(),
                verification.getUpdatedAt(), verification.getId(), verification.getVersion()
        );
    }

    @Override
    public void updateStatus(String id, String status, long now, long version) {
        jdbcTemplate.update(
                "UPDATE identity_domain_verification SET status = ?, updated_at = ?, version = version + 1 " +
                "WHERE id = ? AND version = ?",
                status, now, id, version);
    }

    static class DomainVerificationRowMapper implements RowMapper<DomainVerification> {
        @Override
        public DomainVerification mapRow(ResultSet rs, int rowNum) throws SQLException {
            DomainVerification v = new DomainVerification();
            v.setId(rs.getString("id"));
            v.setDomainId(rs.getString("domain_id"));
            v.setChallengeHash(rs.getString("challenge_hash"));
            v.setExpectedRecordName(rs.getString("expected_record_name"));
            v.setMethod(rs.getString("method"));
            v.setStatus(rs.getString("status"));
            v.setAttemptCount(rs.getInt("attempt_count"));
            v.setExpiresAt(getLongOrNull(rs, "expires_at"));
            v.setVerifiedAt(getLongOrNull(rs, "verified_at"));
            v.setCreatedAt(rs.getLong("created_at"));
            v.setUpdatedAt(rs.getLong("updated_at"));
            v.setVersion(rs.getLong("version"));
            return v;
        }
    }

    private static Long getLongOrNull(ResultSet rs, String column) throws SQLException {
        try {
            long val = rs.getLong(column);
            return rs.wasNull() ? null : val;
        } catch (SQLException e) {
            return null;
        }
    }
}