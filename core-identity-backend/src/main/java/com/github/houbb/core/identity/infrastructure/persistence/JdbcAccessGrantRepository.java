package com.github.houbb.core.identity.infrastructure.persistence;

import com.github.houbb.core.identity.application.domain.AccessGrant;
import com.github.houbb.core.identity.application.port.AccessGrantRepository;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

@Repository
public class JdbcAccessGrantRepository implements AccessGrantRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcAccessGrantRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void save(AccessGrant grant) {
        jdbcTemplate.update(
                "INSERT INTO identity_access_grant (id, subject_type, subject_id, organization_id, " +
                "entitlement_id, source_type, source_id, grant_type, status, " +
                "valid_from, expires_at, granted_by, last_used_at, " +
                "created_at, updated_at, version) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                grant.getId(), grant.getSubjectType(), grant.getSubjectId(),
                grant.getOrganizationId(), grant.getEntitlementId(), grant.getSourceType(),
                grant.getSourceId(), grant.getGrantType(), grant.getStatus(),
                grant.getValidFrom(), grant.getExpiresAt(), grant.getGrantedBy(),
                grant.getLastUsedAt(), grant.getCreatedAt(), grant.getUpdatedAt(), grant.getVersion()
        );
    }

    @Override
    public Optional<AccessGrant> findById(String id) {
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(
                    "SELECT * FROM identity_access_grant WHERE id = ?",
                    new AccessGrantRowMapper(), id));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<AccessGrant> findBySubjectId(String subjectId) {
        return jdbcTemplate.query(
                "SELECT * FROM identity_access_grant WHERE subject_id = ? ORDER BY created_at DESC",
                new AccessGrantRowMapper(), subjectId);
    }

    @Override
    public List<AccessGrant> findBySubjectIdAndOrg(String subjectId, String organizationId) {
        return jdbcTemplate.query(
                "SELECT * FROM identity_access_grant WHERE subject_id = ? AND organization_id = ? ORDER BY created_at DESC",
                new AccessGrantRowMapper(), subjectId, organizationId);
    }

    @Override
    public List<AccessGrant> findByEntitlementId(String entitlementId) {
        return jdbcTemplate.query(
                "SELECT * FROM identity_access_grant WHERE entitlement_id = ? ORDER BY created_at DESC",
                new AccessGrantRowMapper(), entitlementId);
    }

    @Override
    public List<AccessGrant> findActiveBySubjectId(String subjectId) {
        return jdbcTemplate.query(
                "SELECT * FROM identity_access_grant WHERE subject_id = ? AND status = 'ACTIVE' " +
                "ORDER BY created_at DESC",
                new AccessGrantRowMapper(), subjectId);
    }

    @Override
    public List<AccessGrant> findExpiringGrants(long beforeTimestamp, String status) {
        return jdbcTemplate.query(
                "SELECT * FROM identity_access_grant WHERE expires_at <= ? AND status = ? " +
                "ORDER BY expires_at ASC",
                new AccessGrantRowMapper(), beforeTimestamp, status);
    }

    @Override
    public List<AccessGrant> findBySourceTypeAndSourceId(String sourceType, String sourceId) {
        return jdbcTemplate.query(
                "SELECT * FROM identity_access_grant WHERE source_type = ? AND source_id = ?",
                new AccessGrantRowMapper(), sourceType, sourceId);
    }

    @Override
    public void update(AccessGrant grant) {
        jdbcTemplate.update(
                "UPDATE identity_access_grant SET grant_type = ?, status = ?, " +
                "expires_at = ?, last_used_at = ?, " +
                "updated_at = ?, version = version + 1 WHERE id = ? AND version = ?",
                grant.getGrantType(), grant.getStatus(),
                grant.getExpiresAt(), grant.getLastUsedAt(),
                grant.getUpdatedAt(), grant.getId(), grant.getVersion()
        );
    }

    @Override
    public void updateStatus(String id, String status, long now, long version) {
        jdbcTemplate.update(
                "UPDATE identity_access_grant SET status = ?, updated_at = ?, version = version + 1 " +
                "WHERE id = ? AND version = ?",
                status, now, id, version);
    }

    @Override
    public void updateStatusAndExpiry(String id, String status, long expiresAt, long now, long version) {
        jdbcTemplate.update(
                "UPDATE identity_access_grant SET status = ?, expires_at = ?, " +
                "updated_at = ?, version = version + 1 WHERE id = ? AND version = ?",
                status, expiresAt, now, id, version);
    }

    @Override
    public void revoke(String id, String revokedBy, long revokedAt, String reason, long now, long version) {
        jdbcTemplate.update(
                "UPDATE identity_access_grant SET status = 'REVOKED', revoked_by = ?, " +
                "revoked_at = ?, revoke_reason = ?, updated_at = ?, version = version + 1 " +
                "WHERE id = ? AND version = ?",
                revokedBy, revokedAt, reason, now, id, version);
    }

    @Override
    public int countActiveBySubjectId(String subjectId) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM identity_access_grant WHERE subject_id = ? AND status = 'ACTIVE'",
                Integer.class, subjectId);
        return count != null ? count : 0;
    }

    static class AccessGrantRowMapper implements RowMapper<AccessGrant> {
        @Override
        public AccessGrant mapRow(ResultSet rs, int rowNum) throws SQLException {
            AccessGrant g = new AccessGrant();
            g.setId(rs.getString("id"));
            g.setSubjectType(rs.getString("subject_type"));
            g.setSubjectId(rs.getString("subject_id"));
            g.setOrganizationId(rs.getString("organization_id"));
            g.setEntitlementId(rs.getString("entitlement_id"));
            g.setSourceType(rs.getString("source_type"));
            g.setSourceId(rs.getString("source_id"));
            g.setGrantType(rs.getString("grant_type"));
            g.setStatus(rs.getString("status"));
            g.setValidFrom(rs.getLong("valid_from"));
            g.setExpiresAt(getLongOrNull(rs, "expires_at"));
            g.setGrantedBy(rs.getString("granted_by"));
            g.setRevokedBy(rs.getString("revoked_by"));
            g.setRevokedAt(getLongOrNull(rs, "revoked_at"));
            g.setRevokeReason(rs.getString("revoke_reason"));
            g.setLastUsedAt(getLongOrNull(rs, "last_used_at"));
            g.setCreatedAt(rs.getLong("created_at"));
            g.setUpdatedAt(rs.getLong("updated_at"));
            g.setVersion(rs.getLong("version"));
            return g;
        }

        private Long getLongOrNull(ResultSet rs, String column) throws SQLException {
            long val = rs.getLong(column);
            return rs.wasNull() ? null : val;
        }
    }
}
