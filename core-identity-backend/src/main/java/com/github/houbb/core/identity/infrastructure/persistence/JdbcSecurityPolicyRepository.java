package com.github.houbb.core.identity.infrastructure.persistence;

import com.github.houbb.core.identity.application.domain.SecurityPolicy;
import com.github.houbb.core.identity.application.port.SecurityPolicyRepository;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

@Repository
public class JdbcSecurityPolicyRepository implements SecurityPolicyRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcSecurityPolicyRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Optional<SecurityPolicy> findByOrganizationId(String organizationId) {
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(
                    "SELECT * FROM identity_security_policy WHERE organization_id = ? AND status != 'SUSPENDED'",
                    new PolicyRowMapper(), organizationId));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public void save(SecurityPolicy p) {
        jdbcTemplate.update(
                "INSERT INTO identity_security_policy (id, organization_id, name, status, minimum_auth_level, " +
                "phishing_resistant_required, allowed_authenticator_types_json, privileged_roles_only, " +
                "trusted_device_days, session_idle_seconds, session_absolute_seconds, reauth_seconds, " +
                "grace_period_ends_at, created_by, published_at, created_at, updated_at, version) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                p.getId(), p.getOrganizationId(), p.getName(), p.getStatus(), p.getMinimumAuthLevel(),
                p.getPhishingResistantRequired(), p.getAllowedAuthenticatorTypesJson(), p.getPrivilegedRolesOnly(),
                p.getTrustedDeviceDays(), p.getSessionIdleSeconds(), p.getSessionAbsoluteSeconds(),
                p.getReauthSeconds(), p.getGracePeriodEndsAt(), p.getCreatedBy(), p.getPublishedAt(),
                p.getCreatedAt(), p.getUpdatedAt(), p.getVersion()
        );
    }

    @Override
    public void update(SecurityPolicy p) {
        jdbcTemplate.update(
                "UPDATE identity_security_policy SET name = ?, status = ?, minimum_auth_level = ?, " +
                "phishing_resistant_required = ?, allowed_authenticator_types_json = ?, privileged_roles_only = ?, " +
                "trusted_device_days = ?, session_idle_seconds = ?, session_absolute_seconds = ?, " +
                "reauth_seconds = ?, grace_period_ends_at = ?, published_at = ?, updated_at = ?, " +
                "version = version + 1 WHERE id = ? AND version = ?",
                p.getName(), p.getStatus(), p.getMinimumAuthLevel(),
                p.getPhishingResistantRequired(), p.getAllowedAuthenticatorTypesJson(), p.getPrivilegedRolesOnly(),
                p.getTrustedDeviceDays(), p.getSessionIdleSeconds(), p.getSessionAbsoluteSeconds(),
                p.getReauthSeconds(), p.getGracePeriodEndsAt(), p.getPublishedAt(), p.getUpdatedAt(),
                p.getId(), p.getVersion()
        );
    }

    static class PolicyRowMapper implements RowMapper<SecurityPolicy> {
        @Override
        public SecurityPolicy mapRow(ResultSet rs, int rowNum) throws SQLException {
            SecurityPolicy p = new SecurityPolicy();
            p.setId(rs.getString("id"));
            p.setOrganizationId(rs.getString("organization_id"));
            p.setName(rs.getString("name"));
            p.setStatus(rs.getString("status"));
            p.setMinimumAuthLevel(rs.getString("minimum_auth_level"));
            p.setPhishingResistantRequired(rs.getInt("phishing_resistant_required"));
            p.setAllowedAuthenticatorTypesJson(rs.getString("allowed_authenticator_types_json"));
            p.setPrivilegedRolesOnly(rs.getInt("privileged_roles_only"));
            p.setTrustedDeviceDays(getNullableInt(rs, "trusted_device_days"));
            p.setSessionIdleSeconds(getNullableInt(rs, "session_idle_seconds"));
            p.setSessionAbsoluteSeconds(getNullableInt(rs, "session_absolute_seconds"));
            p.setReauthSeconds(getNullableInt(rs, "reauth_seconds"));
            p.setGracePeriodEndsAt(getNullableLong(rs, "grace_period_ends_at"));
            p.setCreatedBy(rs.getString("created_by"));
            p.setPublishedAt(getNullableLong(rs, "published_at"));
            p.setCreatedAt(rs.getLong("created_at"));
            p.setUpdatedAt(rs.getLong("updated_at"));
            p.setVersion(rs.getLong("version"));
            return p;
        }

        private Long getNullableLong(ResultSet rs, String column) throws SQLException {
            long val = rs.getLong(column);
            return rs.wasNull() ? null : val;
        }

        private Integer getNullableInt(ResultSet rs, String column) throws SQLException {
            int val = rs.getInt(column);
            return rs.wasNull() ? null : val;
        }
    }
}
