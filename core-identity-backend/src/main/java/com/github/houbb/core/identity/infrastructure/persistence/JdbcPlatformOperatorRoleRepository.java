package com.github.houbb.core.identity.infrastructure.persistence;

import com.github.houbb.core.identity.application.port.PlatformOperatorRoleRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class JdbcPlatformOperatorRoleRepository implements PlatformOperatorRoleRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcPlatformOperatorRoleRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void save(String id, String operatorId, String roleCode, String grantedBy, long grantedAt, long createdAt) {
        jdbcTemplate.update(
                "INSERT INTO identity_platform_operator_role (id, operator_id, role_code, granted_by, granted_at, created_at) " +
                "VALUES (?, ?, ?, ?, ?, ?)",
                id, operatorId, roleCode, grantedBy, grantedAt, createdAt);
    }

    @Override
    public void delete(String operatorId, String roleCode) {
        jdbcTemplate.update(
                "DELETE FROM identity_platform_operator_role WHERE operator_id = ? AND role_code = ?",
                operatorId, roleCode);
    }

    @Override
    public List<String> findRoleCodesByOperatorId(String operatorId) {
        return jdbcTemplate.queryForList(
                "SELECT role_code FROM identity_platform_operator_role WHERE operator_id = ?",
                String.class, operatorId);
    }

    @Override
    public int countByOperatorAndRole(String operatorId, String roleCode) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM identity_platform_operator_role WHERE operator_id = ? AND role_code = ?",
                Integer.class, operatorId, roleCode);
        return count != null ? count : 0;
    }
}
