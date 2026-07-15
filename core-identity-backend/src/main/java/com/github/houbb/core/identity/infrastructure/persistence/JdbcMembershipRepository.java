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
                "joined_at, created_at, updated_at, version) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
                membership.getId(), membership.getOrganizationId(), membership.getUserId(),
                membership.getMembershipType(), membership.getStatus(), membership.getJoinedAt(),
                membership.getCreatedAt(), membership.getUpdatedAt(), membership.getVersion()
        );
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
    public void update(Membership membership) {
        jdbcTemplate.update(
                "UPDATE identity_membership SET membership_type = ?, status = ?, updated_at = ?, " +
                "version = version + 1 WHERE id = ? AND version = ?",
                membership.getMembershipType(), membership.getStatus(),
                membership.getUpdatedAt(), membership.getId(), membership.getVersion()
        );
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
            m.setJoinedAt(rs.getLong("joined_at"));
            m.setCreatedAt(rs.getLong("created_at"));
            m.setUpdatedAt(rs.getLong("updated_at"));
            m.setVersion(rs.getLong("version"));
            return m;
        }
    }
}