package com.github.houbb.core.identity.infrastructure.persistence;

import com.github.houbb.core.identity.application.domain.FederationConnection;
import com.github.houbb.core.identity.application.port.FederationConnectionRepository;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Repository
public class JdbcFederationConnectionRepository implements FederationConnectionRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcFederationConnectionRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void save(FederationConnection c) {
        jdbcTemplate.update(
                "INSERT INTO identity_federation_connection (id, connection_key, organization_id, connection_type, " +
                "name, status, login_button_text, logo_object_id, priority, jit_enabled, scim_enabled, " +
                "last_success_at, last_failure_at, last_error_code, created_by, created_at, updated_at, version) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                c.getId(), c.getConnectionKey(), c.getOrganizationId(), c.getConnectionType(),
                c.getName(), c.getStatus(), c.getLoginButtonText(), c.getLogoObjectId(),
                c.getPriority(), c.getJitEnabled(), c.getScimEnabled(),
                c.getLastSuccessAt(), c.getLastFailureAt(), c.getLastErrorCode(), c.getCreatedBy(),
                c.getCreatedAt(), c.getUpdatedAt(), c.getVersion()
        );
    }

    @Override
    public Optional<FederationConnection> findById(String id) {
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(
                    "SELECT * FROM identity_federation_connection WHERE id = ?", new FcRowMapper(), id));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<FederationConnection> findByConnectionKey(String connectionKey) {
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(
                    "SELECT * FROM identity_federation_connection WHERE connection_key = ?", new FcRowMapper(), connectionKey));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<FederationConnection> findByOrganizationId(String organizationId) {
        return jdbcTemplate.query(
                "SELECT * FROM identity_federation_connection WHERE organization_id = ? ORDER BY priority DESC, created_at ASC",
                new FcRowMapper(), organizationId);
    }

    @Override
    public List<FederationConnection> findByOrganizationIdAndStatus(String organizationId, String status) {
        return jdbcTemplate.query(
                "SELECT * FROM identity_federation_connection WHERE organization_id = ? AND status = ? ORDER BY priority DESC",
                new FcRowMapper(), organizationId, status);
    }

    @Override
    public List<FederationConnection> findByStatus(String status) {
        return jdbcTemplate.query(
                "SELECT * FROM identity_federation_connection WHERE status = ? ORDER BY created_at ASC",
                new FcRowMapper(), status);
    }

    @Override
    public void update(FederationConnection c) {
        jdbcTemplate.update(
                "UPDATE identity_federation_connection SET name = ?, status = ?, login_button_text = ?, " +
                "logo_object_id = ?, priority = ?, jit_enabled = ?, scim_enabled = ?, " +
                "last_success_at = ?, last_failure_at = ?, last_error_code = ?, " +
                "updated_at = ?, version = version + 1 WHERE id = ? AND version = ?",
                c.getName(), c.getStatus(), c.getLoginButtonText(), c.getLogoObjectId(),
                c.getPriority(), c.getJitEnabled(), c.getScimEnabled(),
                c.getLastSuccessAt(), c.getLastFailureAt(), c.getLastErrorCode(),
                c.getUpdatedAt(), c.getId(), c.getVersion()
        );
    }

    @Override
    public void updateStatus(String id, String status, long lastFailureAt, String lastErrorCode, long now, long version) {
        jdbcTemplate.update(
                "UPDATE identity_federation_connection SET status = ?, last_failure_at = ?, last_error_code = ?, " +
                "updated_at = ?, version = version + 1 WHERE id = ? AND version = ?",
                status, lastFailureAt, lastErrorCode, now, id, version);
    }

    @Override
    public void updateLastSuccess(String id, long lastSuccessAt, long now, long version) {
        jdbcTemplate.update(
                "UPDATE identity_federation_connection SET last_success_at = ?, updated_at = ?, " +
                "version = version + 1 WHERE id = ? AND version = ?",
                lastSuccessAt, now, id, version);
    }

    static class FcRowMapper implements RowMapper<FederationConnection> {
        @Override
        public FederationConnection mapRow(ResultSet rs, int rowNum) throws SQLException {
            FederationConnection c = new FederationConnection();
            c.setId(rs.getString("id"));
            c.setConnectionKey(rs.getString("connection_key"));
            c.setOrganizationId(rs.getString("organization_id"));
            c.setConnectionType(rs.getString("connection_type"));
            c.setName(getStringOrNull(rs, "name"));
            c.setStatus(rs.getString("status"));
            c.setLoginButtonText(getStringOrNull(rs, "login_button_text"));
            c.setLogoObjectId(getStringOrNull(rs, "logo_object_id"));
            c.setPriority(rs.getInt("priority"));
            c.setJitEnabled(rs.getInt("jit_enabled"));
            c.setScimEnabled(rs.getInt("scim_enabled"));
            c.setLastSuccessAt(getLongOrNull(rs, "last_success_at"));
            c.setLastFailureAt(getLongOrNull(rs, "last_failure_at"));
            c.setLastErrorCode(getStringOrNull(rs, "last_error_code"));
            c.setCreatedBy(getStringOrNull(rs, "created_by"));
            c.setCreatedAt(rs.getLong("created_at"));
            c.setUpdatedAt(rs.getLong("updated_at"));
            c.setVersion(rs.getLong("version"));
            return c;
        }
    }

    private static String getStringOrNull(ResultSet rs, String column) throws SQLException {
        try { return rs.getString(column); } catch (SQLException e) { return null; }
    }

    private static Long getLongOrNull(ResultSet rs, String column) throws SQLException {
        try { long val = rs.getLong(column); return rs.wasNull() ? null : val; } catch (SQLException e) { return null; }
    }
}
