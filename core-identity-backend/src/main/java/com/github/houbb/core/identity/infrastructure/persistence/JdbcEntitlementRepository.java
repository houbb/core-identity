package com.github.houbb.core.identity.infrastructure.persistence;

import com.github.houbb.core.identity.application.domain.Entitlement;
import com.github.houbb.core.identity.application.port.EntitlementRepository;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Repository
public class JdbcEntitlementRepository implements EntitlementRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcEntitlementRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void save(Entitlement entitlement) {
        jdbcTemplate.update(
                "INSERT INTO identity_entitlement (id, organization_id, entitlement_type, target_id, " +
                "code, name, risk_level, owner_user_id, status, review_frequency_days, " +
                "created_at, updated_at, version) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                entitlement.getId(), entitlement.getOrganizationId(), entitlement.getEntitlementType(),
                entitlement.getTargetId(), entitlement.getCode(), entitlement.getName(),
                entitlement.getRiskLevel(), entitlement.getOwnerUserId(), entitlement.getStatus(),
                entitlement.getReviewFrequencyDays(), entitlement.getCreatedAt(),
                entitlement.getUpdatedAt(), entitlement.getVersion()
        );
    }

    @Override
    public Optional<Entitlement> findById(String id) {
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(
                    "SELECT * FROM identity_entitlement WHERE id = ?",
                    new EntitlementRowMapper(), id));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<Entitlement> findByCode(String code) {
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(
                    "SELECT * FROM identity_entitlement WHERE code = ?",
                    new EntitlementRowMapper(), code));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<Entitlement> findByOrganizationId(String organizationId) {
        return jdbcTemplate.query(
                "SELECT * FROM identity_entitlement WHERE organization_id = ? ORDER BY code",
                new EntitlementRowMapper(), organizationId);
    }

    @Override
    public List<Entitlement> findByOrganizationIdAndType(String organizationId, String entitlementType) {
        return jdbcTemplate.query(
                "SELECT * FROM identity_entitlement WHERE organization_id = ? AND entitlement_type = ? ORDER BY code",
                new EntitlementRowMapper(), organizationId, entitlementType);
    }

    @Override
    public List<Entitlement> findByTargetId(String targetId) {
        return jdbcTemplate.query(
                "SELECT * FROM identity_entitlement WHERE target_id = ?",
                new EntitlementRowMapper(), targetId);
    }

    @Override
    public List<Entitlement> findAllActive() {
        return jdbcTemplate.query(
                "SELECT * FROM identity_entitlement WHERE status = 'ACTIVE' ORDER BY code",
                new EntitlementRowMapper());
    }

    @Override
    public void update(Entitlement entitlement) {
        jdbcTemplate.update(
                "UPDATE identity_entitlement SET name = ?, risk_level = ?, owner_user_id = ?, " +
                "status = ?, review_frequency_days = ?, " +
                "updated_at = ?, version = version + 1 WHERE id = ? AND version = ?",
                entitlement.getName(), entitlement.getRiskLevel(), entitlement.getOwnerUserId(),
                entitlement.getStatus(), entitlement.getReviewFrequencyDays(),
                entitlement.getUpdatedAt(), entitlement.getId(), entitlement.getVersion()
        );
    }

    @Override
    public void updateStatus(String id, String status, long now, long version) {
        jdbcTemplate.update(
                "UPDATE identity_entitlement SET status = ?, updated_at = ?, version = version + 1 " +
                "WHERE id = ? AND version = ?",
                status, now, id, version);
    }

    static class EntitlementRowMapper implements RowMapper<Entitlement> {
        @Override
        public Entitlement mapRow(ResultSet rs, int rowNum) throws SQLException {
            Entitlement e = new Entitlement();
            e.setId(rs.getString("id"));
            e.setOrganizationId(rs.getString("organization_id"));
            e.setEntitlementType(rs.getString("entitlement_type"));
            e.setTargetId(rs.getString("target_id"));
            e.setCode(rs.getString("code"));
            e.setName(rs.getString("name"));
            e.setRiskLevel(rs.getString("risk_level"));
            e.setOwnerUserId(rs.getString("owner_user_id"));
            e.setStatus(rs.getString("status"));
            e.setReviewFrequencyDays(rs.getInt("review_frequency_days"));
            e.setCreatedAt(rs.getLong("created_at"));
            e.setUpdatedAt(rs.getLong("updated_at"));
            e.setVersion(rs.getLong("version"));
            return e;
        }
    }
}
