package com.github.houbb.core.identity.infrastructure.persistence;

import com.github.houbb.core.identity.application.domain.ScimGroup;
import com.github.houbb.core.identity.application.port.ScimGroupRepository;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Repository
public class JdbcScimGroupRepository implements ScimGroupRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcScimGroupRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void save(ScimGroup g) {
        jdbcTemplate.update(
                "INSERT INTO identity_scim_group (id, connection_id, scim_resource_id, external_id, " +
                "display_name, status, created_at, updated_at, version) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
                g.getId(), g.getConnectionId(), g.getScimResourceId(), g.getExternalId(),
                g.getDisplayName(), g.getStatus(), g.getCreatedAt(), g.getUpdatedAt(), g.getVersion());
    }

    @Override
    public Optional<ScimGroup> findById(String id) {
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(
                    "SELECT * FROM identity_scim_group WHERE id = ?", new SgRowMapper(), id));
        } catch (EmptyResultDataAccessException e) { return Optional.empty(); }
    }

    @Override
    public Optional<ScimGroup> findByConnectionIdAndExternalId(String connectionId, String externalId) {
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(
                    "SELECT * FROM identity_scim_group WHERE connection_id = ? AND external_id = ?",
                    new SgRowMapper(), connectionId, externalId));
        } catch (EmptyResultDataAccessException e) { return Optional.empty(); }
    }

    @Override
    public List<ScimGroup> findByConnectionId(String connectionId) {
        return jdbcTemplate.query("SELECT * FROM identity_scim_group WHERE connection_id = ? ORDER BY display_name ASC",
                new SgRowMapper(), connectionId);
    }

    @Override
    public void update(ScimGroup g) {
        jdbcTemplate.update(
                "UPDATE identity_scim_group SET display_name = ?, status = ?, updated_at = ?, " +
                "version = version + 1 WHERE id = ? AND version = ?",
                g.getDisplayName(), g.getStatus(), g.getUpdatedAt(), g.getId(), g.getVersion());
    }

    @Override
    public void updateStatus(String id, String status, long now, long version) {
        jdbcTemplate.update(
                "UPDATE identity_scim_group SET status = ?, updated_at = ?, version = version + 1 " +
                "WHERE id = ? AND version = ?", status, now, id, version);
    }

    static class SgRowMapper implements RowMapper<ScimGroup> {
        @Override
        public ScimGroup mapRow(ResultSet rs, int rowNum) throws SQLException {
            ScimGroup g = new ScimGroup();
            g.setId(rs.getString("id"));
            g.setConnectionId(rs.getString("connection_id"));
            g.setScimResourceId(getStr(rs, "scim_resource_id"));
            g.setExternalId(getStr(rs, "external_id"));
            g.setDisplayName(rs.getString("display_name"));
            g.setStatus(rs.getString("status"));
            g.setCreatedAt(rs.getLong("created_at"));
            g.setUpdatedAt(rs.getLong("updated_at"));
            g.setVersion(rs.getLong("version"));
            return g;
        }
    }

    private static String getStr(ResultSet rs, String c) throws SQLException {
        try { return rs.getString(c); } catch (SQLException e) { return null; }
    }
}