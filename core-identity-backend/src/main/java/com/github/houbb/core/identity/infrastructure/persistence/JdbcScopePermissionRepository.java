package com.github.houbb.core.identity.infrastructure.persistence;

import com.github.houbb.core.identity.application.domain.ScopePermission;
import com.github.houbb.core.identity.application.port.ScopePermissionRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Repository
public class JdbcScopePermissionRepository implements ScopePermissionRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcScopePermissionRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void save(ScopePermission mapping) {
        jdbcTemplate.update(
                "INSERT OR IGNORE INTO identity_scope_permission (id, scope_id, permission_id, created_at) " +
                "VALUES (?, ?, ?, ?)",
                mapping.getId(), mapping.getScopeId(), mapping.getPermissionId(),
                mapping.getCreatedAt()
        );
    }

    @Override
    public void saveAll(List<ScopePermission> mappings) {
        for (ScopePermission m : mappings) {
            save(m);
        }
    }

    @Override
    public List<ScopePermission> findByScopeId(String scopeId) {
        return jdbcTemplate.query(
                "SELECT * FROM identity_scope_permission WHERE scope_id = ? ORDER BY permission_id",
                new ScopePermissionRowMapper(), scopeId);
    }

    @Override
    public List<ScopePermission> findByPermissionId(String permissionId) {
        return jdbcTemplate.query(
                "SELECT * FROM identity_scope_permission WHERE permission_id = ? ORDER BY scope_id",
                new ScopePermissionRowMapper(), permissionId);
    }

    @Override
    public List<ScopePermission> findByScopeIds(List<String> scopeIds) {
        if (scopeIds == null || scopeIds.isEmpty()) {
            return new ArrayList<>();
        }
        StringBuilder sql = new StringBuilder("SELECT * FROM identity_scope_permission WHERE scope_id IN (");
        List<Object> params = new ArrayList<>();
        for (int i = 0; i < scopeIds.size(); i++) {
            if (i > 0) sql.append(", ");
            sql.append("?");
            params.add(scopeIds.get(i));
        }
        sql.append(") ORDER BY scope_id, permission_id");
        return jdbcTemplate.query(sql.toString(), new ScopePermissionRowMapper(), params.toArray());
    }

    @Override
    public void deleteByScopeId(String scopeId) {
        jdbcTemplate.update("DELETE FROM identity_scope_permission WHERE scope_id = ?", scopeId);
    }

    @Override
    public void deleteByScopeIdAndPermissionId(String scopeId, String permissionId) {
        jdbcTemplate.update(
                "DELETE FROM identity_scope_permission WHERE scope_id = ? AND permission_id = ?",
                scopeId, permissionId);
    }

    static class ScopePermissionRowMapper implements RowMapper<ScopePermission> {
        @Override
        public ScopePermission mapRow(ResultSet rs, int rowNum) throws SQLException {
            ScopePermission sp = new ScopePermission();
            sp.setId(rs.getString("id"));
            sp.setScopeId(rs.getString("scope_id"));
            sp.setPermissionId(rs.getString("permission_id"));
            sp.setCreatedAt(rs.getLong("created_at"));
            return sp;
        }
    }
}