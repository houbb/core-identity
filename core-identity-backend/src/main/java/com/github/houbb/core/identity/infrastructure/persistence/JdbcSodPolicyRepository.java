package com.github.houbb.core.identity.infrastructure.persistence;

import com.github.houbb.core.identity.application.domain.SodPolicy;
import com.github.houbb.core.identity.application.port.SodPolicyRepository;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Repository
public class JdbcSodPolicyRepository implements SodPolicyRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcSodPolicyRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void save(SodPolicy policy) {
        jdbcTemplate.update(
                "INSERT INTO identity_sod_policy (id, organization_id, name, policy_type, " +
                "enforcement_mode, status, owner_user_id, created_at, updated_at, version) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                policy.getId(), policy.getOrganizationId(), policy.getName(), policy.getPolicyType(),
                policy.getEnforcementMode(), policy.getStatus(), policy.getOwnerUserId(),
                policy.getCreatedAt(), policy.getUpdatedAt(), policy.getVersion()
        );
    }

    @Override
    public Optional<SodPolicy> findById(String id) {
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(
                    "SELECT * FROM identity_sod_policy WHERE id = ?",
                    new SodPolicyRowMapper(), id));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<SodPolicy> findByOrgId(String organizationId) {
        return jdbcTemplate.query(
                "SELECT * FROM identity_sod_policy WHERE organization_id = ? ORDER BY name",
                new SodPolicyRowMapper(), organizationId);
    }

    @Override
    public List<SodPolicy> findActiveByOrgId(String organizationId) {
        return jdbcTemplate.query(
                "SELECT * FROM identity_sod_policy WHERE organization_id = ? AND status = 'ACTIVE' ORDER BY name",
                new SodPolicyRowMapper(), organizationId);
    }

    @Override
    public void update(SodPolicy policy) {
        jdbcTemplate.update(
                "UPDATE identity_sod_policy SET name = ?, enforcement_mode = ?, status = ?, " +
                "updated_at = ?, version = version + 1 WHERE id = ? AND version = ?",
                policy.getName(), policy.getEnforcementMode(), policy.getStatus(),
                policy.getUpdatedAt(), policy.getId(), policy.getVersion()
        );
    }

    @Override
    public void updateStatus(String id, String status, long now, long version) {
        jdbcTemplate.update(
                "UPDATE identity_sod_policy SET status = ?, updated_at = ?, version = version + 1 WHERE id = ? AND version = ?",
                status, now, id, version);
    }

    static class SodPolicyRowMapper implements RowMapper<SodPolicy> {
        @Override
        public SodPolicy mapRow(ResultSet rs, int rowNum) throws SQLException {
            SodPolicy p = new SodPolicy();
            p.setId(rs.getString("id"));
            p.setOrganizationId(rs.getString("organization_id"));
            p.setName(rs.getString("name"));
            p.setPolicyType(rs.getString("policy_type"));
            p.setEnforcementMode(rs.getString("enforcement_mode"));
            p.setStatus(rs.getString("status"));
            p.setOwnerUserId(rs.getString("owner_user_id"));
            p.setCreatedAt(rs.getLong("created_at"));
            p.setUpdatedAt(rs.getLong("updated_at"));
            p.setVersion(rs.getLong("version"));
            return p;
        }
    }
}
