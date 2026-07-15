package com.github.houbb.core.identity.infrastructure.persistence;

import com.github.houbb.core.identity.application.domain.Organization;
import com.github.houbb.core.identity.application.port.OrganizationRepository;
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
public class JdbcOrganizationRepository implements OrganizationRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcOrganizationRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void save(Organization organization) {
        jdbcTemplate.update(
                "INSERT INTO identity_organization (id, organization_type, name, slug, personal_owner_user_id, " +
                "owner_user_id, description, status, logo_object_id, suspended_at, suspended_reason, " +
                "deletion_requested_at, deletion_effective_at, authorization_version, " +
                "created_at, updated_at, version) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                organization.getId(), organization.getOrganizationType(), organization.getName(),
                organization.getSlug(), organization.getPersonalOwnerUserId(), organization.getOwnerUserId(),
                organization.getDescription(), organization.getStatus(), organization.getLogoObjectId(),
                organization.getSuspendedAt(), organization.getSuspendedReason(),
                organization.getDeletionRequestedAt(), organization.getDeletionEffectiveAt(),
                organization.getAuthorizationVersion(),
                organization.getCreatedAt(), organization.getUpdatedAt(), organization.getVersion()
        );
    }

    @Override
    public Optional<Organization> findById(String id) {
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(
                    "SELECT * FROM identity_organization WHERE id = ?", new OrganizationRowMapper(), id));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<Organization> findByPersonalOwner(String userId) {
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(
                    "SELECT * FROM identity_organization WHERE personal_owner_user_id = ?",
                    new OrganizationRowMapper(), userId));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<Organization> findByOwnerUserId(String userId) {
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(
                    "SELECT * FROM identity_organization WHERE owner_user_id = ? AND organization_type = 'TEAM'",
                    new OrganizationRowMapper(), userId));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<Organization> findAllByUserId(String userId) {
        return jdbcTemplate.query(
                "SELECT o.* FROM identity_organization o " +
                "INNER JOIN identity_membership m ON o.id = m.organization_id " +
                "WHERE m.user_id = ? AND m.status = 'ACTIVE' " +
                "ORDER BY m.last_accessed_at DESC NULLS LAST, o.created_at DESC",
                new OrganizationRowMapper(), userId);
    }

    @Override
    public Optional<Organization> findBySlug(String slug) {
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(
                    "SELECT * FROM identity_organization WHERE slug = ?", new OrganizationRowMapper(), slug));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public void update(Organization organization) {
        jdbcTemplate.update(
                "UPDATE identity_organization SET name = ?, slug = ?, description = ?, status = ?, " +
                "owner_user_id = ?, logo_object_id = ?, suspended_at = ?, suspended_reason = ?, " +
                "deletion_requested_at = ?, deletion_effective_at = ?, " +
                "authorization_version = ?, updated_at = ?, " +
                "version = version + 1 WHERE id = ? AND version = ?",
                organization.getName(), organization.getSlug(), organization.getDescription(),
                organization.getStatus(), organization.getOwnerUserId(), organization.getLogoObjectId(),
                organization.getSuspendedAt(), organization.getSuspendedReason(),
                organization.getDeletionRequestedAt(), organization.getDeletionEffectiveAt(),
                organization.getAuthorizationVersion(), organization.getUpdatedAt(),
                organization.getId(), organization.getVersion()
        );
    }

    @Override
    public void updateOwner(String id, String newOwnerUserId, long authorizationVersion, long now, long version) {
        jdbcTemplate.update(
                "UPDATE identity_organization SET owner_user_id = ?, authorization_version = ?, " +
                "updated_at = ?, version = version + 1 WHERE id = ? AND version = ?",
                newOwnerUserId, authorizationVersion, now, id, version);
    }

    @Override
    public void updateStatus(String id, String status, long now, long version) {
        jdbcTemplate.update(
                "UPDATE identity_organization SET status = ?, updated_at = ?, version = version + 1 " +
                "WHERE id = ? AND version = ?",
                status, now, id, version);
    }

    @Override
    public int countByUserIdAndStatus(String userId, String status) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM identity_membership WHERE user_id = ? AND status = ?",
                Integer.class, userId, status);
        return count != null ? count : 0;
    }

    static class OrganizationRowMapper implements RowMapper<Organization> {
        @Override
        public Organization mapRow(ResultSet rs, int rowNum) throws SQLException {
            Organization o = new Organization();
            o.setId(rs.getString("id"));
            o.setOrganizationType(rs.getString("organization_type"));
            o.setName(rs.getString("name"));
            o.setSlug(rs.getString("slug"));
            o.setPersonalOwnerUserId(rs.getString("personal_owner_user_id"));
            // P2 fields - may be null for pre-migration rows
            o.setOwnerUserId(getStringOrNull(rs, "owner_user_id"));
            o.setDescription(getStringOrNull(rs, "description"));
            o.setStatus(rs.getString("status"));
            o.setLogoObjectId(getStringOrNull(rs, "logo_object_id"));
            o.setSuspendedAt(getLongOrNull(rs, "suspended_at"));
            o.setSuspendedReason(getStringOrNull(rs, "suspended_reason"));
            o.setDeletionRequestedAt(getLongOrNull(rs, "deletion_requested_at"));
            o.setDeletionEffectiveAt(getLongOrNull(rs, "deletion_effective_at"));
            o.setAuthorizationVersion(getLongOrDefault(rs, "authorization_version", 1));
            o.setCreatedAt(rs.getLong("created_at"));
            o.setUpdatedAt(rs.getLong("updated_at"));
            o.setVersion(rs.getLong("version"));
            return o;
        }
    }

    private static String getStringOrNull(ResultSet rs, String column) throws SQLException {
        try {
            return rs.getString(column);
        } catch (SQLException e) {
            return null;
        }
    }

    private static Long getLongOrNull(ResultSet rs, String column) throws SQLException {
        try {
            long val = rs.getLong(column);
            return rs.wasNull() ? null : val;
        } catch (SQLException e) {
            return null;
        }
    }

    private static long getLongOrDefault(ResultSet rs, String column, long defaultVal) throws SQLException {
        try {
            long val = rs.getLong(column);
            return rs.wasNull() ? defaultVal : val;
        } catch (SQLException e) {
            return defaultVal;
        }
    }
}