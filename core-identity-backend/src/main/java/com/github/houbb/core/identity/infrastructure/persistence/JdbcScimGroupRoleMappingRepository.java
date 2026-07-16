package com.github.houbb.core.identity.infrastructure.persistence;

import com.github.houbb.core.identity.application.domain.ScimGroupRoleMapping;
import com.github.houbb.core.identity.application.port.ScimGroupRoleMappingRepository;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Repository
public class JdbcScimGroupRoleMappingRepository implements ScimGroupRoleMappingRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcScimGroupRoleMappingRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void save(ScimGroupRoleMapping m) {
        jdbcTemplate.update(
                "INSERT INTO identity_scim_group_role_mapping (id, group_id, role_id, mapping_mode, " +
                "status, created_by, created_at, updated_at, version) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
                m.getId(), m.getGroupId(), m.getRoleId(), m.getMappingMode(),
                m.getStatus(), m.getCreatedBy(), m.getCreatedAt(), m.getUpdatedAt(), m.getVersion());
    }

    @Override
    public Optional<ScimGroupRoleMapping> findById(String id) {
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(
                    "SELECT * FROM identity_scim_group_role_mapping WHERE id = ?", new SgrmRowMapper(), id));
        } catch (EmptyResultDataAccessException e) { return Optional.empty(); }
    }

    @Override
    public List<ScimGroupRoleMapping> findByGroupId(String groupId) {
        return jdbcTemplate.query(
                "SELECT * FROM identity_scim_group_role_mapping WHERE group_id = ?", new SgrmRowMapper(), groupId);
    }

    @Override
    public List<ScimGroupRoleMapping> findByRoleId(String roleId) {
        return jdbcTemplate.query(
                "SELECT * FROM identity_scim_group_role_mapping WHERE role_id = ?", new SgrmRowMapper(), roleId);
    }

    @Override
    public void update(ScimGroupRoleMapping m) {
        jdbcTemplate.update(
                "UPDATE identity_scim_group_role_mapping SET mapping_mode = ?, status = ?, " +
                "updated_at = ?, version = version + 1 WHERE id = ? AND version = ?",
                m.getMappingMode(), m.getStatus(), m.getUpdatedAt(), m.getId(), m.getVersion());
    }

    @Override
    public void deleteById(String id) {
        jdbcTemplate.update("DELETE FROM identity_scim_group_role_mapping WHERE id = ?", id);
    }

    static class SgrmRowMapper implements RowMapper<ScimGroupRoleMapping> {
        @Override
        public ScimGroupRoleMapping mapRow(ResultSet rs, int rowNum) throws SQLException {
            ScimGroupRoleMapping m = new ScimGroupRoleMapping();
            m.setId(rs.getString("id"));
            m.setGroupId(rs.getString("group_id"));
            m.setRoleId(rs.getString("role_id"));
            m.setMappingMode(rs.getString("mapping_mode"));
            m.setStatus(rs.getString("status"));
            m.setCreatedBy(getStr(rs, "created_by"));
            m.setCreatedAt(rs.getLong("created_at"));
            m.setUpdatedAt(rs.getLong("updated_at"));
            m.setVersion(rs.getLong("version"));
            return m;
        }
    }

    private static String getStr(ResultSet rs, String c) throws SQLException {
        try { return rs.getString(c); } catch (SQLException e) { return null; }
    }
}