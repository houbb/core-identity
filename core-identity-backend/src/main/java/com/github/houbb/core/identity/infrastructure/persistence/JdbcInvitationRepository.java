package com.github.houbb.core.identity.infrastructure.persistence;

import com.github.houbb.core.identity.application.domain.Invitation;
import com.github.houbb.core.identity.application.port.InvitationRepository;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Repository
public class JdbcInvitationRepository implements InvitationRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcInvitationRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void save(Invitation invitation) {
        jdbcTemplate.update(
                "INSERT INTO identity_invitation (id, organization_id, email_normalized, email_display, " +
                "token_hash, status, invited_by_user_id, accepted_by_user_id, message, " +
                "expires_at, accepted_at, declined_at, revoked_at, created_at, updated_at, version) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                invitation.getId(), invitation.getOrganizationId(), invitation.getEmailNormalized(),
                invitation.getEmailDisplay(), invitation.getTokenHash(), invitation.getStatus(),
                invitation.getInvitedByUserId(), invitation.getAcceptedByUserId(), invitation.getMessage(),
                invitation.getExpiresAt(), invitation.getAcceptedAt(), invitation.getDeclinedAt(),
                invitation.getRevokedAt(), invitation.getCreatedAt(), invitation.getUpdatedAt(),
                invitation.getVersion()
        );
    }

    @Override
    public Optional<Invitation> findById(String id) {
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(
                    "SELECT * FROM identity_invitation WHERE id = ?", new InvitationRowMapper(), id));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<Invitation> findByTokenHash(String tokenHash) {
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(
                    "SELECT * FROM identity_invitation WHERE token_hash = ?", new InvitationRowMapper(), tokenHash));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<Invitation> findByOrgAndEmailAndPending(String organizationId, String emailNormalized) {
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(
                    "SELECT * FROM identity_invitation WHERE organization_id = ? AND email_normalized = ? AND status = 'PENDING'",
                    new InvitationRowMapper(), organizationId, emailNormalized));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<Invitation> findByOrgId(String organizationId) {
        return jdbcTemplate.query(
                "SELECT * FROM identity_invitation WHERE organization_id = ? ORDER BY created_at DESC",
                new InvitationRowMapper(), organizationId);
    }

    @Override
    public List<Invitation> findByOrgAndStatus(String organizationId, String status) {
        return jdbcTemplate.query(
                "SELECT * FROM identity_invitation WHERE organization_id = ? AND status = ? ORDER BY created_at DESC",
                new InvitationRowMapper(), organizationId, status);
    }

    @Override
    public void update(Invitation invitation) {
        jdbcTemplate.update(
                "UPDATE identity_invitation SET token_hash = ?, status = ?, accepted_by_user_id = ?, " +
                "message = ?, expires_at = ?, accepted_at = ?, declined_at = ?, revoked_at = ?, " +
                "updated_at = ?, version = version + 1 WHERE id = ? AND version = ?",
                invitation.getTokenHash(), invitation.getStatus(), invitation.getAcceptedByUserId(),
                invitation.getMessage(), invitation.getExpiresAt(), invitation.getAcceptedAt(),
                invitation.getDeclinedAt(), invitation.getRevokedAt(),
                invitation.getUpdatedAt(), invitation.getId(), invitation.getVersion()
        );
    }

    @Override
    public void updateStatus(String id, String status, Long actionAt, long now, long version) {
        jdbcTemplate.update(
                "UPDATE identity_invitation SET status = ?, updated_at = ?, version = version + 1 " +
                "WHERE id = ? AND version = ?",
                status, now, id, version);
    }

    static class InvitationRowMapper implements RowMapper<Invitation> {
        @Override
        public Invitation mapRow(ResultSet rs, int rowNum) throws SQLException {
            Invitation inv = new Invitation();
            inv.setId(rs.getString("id"));
            inv.setOrganizationId(rs.getString("organization_id"));
            inv.setEmailNormalized(rs.getString("email_normalized"));
            inv.setEmailDisplay(rs.getString("email_display"));
            inv.setTokenHash(rs.getString("token_hash"));
            inv.setStatus(rs.getString("status"));
            inv.setInvitedByUserId(rs.getString("invited_by_user_id"));
            inv.setAcceptedByUserId(rs.getString("accepted_by_user_id"));
            inv.setMessage(rs.getString("message"));
            inv.setExpiresAt(rs.getLong("expires_at"));
            setNullLong(rs, "accepted_at", inv::setAcceptedAt);
            setNullLong(rs, "declined_at", inv::setDeclinedAt);
            setNullLong(rs, "revoked_at", inv::setRevokedAt);
            inv.setCreatedAt(rs.getLong("created_at"));
            inv.setUpdatedAt(rs.getLong("updated_at"));
            inv.setVersion(rs.getLong("version"));
            return inv;
        }
    }

    @FunctionalInterface
    private interface LongSetter { void set(long val); }

    private static void setNullLong(ResultSet rs, String col, LongSetter setter) throws SQLException {
        long val = rs.getLong(col);
        if (!rs.wasNull()) setter.set(val);
    }
}