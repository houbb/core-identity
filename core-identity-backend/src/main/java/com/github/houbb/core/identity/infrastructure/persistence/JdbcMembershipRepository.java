package com.github.houbb.core.identity.infrastructure.persistence;

import com.github.houbb.core.identity.application.domain.Membership;
import com.github.houbb.core.identity.application.port.MembershipRepository;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Repository
public class JdbcMembershipRepository implements MembershipRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcMembershipRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void save(Membership membership) {
        jdbcTemplate.update(
                "INSERT INTO identity_membership (id, organization_id, user_id, membership_type, status, " +
                "source, joined_at, left_at, removed_at, suspended_at, last_accessed_at, created_by, " +
                "created_at, updated_at, version) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                membership.getId(), membership.getOrganizationId(), membership.getUserId(),
                membership.getMembershipType(), membership.getStatus(), membership.getSource(),
                membership.getJoinedAt(), membership.getLeftAt(), membership.getRemovedAt(),
                membership.getSuspendedAt(), membership.getLastAccessedAt(), membership.getCreatedBy(),
                membership.getCreatedAt(), membership.getUpdatedAt(), membership.getVersion()
        );
    }

    @Override
    public Optional<Membership> findById(String id) {
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(
                    "SELECT * FROM identity_membership WHERE id = ?", new MembershipRowMapper(), id));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<Membership> findByOrgAndUser(String organizationId, String userId) {
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(
                    "SELECT * FROM identity_membership WHERE organization_id = ? AND user_id = ?",
                    new MembershipRowMapper(), organizationId, userId));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<Membership> findByUserId(String userId) {
        return jdbcTemplate.query(
                "SELECT * FROM identity_membership WHERE user_id = ?", new MembershipRowMapper(), userId);
    }

    @Override
    public List<Membership> findByOrganizationId(String organizationId) {
        return jdbcTemplate.query(
                "SELECT * FROM identity_membership WHERE organization_id = ?",
                new MembershipRowMapper(), organizationId);
    }

    @Override
    public List<Membership> findByOrgAndStatus(String organizationId, String status) {
        return jdbcTemplate.query(
                "SELECT * FROM identity_membership WHERE organization_id = ? AND status = ?",
                new MembershipRowMapper(), organizationId, status);
    }

    @Override
    public void update(Membership membership) {
        jdbcTemplate.update(
                "UPDATE identity_membership SET membership_type = ?, status = ?, source = ?, " +
                "left_at = ?, removed_at = ?, suspended_at = ?, last_accessed_at = ?, " +
                "updated_at = ?, version = version + 1 WHERE id = ? AND version = ?",
                membership.getMembershipType(), membership.getStatus(), membership.getSource(),
                membership.getLeftAt(), membership.getRemovedAt(), membership.getSuspendedAt(),
                membership.getLastAccessedAt(),
                membership.getUpdatedAt(), membership.getId(), membership.getVersion()
        );
    }

    @Override
    public void updateStatus(String id, String status, long now, long version) {
        jdbcTemplate.update(
                "UPDATE identity_membership SET status = ?, updated_at = ?, version = version + 1 " +
                "WHERE id = ? AND version = ?",
                status, now, id, version);
    }

    @Override
    public void updateLastAccessed(String id, long now, long version) {
        jdbcTemplate.update(
                "UPDATE identity_membership SET last_accessed_at = ?, updated_at = now " +
                "WHERE id = ? AND version = ?",
                now, id, version);
    }

    @Override
    public int countActiveByOrgId(String organizationId) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM identity_membership WHERE organization_id = ? AND status = 'ACTIVE'",
                Integer.class, organizationId);
        return count != null ? count : 0;
    }

    static class MembershipRowMapper implements RowMapper<Membership> {
        @Override
        public Membership mapRow(ResultSet rs, int rowNum) throws SQLException {
            Membership m = new Membership();
            m.setId(rs.getString("id"));
            m.setOrganizationId(rs.getString("organization_id"));
            m.setUserId(rs.getString("user_id"));
            m.setMembershipType(rs.getString("membership_type"));
            m.setStatus(rs.getString("status"));
            m.setSource(getStringOrNull(rs, "source"));
            m.setJoinedAt(rs.getLong("joined_at"));
            m.setLeftAt(getLongOrNull(rs, "left_at"));
            m.setRemovedAt(getLongOrNull(rs, "removed_at"));
            m.setSuspendedAt(getLongOrNull(rs, "suspended_at"));
            m.setLastAccessedAt(getLongOrNull(rs, "last_accessed_at"));
            m.setCreatedBy(getStringOrNull(rs, "created_by"));
            m.setCreatedAt(rs.getLong("created_at"));
            m.setUpdatedAt(rs.getLong("updated_at"));
            m.setVersion(rs.getLong("version"));
            return m;
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
}