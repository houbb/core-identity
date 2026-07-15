package com.github.houbb.core.identity.infrastructure.persistence;

import com.github.houbb.core.identity.application.domain.RolePermission;
import com.github.houbb.core.identity.application.port.RolePermissionRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Repository
public class JdbcRolePermissionRepository implements RolePermissionRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcRolePermissionRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void save(RolePermission rp) {
        jdbcTemplate.update(
                "INSERT INTO identity_role_permission (role_id, permission_id, granted_by, created_at) " +
                "VALUES (?, ?, ?, ?)",
                rp.getRoleId(), rp.getPermissionId(), rp.getGrantedBy(), rp.getCreatedAt()
        );
    }

    @Override
    public void deleteByRoleAndPermission(String roleId, String permissionId) {
        jdbcTemplate.update(
                "DELETE FROM identity_role_permission WHERE role_id = ? AND permission_id = ?",
                roleId, permissionId);
    }

    @Override
    public void deleteAllByRoleId(String roleId) {
        jdbcTemplate.update("DELETE FROM identity_role_permission WHERE role_id = ?", roleId);
    }

    @Override
    public List<RolePermission> findByRoleId(String roleId) {
        return jdbcTemplate.query(
                "SELECT * FROM identity_role_permission WHERE role_id = ?",
                new RolePermissionRowMapper(), roleId);
    }

    @Override
    public List<RolePermission> findByPermissionId(String permissionId) {
        return jdbcTemplate.query(
                "SELECT * FROM identity_role_permission WHERE permission_id = ?",
                new RolePermissionRowMapper(), permissionId);
    }

    @Override
    public List<String> findPermissionIdsByRoleId(String roleId) {
        return jdbcTemplate.queryForList(
                "SELECT permission_id FROM identity_role_permission WHERE role_id = ?",
                String.class, roleId);
    }

    @Override
    public List<String> findPermissionCodesByRoleIds(List<String> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) {
            return Collections.emptyList();
        }
        StringBuilder sql = new StringBuilder(
                "SELECT DISTINCT p.permission_code FROM identity_permission p " +
                "INNER JOIN identity_role_permission rp ON p.id = rp.permission_id " +
                "WHERE rp.role_id IN (");
        List<Object> params = new ArrayList<>();
        for (int i = 0; i < roleIds.size(); i++) {
            if (i > 0) sql.append(", ");
            sql.append("?");
            params.add(roleIds.get(i));
        }
        sql.append(")");
        return jdbcTemplate.queryForList(sql.toString(), String.class, params.toArray());
    }

    @Override
    @Transactional
    public void replacePermissions(String roleId, List<String> permissionIds, String grantedBy, long now) {
        jdbcTemplate.update("DELETE FROM identity_role_permission WHERE role_id = ?", roleId);
        for (String permissionId : permissionIds) {
            jdbcTemplate.update(
                    "INSERT INTO identity_role_permission (role_id, permission_id, granted_by, created_at) " +
                    "VALUES (?, ?, ?, ?)",
                    roleId, permissionId, grantedBy, now);
        }
    }

    static class RolePermissionRowMapper implements RowMapper<RolePermission> {
        @Override
        public RolePermission mapRow(ResultSet rs, int rowNum) throws SQLException {
            RolePermission rp = new RolePermission();
            rp.setRoleId(rs.getString("role_id"));
            rp.setPermissionId(rs.getString("permission_id"));
            rp.setGrantedBy(rs.getString("granted_by"));
            rp.setCreatedAt(rs.getLong("created_at"));
            return rp;
        }
    }
}