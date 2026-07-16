package com.github.houbb.core.identity.infrastructure.persistence;

import com.github.houbb.core.identity.application.domain.ScimResource;
import com.github.houbb.core.identity.application.port.ScimResourceRepository;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Repository
public class JdbcScimResourceRepository implements ScimResourceRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcScimResourceRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void save(ScimResource r) {
        jdbcTemplate.update(
                "INSERT INTO identity_scim_resource (id, connection_id, resource_type, external_id, " +
                "local_resource_id, user_name, active, resource_version, last_payload_hash, last_synced_at, " +
                "created_at, updated_at, version) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                r.getId(), r.getConnectionId(), r.getResourceType(), r.getExternalId(),
                r.getLocalResourceId(), r.getUserName(), r.getActive(), r.getResourceVersion(),
                r.getLastPayloadHash(), r.getLastSyncedAt(), r.getCreatedAt(), r.getUpdatedAt(), r.getVersion()
        );
    }

    @Override
    public Optional<ScimResource> findById(String id) {
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(
                    "SELECT * FROM identity_scim_resource WHERE id = ?", new SrRowMapper(), id));
        } catch (EmptyResultDataAccessException e) { return Optional.empty(); }
    }

    @Override
    public Optional<ScimResource> findByConnectionIdAndResourceTypeAndExternalId(
            String connectionId, String resourceType, String externalId) {
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(
                    "SELECT * FROM identity_scim_resource WHERE connection_id = ? AND resource_type = ? AND external_id = ?",
                    new SrRowMapper(), connectionId, resourceType, externalId));
        } catch (EmptyResultDataAccessException e) { return Optional.empty(); }
    }

    @Override
    public Optional<ScimResource> findByConnectionIdAndResourceTypeAndLocalResourceId(
            String connectionId, String resourceType, String localResourceId) {
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(
                    "SELECT * FROM identity_scim_resource WHERE connection_id = ? AND resource_type = ? AND local_resource_id = ?",
                    new SrRowMapper(), connectionId, resourceType, localResourceId));
        } catch (EmptyResultDataAccessException e) { return Optional.empty(); }
    }

    @Override
    public List<ScimResource> findByConnectionIdAndResourceType(String connectionId, String resourceType) {
        return jdbcTemplate.query(
                "SELECT * FROM identity_scim_resource WHERE connection_id = ? AND resource_type = ? ORDER BY created_at ASC",
                new SrRowMapper(), connectionId, resourceType);
    }

    @Override
    public void update(ScimResource r) {
        jdbcTemplate.update(
                "UPDATE identity_scim_resource SET user_name = ?, active = ?, resource_version = ?, " +
                "last_payload_hash = ?, last_synced_at = ?, updated_at = ?, version = version + 1 " +
                "WHERE id = ? AND version = ?",
                r.getUserName(), r.getActive(), r.getResourceVersion(), r.getLastPayloadHash(),
                r.getLastSyncedAt(), r.getUpdatedAt(), r.getId(), r.getVersion());
    }

    @Override
    public void updateActive(String id, int active, long resourceVersion, long lastSyncedAt, long now, long version) {
        jdbcTemplate.update(
                "UPDATE identity_scim_resource SET active = ?, resource_version = ?, last_synced_at = ?, " +
                "updated_at = ?, version = version + 1 WHERE id = ? AND version = ?",
                active, resourceVersion, lastSyncedAt, now, id, version);
    }

    static class SrRowMapper implements RowMapper<ScimResource> {
        @Override
        public ScimResource mapRow(ResultSet rs, int rowNum) throws SQLException {
            ScimResource r = new ScimResource();
            r.setId(rs.getString("id"));
            r.setConnectionId(rs.getString("connection_id"));
            r.setResourceType(rs.getString("resource_type"));
            r.setExternalId(rs.getString("external_id"));
            r.setLocalResourceId(getStr(rs, "local_resource_id"));
            r.setUserName(getStr(rs, "user_name"));
            r.setActive(rs.getInt("active"));
            r.setResourceVersion(rs.getLong("resource_version"));
            r.setLastPayloadHash(getStr(rs, "last_payload_hash"));
            r.setLastSyncedAt(getLong(rs, "last_synced_at"));
            r.setCreatedAt(rs.getLong("created_at"));
            r.setUpdatedAt(rs.getLong("updated_at"));
            r.setVersion(rs.getLong("version"));
            return r;
        }
    }

    private static String getStr(ResultSet rs, String c) throws SQLException {
        try { return rs.getString(c); } catch (SQLException e) { return null; }
    }
    private static Long getLong(ResultSet rs, String c) throws SQLException {
        try { long v = rs.getLong(c); return rs.wasNull() ? null : v; } catch (SQLException e) { return null; }
    }
}