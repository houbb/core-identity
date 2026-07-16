package com.github.houbb.core.identity.infrastructure.persistence;

import com.github.houbb.core.identity.application.domain.ExternalIdentity;
import com.github.houbb.core.identity.application.port.ExternalIdentityRepository;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Repository
public class JdbcExternalIdentityRepository implements ExternalIdentityRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcExternalIdentityRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void save(ExternalIdentity e) {
        jdbcTemplate.update(
                "INSERT INTO identity_external_identity (id, user_id, organization_id, connection_id, " +
                "external_subject, external_username, external_email, external_employee_id, status, " +
                "management_source, claims_snapshot_json, first_login_at, last_login_at, " +
                "linked_at, unlinked_at, created_at, updated_at, version) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                e.getId(), e.getUserId(), e.getOrganizationId(), e.getConnectionId(),
                e.getExternalSubject(), e.getExternalUsername(), e.getExternalEmail(), e.getExternalEmployeeId(),
                e.getStatus(), e.getManagementSource(), e.getClaimsSnapshotJson(),
                e.getFirstLoginAt(), e.getLastLoginAt(), e.getLinkedAt(), e.getUnlinkedAt(),
                e.getCreatedAt(), e.getUpdatedAt(), e.getVersion()
        );
    }

    @Override
    public Optional<ExternalIdentity> findById(String id) {
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(
                    "SELECT * FROM identity_external_identity WHERE id = ?", new EiRowMapper(), id));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<ExternalIdentity> findByConnectionIdAndExternalSubject(String connectionId, String externalSubject) {
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(
                    "SELECT * FROM identity_external_identity WHERE connection_id = ? AND external_subject = ?",
                    new EiRowMapper(), connectionId, externalSubject));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<ExternalIdentity> findByUserId(String userId) {
        return jdbcTemplate.query(
                "SELECT * FROM identity_external_identity WHERE user_id = ? ORDER BY created_at DESC",
                new EiRowMapper(), userId);
    }

    @Override
    public List<ExternalIdentity> findByOrganizationId(String organizationId) {
        return jdbcTemplate.query(
                "SELECT * FROM identity_external_identity WHERE organization_id = ? ORDER BY created_at DESC",
                new EiRowMapper(), organizationId);
    }

    @Override
    public List<ExternalIdentity> findByConnectionId(String connectionId) {
        return jdbcTemplate.query(
                "SELECT * FROM identity_external_identity WHERE connection_id = ? ORDER BY created_at DESC",
                new EiRowMapper(), connectionId);
    }

    @Override
    public void update(ExternalIdentity e) {
        jdbcTemplate.update(
                "UPDATE identity_external_identity SET user_id = ?, external_username = ?, external_email = ?, " +
                "external_employee_id = ?, status = ?, management_source = ?, claims_snapshot_json = ?, " +
                "last_login_at = ?, updated_at = ?, version = version + 1 WHERE id = ? AND version = ?",
                e.getUserId(), e.getExternalUsername(), e.getExternalEmail(), e.getExternalEmployeeId(),
                e.getStatus(), e.getManagementSource(), e.getClaimsSnapshotJson(),
                e.getLastLoginAt(), e.getUpdatedAt(), e.getId(), e.getVersion()
        );
    }

    @Override
    public void updateStatus(String id, String status, long now, long version) {
        jdbcTemplate.update(
                "UPDATE identity_external_identity SET status = ?, updated_at = ?, version = version + 1 " +
                "WHERE id = ? AND version = ?", status, now, id, version);
    }

    @Override
    public void updateLastLogin(String id, long lastLoginAt, long now, long version) {
        jdbcTemplate.update(
                "UPDATE identity_external_identity SET last_login_at = ?, updated_at = ?, version = version + 1 " +
                "WHERE id = ? AND version = ?", lastLoginAt, now, id, version);
    }

    @Override
    public void unlink(String id, long unlinkedAt, long now, long version) {
        jdbcTemplate.update(
                "UPDATE identity_external_identity SET status = 'UNLINKED', unlinked_at = ?, " +
                "updated_at = ?, version = version + 1 WHERE id = ? AND version = ?",
                unlinkedAt, now, id, version);
    }

    static class EiRowMapper implements RowMapper<ExternalIdentity> {
        @Override
        public ExternalIdentity mapRow(ResultSet rs, int rowNum) throws SQLException {
            ExternalIdentity e = new ExternalIdentity();
            e.setId(rs.getString("id"));
            e.setUserId(getStringOrNull(rs, "user_id"));
            e.setOrganizationId(rs.getString("organization_id"));
            e.setConnectionId(rs.getString("connection_id"));
            e.setExternalSubject(rs.getString("external_subject"));
            e.setExternalUsername(getStringOrNull(rs, "external_username"));
            e.setExternalEmail(getStringOrNull(rs, "external_email"));
            e.setExternalEmployeeId(getStringOrNull(rs, "external_employee_id"));
            e.setStatus(rs.getString("status"));
            e.setManagementSource(getStringOrNull(rs, "management_source"));
            e.setClaimsSnapshotJson(getStringOrNull(rs, "claims_snapshot_json"));
            e.setFirstLoginAt(getLongOrNull(rs, "first_login_at"));
            e.setLastLoginAt(getLongOrNull(rs, "last_login_at"));
            e.setLinkedAt(getLongOrNull(rs, "linked_at"));
            e.setUnlinkedAt(getLongOrNull(rs, "unlinked_at"));
            e.setCreatedAt(rs.getLong("created_at"));
            e.setUpdatedAt(rs.getLong("updated_at"));
            e.setVersion(rs.getLong("version"));
            return e;
        }
    }

    private static String getStringOrNull(ResultSet rs, String column) throws SQLException {
        try { return rs.getString(column); } catch (SQLException e) { return null; }
    }

    private static Long getLongOrNull(ResultSet rs, String column) throws SQLException {
        try { long v = rs.getLong(column); return rs.wasNull() ? null : v; } catch (SQLException e) { return null; }
    }
}
