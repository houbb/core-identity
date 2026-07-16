package com.github.houbb.core.identity.infrastructure.persistence;

import com.github.houbb.core.identity.application.domain.AccountLinkRequest;
import com.github.houbb.core.identity.application.port.AccountLinkRequestRepository;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Repository
public class JdbcAccountLinkRequestRepository implements AccountLinkRequestRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcAccountLinkRequestRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void save(AccountLinkRequest request) {
        jdbcTemplate.update(
                "INSERT INTO identity_account_link_request (id, connection_id, external_subject, " +
                "candidate_user_id, external_email, status, risk_level, verification_method, " +
                "expires_at, confirmed_at, rejected_at, created_at, updated_at, version) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                request.getId(), request.getConnectionId(), request.getExternalSubject(),
                request.getCandidateUserId(), request.getExternalEmail(), request.getStatus(),
                request.getRiskLevel(), request.getVerificationMethod(),
                request.getExpiresAt(), request.getConfirmedAt(), request.getRejectedAt(),
                request.getCreatedAt(), request.getUpdatedAt(), request.getVersion()
        );
    }

    @Override
    public Optional<AccountLinkRequest> findById(String id) {
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(
                    "SELECT * FROM identity_account_link_request WHERE id = ?",
                    new AccountLinkRequestRowMapper(), id));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<AccountLinkRequest> findByConnectionIdAndStatus(String connectionId, String status) {
        return jdbcTemplate.query(
                "SELECT * FROM identity_account_link_request WHERE connection_id = ? AND status = ? " +
                "ORDER BY created_at DESC",
                new AccountLinkRequestRowMapper(), connectionId, status);
    }

    @Override
    public List<AccountLinkRequest> findByStatus(String status) {
        return jdbcTemplate.query(
                "SELECT * FROM identity_account_link_request WHERE status = ? ORDER BY created_at DESC",
                new AccountLinkRequestRowMapper(), status);
    }

    @Override
    public void update(AccountLinkRequest request) {
        jdbcTemplate.update(
                "UPDATE identity_account_link_request SET connection_id = ?, external_subject = ?, " +
                "candidate_user_id = ?, external_email = ?, status = ?, risk_level = ?, " +
                "verification_method = ?, expires_at = ?, confirmed_at = ?, rejected_at = ?, " +
                "updated_at = ?, version = version + 1 WHERE id = ? AND version = ?",
                request.getConnectionId(), request.getExternalSubject(),
                request.getCandidateUserId(), request.getExternalEmail(), request.getStatus(),
                request.getRiskLevel(), request.getVerificationMethod(),
                request.getExpiresAt(), request.getConfirmedAt(), request.getRejectedAt(),
                request.getUpdatedAt(), request.getId(), request.getVersion()
        );
    }

    @Override
    public void updateStatus(String id, String status, long now, long version) {
        jdbcTemplate.update(
                "UPDATE identity_account_link_request SET status = ?, updated_at = ?, " +
                "version = version + 1 WHERE id = ? AND version = ?",
                status, now, id, version);
    }

    static class AccountLinkRequestRowMapper implements RowMapper<AccountLinkRequest> {
        @Override
        public AccountLinkRequest mapRow(ResultSet rs, int rowNum) throws SQLException {
            AccountLinkRequest r = new AccountLinkRequest();
            r.setId(rs.getString("id"));
            r.setConnectionId(rs.getString("connection_id"));
            r.setExternalSubject(rs.getString("external_subject"));
            r.setCandidateUserId(rs.getString("candidate_user_id"));
            r.setExternalEmail(rs.getString("external_email"));
            r.setStatus(rs.getString("status"));
            r.setRiskLevel(rs.getString("risk_level"));
            r.setVerificationMethod(rs.getString("verification_method"));
            r.setExpiresAt(getNullableLong(rs, "expires_at"));
            r.setConfirmedAt(getNullableLong(rs, "confirmed_at"));
            r.setRejectedAt(getNullableLong(rs, "rejected_at"));
            r.setCreatedAt(rs.getLong("created_at"));
            r.setUpdatedAt(rs.getLong("updated_at"));
            r.setVersion(rs.getLong("version"));
            return r;
        }

        private static Long getNullableLong(ResultSet rs, String column) throws SQLException {
            long val = rs.getLong(column);
            if (rs.wasNull()) return null;
            return val;
        }
    }
}