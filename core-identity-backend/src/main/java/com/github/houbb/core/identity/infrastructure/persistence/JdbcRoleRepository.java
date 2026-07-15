package com.github.houbb.core.identity.infrastructure.persistence;

import com.github.houbb.core.identity.application.domain.Role;
import com.github.houbb.core.identity.application.port.RoleRepository;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

@Repository
public class JdbcRoleRepository implements RoleRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcRoleRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void save(Role role) {
        jdbcTemplate.update(
                "INSERT INTO identity_role (id, organization_id, role_key, name, description, role_type, " +
                "status, system_protected, sort_order, created_by, created_at, updated_at, version) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                role.getId(), role.getOrganizationId(), role.getRoleKey(), role.getName(),
                role.getDescription(), role.getRoleType(), role.getStatus(), role.getSystemProtected(),
                role.getSortOrder(), role.getCreatedBy(), role.getCreatedAt(), role.getUpdatedAt(), role.getVersion()
        );
    }

    @Override
    public Optional<Role> findById(String id) {
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(
                    "SELECT * FROM identity_role WHERE id = ?", new RoleRowMapper(), id));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<Role> findByOrgAndKey(String organizationId, String roleKey) {
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(
                    "SELECT * FROM identity_role WHERE organization_id = ? AND role_key = ?",
                    new RoleRowMapper(), organizationId, roleKey));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<Role> findByOrgId(String organizationId) {
        return jdbcTemplate.query(
                "SELECT * FROM identity_role WHERE organization_id = ? ORDER BY sort_order, created_at",
                new RoleRowMapper(), organizationId);
    }

    @Override
    public List<Role> findByIds(List<String> ids) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyList();
        }
        StringBuilder sql = new StringBuilder("SELECT * FROM identity_role WHERE id IN (");
        List<Object> params = new ArrayList<>();
        for (int i = 0; i < ids.size(); i++) {
            if (i > 0) sql.append(", ");
            sql.append("?");
            params.add(ids.get(i));
        }
        sql.append(")");
        return jdbcTemplate.query(sql.toString(), new RoleRowMapper(), params.toArray());
    }

    @Override
    public boolean existsByNameInOrg(String organizationId, String name, String excludeId) {
        StringBuilder sql = new StringBuilder(
                "SELECT COUNT(*) FROM identity_role WHERE organization_id = ? AND name = ?");
        List<Object> params = new ArrayList<>();
        params.add(organizationId);
        params.add(name);
        if (excludeId != null) {
            sql.append(" AND id != ?");
            params.add(excludeId);
        }
        Integer count = jdbcTemplate.queryForObject(sql.toString(), Integer.class, params.toArray());
        return count != null && count > 0;
    }

    @Override
    public void update(Role role) {
        jdbcTemplate.update(
                "UPDATE identity_role SET name = ?, description = ?, role_type = ?, status = ?, " +
                "sort_order = ?, updated_at = ?, version = version + 1 " +
                "WHERE id = ? AND version = ?",
                role.getName(), role.getDescription(), role.getRoleType(), role.getStatus(),
                role.getSortOrder(), role.getUpdatedAt(), role.getId(), role.getVersion()
        );
    }

    @Override
    public void deleteById(String id, long version) {
        jdbcTemplate.update(
                "DELETE FROM identity_role WHERE id = ? AND version = ?", id, version);
    }

    @Override
    public int countMembersByRoleId(String roleId) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM identity_membership_role WHERE role_id = ?",
                Integer.class, roleId);
        return count != null ? count : 0;
    }

    static class RoleRowMapper implements RowMapper<Role> {
        @Override
        public Role mapRow(ResultSet rs, int rowNum) throws SQLException {
            Role r = new Role();
            r.setId(rs.getString("id"));
            r.setOrganizationId(rs.getString("organization_id"));
            r.setRoleKey(rs.getString("role_key"));
            r.setName(rs.getString("name"));
            r.setDescription(rs.getString("description"));
            r.setRoleType(rs.getString("role_type"));
            r.setStatus(rs.getString("status"));
            r.setSystemProtected(rs.getInt("system_protected"));
            r.setSortOrder(rs.getInt("sort_order"));
            r.setCreatedBy(rs.getString("created_by"));
            r.setCreatedAt(rs.getLong("created_at"));
            r.setUpdatedAt(rs.getLong("updated_at"));
            r.setVersion(rs.getLong("version"));
            return r;
        }
    }
}