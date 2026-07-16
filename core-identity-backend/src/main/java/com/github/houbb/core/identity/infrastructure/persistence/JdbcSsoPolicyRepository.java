package com.github.houbb.core.identity.infrastructure.persistence;

import com.github.houbb.core.identity.application.domain.SsoPolicy;
import com.github.houbb.core.identity.application.port.SsoPolicyRepository;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

@Repository
public class JdbcSsoPolicyRepository implements SsoPolicyRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcSsoPolicyRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void save(SsoPolicy p) {
        jdbcTemplate.update(
                "INSERT INTO identity_sso_policy (id, organization_id, enforcement_mode, " +
                "connection_ids_json, grace_period_ends_at, local_login_allowed, " +
                "require_sso_for_privileged, break_glass_required, status, published_at, " +
                "created_by, created_at, updated_at, version) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                p.getId(), p.getOrganizationId(), p.getEnforcementMode(),
                p.getConnectionIdsJson(), p.getGracePeriodEndsAt(), p.getLocalLoginAllowed(),
                p.getRequireSsoForPrivileged(), p.getBreakGlassRequired(), p.getStatus(),
                p.getPublishedAt(), p.getCreatedBy(), p.getCreatedAt(), p.getUpdatedAt(),
                p.getVersion()
        );
    }

    @Override
    public Optional<SsoPolicy> findById(String id) {
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(
                    "SELECT * FROM identity_sso_policy WHERE id = ?",
                    new SsoPolicyRowMapper(), id));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<SsoPolicy> findByOrganizationId(String organizationId) {
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(
                    "SELECT * FROM identity_sso_policy WHERE organization_id = ?",
                    new SsoPolicyRowMapper(), organizationId));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public void update(SsoPolicy p) {
        jdbcTemplate.update(
                "UPDATE identity_sso_policy SET enforcement_mode = ?, " +
                "connection_ids_json = ?, grace_period_ends_at = ?, local_login_allowed = ?, " +
                "require_sso_for_privileged = ?, break_glass_required = ?, status = ?, " +
                "published_at = ?, updated_at = ?, version = version + 1 " +
                "WHERE id = ? AND version = ?",
                p.getEnforcementMode(), p.getConnectionIdsJson(), p.getGracePeriodEndsAt(),
                p.getLocalLoginAllowed(), p.getRequireSsoForPrivileged(),
                p.getBreakGlassRequired(), p.getStatus(), p.getPublishedAt(),
                p.getUpdatedAt(), p.getId(), p.getVersion()
        );
    }

    static class SsoPolicyRowMapper implements RowMapper<SsoPolicy> {
        @Override
        public SsoPolicy mapRow(ResultSet rs, int rowNum) throws SQLException {
            SsoPolicy p = new SsoPolicy();
            p.setId(rs.getString("id"));
            p.setOrganizationId(rs.getString("organization_id"));
            p.setEnforcementMode(rs.getString("enforcement_mode"));
            p.setConnectionIdsJson(rs.getString("connection_ids_json"));
            p.setGracePeriodEndsAt(getNullableLong(rs, "grace_period_ends_at"));
            p.setLocalLoginAllowed(rs.getInt("local_login_allowed"));
            p.setRequireSsoForPrivileged(rs.getInt("require_sso_for_privileged"));
            p.setBreakGlassRequired(rs.getInt("break_glass_required"));
            p.setStatus(rs.getString("status"));
            p.setPublishedAt(getNullableLong(rs, "published_at"));
            p.setCreatedBy(rs.getString("created_by"));
            p.setCreatedAt(rs.getLong("created_at"));
            p.setUpdatedAt(rs.getLong("updated_at"));
            p.setVersion(rs.getLong("version"));
            return p;
        }

        private Long getNullableLong(ResultSet rs, String column) throws SQLException {
            long val = rs.getLong(column);
            return rs.wasNull() ? null : val;
        }
    }
}