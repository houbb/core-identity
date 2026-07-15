package com.github.houbb.core.identity.infrastructure.persistence;

import com.github.houbb.core.identity.application.domain.Permission;
import com.github.houbb.core.identity.application.port.PermissionRepository;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class JdbcPermissionRepository implements PermissionRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcPermissionRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void save(Permission permission) {
        jdbcTemplate.update(
                "INSERT INTO identity_permission (id, permission_code, source_service, resource, action, " +
                "name, description, risk_level, assignable, status, source_version, " +
                "created_at, updated_at, version) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                permission.getId(), permission.getPermissionCode(), permission.getSourceService(),
                permission.getResource(), permission.getAction(), permission.getName(),
                permission.getDescription(), permission.getRiskLevel(), permission.getAssignable(),
                permission.getStatus(), permission.getSourceVersion(),
                permission.getCreatedAt(), permission.getUpdatedAt(), permission.getVersion()
        );
    }

    @Override
    public Optional<Permission> findById(String id) {
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(
                    "SELECT * FROM identity_permission WHERE id = ?", new PermissionRowMapper(), id));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<Permission> findByCode(String permissionCode) {
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(
                    "SELECT * FROM identity_permission WHERE permission_code = ?",
                    new PermissionRowMapper(), permissionCode));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<Permission> findByService(String sourceService) {
        return jdbcTemplate.query(
                "SELECT * FROM identity_permission WHERE source_service = ? ORDER BY permission_code",
                new PermissionRowMapper(), sourceService);
    }

    @Override
    public List<Permission> findByServices(List<String> services) {
        if (services == null || services.isEmpty()) {
            return new ArrayList<>();
        }
        StringBuilder sql = new StringBuilder(
                "SELECT * FROM identity_permission WHERE source_service IN (");
        List<Object> params = new ArrayList<>();
        for (int i = 0; i < services.size(); i++) {
            if (i > 0) sql.append(", ");
            sql.append("?");
            params.add(services.get(i));
        }
        sql.append(") ORDER BY source_service, permission_code");
        return jdbcTemplate.query(sql.toString(), new PermissionRowMapper(), params.toArray());
    }

    @Override
    public List<Permission> findAllAssignable() {
        return jdbcTemplate.query(
                "SELECT * FROM identity_permission WHERE assignable = 1 AND status = 'ACTIVE' ORDER BY permission_code",
                new PermissionRowMapper());
    }

    @Override
    public List<Permission> findAll() {
        return jdbcTemplate.query(
                "SELECT * FROM identity_permission ORDER BY source_service, permission_code",
                new PermissionRowMapper());
    }

    @Override
    public List<Permission> findAssignableByService(String service, String resource,
                                                     String riskLevel, String search) {
        StringBuilder sql = new StringBuilder(
                "SELECT * FROM identity_permission WHERE assignable = 1 AND status = 'ACTIVE'");
        List<Object> params = new ArrayList<>();

        if (service != null && !service.isEmpty()) {
            sql.append(" AND source_service = ?");
            params.add(service);
        }
        if (resource != null && !resource.isEmpty()) {
            sql.append(" AND resource = ?");
            params.add(resource);
        }
        if (riskLevel != null && !riskLevel.isEmpty()) {
            sql.append(" AND risk_level = ?");
            params.add(riskLevel);
        }
        if (search != null && !search.isEmpty()) {
            sql.append(" AND (name LIKE ? OR description LIKE ? OR permission_code LIKE ?)");
            String like = "%" + search + "%";
            params.add(like);
            params.add(like);
            params.add(like);
        }

        sql.append(" ORDER BY source_service, resource, action");
        return jdbcTemplate.query(sql.toString(), new PermissionRowMapper(), params.toArray());
    }

    @Override
    public void update(Permission permission) {
        jdbcTemplate.update(
                "UPDATE identity_permission SET name = ?, description = ?, risk_level = ?, " +
                "assignable = ?, status = ?, source_version = ?, " +
                "updated_at = ?, version = version + 1 WHERE id = ? AND version = ?",
                permission.getName(), permission.getDescription(), permission.getRiskLevel(),
                permission.getAssignable(), permission.getStatus(), permission.getSourceVersion(),
                permission.getUpdatedAt(), permission.getId(), permission.getVersion()
        );
    }

    @Override
    public void updateStatus(String id, String status, long now, long version) {
        jdbcTemplate.update(
                "UPDATE identity_permission SET status = ?, updated_at = ?, version = version + 1 " +
                "WHERE id = ? AND version = ?",
                status, now, id, version);
    }

    @Override
    public int countByService(String sourceService) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM identity_permission WHERE source_service = ?",
                Integer.class, sourceService);
        return count != null ? count : 0;
    }

    static class PermissionRowMapper implements RowMapper<Permission> {
        @Override
        public Permission mapRow(ResultSet rs, int rowNum) throws SQLException {
            Permission p = new Permission();
            p.setId(rs.getString("id"));
            p.setPermissionCode(rs.getString("permission_code"));
            p.setSourceService(rs.getString("source_service"));
            p.setResource(rs.getString("resource"));
            p.setAction(rs.getString("action"));
            p.setName(rs.getString("name"));
            p.setDescription(rs.getString("description"));
            p.setRiskLevel(rs.getString("risk_level"));
            p.setAssignable(rs.getInt("assignable"));
            p.setStatus(rs.getString("status"));
            p.setSourceVersion(rs.getString("source_version"));
            p.setCreatedAt(rs.getLong("created_at"));
            p.setUpdatedAt(rs.getLong("updated_at"));
            p.setVersion(rs.getLong("version"));
            return p;
        }
    }
}