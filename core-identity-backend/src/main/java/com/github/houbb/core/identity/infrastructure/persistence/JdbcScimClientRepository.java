package com.github.houbb.core.identity.infrastructure.persistence;

import com.github.houbb.core.identity.application.domain.ScimClient;
import com.github.houbb.core.identity.application.port.ScimClientRepository;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Repository
public class JdbcScimClientRepository implements ScimClientRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcScimClientRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void save(ScimClient c) {
        jdbcTemplate.update(
                "INSERT INTO identity_scim_client (id, organization_id, connection_id, name, token_prefix, " +
                "token_hash, scopes_json, status, expires_at, ip_allowlist_json, last_used_at, last_used_ip, " +
                "created_at, updated_at, version) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                c.getId(), c.getOrganizationId(), c.getConnectionId(), c.getName(), c.getTokenPrefix(),
                c.getTokenHash(), c.getScopesJson(), c.getStatus(), c.getExpiresAt(), c.getIpAllowlistJson(),
                c.getLastUsedAt(), c.getLastUsedIp(), c.getCreatedAt(), c.getUpdatedAt(), c.getVersion()
        );
    }

    @Override
    public Optional<ScimClient> findById(String id) {
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(
                    "SELECT * FROM identity_scim_client WHERE id = ?", new ScRowMapper(), id));
        } catch (EmptyResultDataAccessException e) { return Optional.empty(); }
    }

    @Override
    public Optional<ScimClient> findByTokenPrefix(String tokenPrefix) {
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(
                    "SELECT * FROM identity_scim_client WHERE token_prefix = ? AND status = 'ACTIVE'",
                    new ScRowMapper(), tokenPrefix));
        } catch (EmptyResultDataAccessException e) { return Optional.empty(); }
    }

    @Override
    public List<ScimClient> findByOrganizationId(String organizationId) {
        return jdbcTemplate.query("SELECT * FROM identity_scim_client WHERE organization_id = ? ORDER BY created_at DESC",
                new ScRowMapper(), organizationId);
    }

    @Override
    public List<ScimClient> findByConnectionId(String connectionId) {
        return jdbcTemplate.query("SELECT * FROM identity_scim_client WHERE connection_id = ? ORDER BY created_at DESC",
                new ScRowMapper(), connectionId);
    }

    @Override
    public void update(ScimClient c) {
        jdbcTemplate.update(
                "UPDATE identity_scim_client SET name = ?, status = ?, scopes_json = ?, " +
                "updated_at = ?, version = version + 1 WHERE id = ? AND version = ?",
                c.getName(), c.getStatus(), c.getScopesJson(), c.getUpdatedAt(), c.getId(), c.getVersion());
    }

    @Override
    public void updateLastUsed(String id, long lastUsedAt, String lastUsedIp, long now, long version) {
        jdbcTemplate.update(
                "UPDATE identity_scim_client SET last_used_at = ?, last_used_ip = ?, updated_at = ?, " +
                "version = version + 1 WHERE id = ? AND version = ?",
                lastUsedAt, lastUsedIp, now, id, version);
    }

    static class ScRowMapper implements RowMapper<ScimClient> {
        @Override
        public ScimClient mapRow(ResultSet rs, int rowNum) throws SQLException {
            ScimClient c = new ScimClient();
            c.setId(rs.getString("id"));
            c.setOrganizationId(rs.getString("organization_id"));
            c.setConnectionId(rs.getString("connection_id"));
            c.setName(getStr(rs, "name"));
            c.setTokenPrefix(rs.getString("token_prefix"));
            c.setTokenHash(rs.getString("token_hash"));
            c.setScopesJson(getStr(rs, "scopes_json"));
            c.setStatus(rs.getString("status"));
            c.setExpiresAt(getLong(rs, "expires_at"));
            c.setIpAllowlistJson(getStr(rs, "ip_allowlist_json"));
            c.setLastUsedAt(getLong(rs, "last_used_at"));
            c.setLastUsedIp(getStr(rs, "last_used_ip"));
            c.setCreatedAt(rs.getLong("created_at"));
            c.setUpdatedAt(rs.getLong("updated_at"));
            c.setVersion(rs.getLong("version"));
            return c;
        }
    }

    private static String getStr(ResultSet rs, String c) throws SQLException {
        try { return rs.getString(c); } catch (SQLException e) { return null; }
    }

    private static Long getLong(ResultSet rs, String c) throws SQLException {
        try { long v = rs.getLong(c); return rs.wasNull() ? null : v; } catch (SQLException e) { return null; }
    }
}