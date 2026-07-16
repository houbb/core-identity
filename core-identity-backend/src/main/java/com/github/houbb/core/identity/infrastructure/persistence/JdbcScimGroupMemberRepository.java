package com.github.houbb.core.identity.infrastructure.persistence;

import com.github.houbb.core.identity.application.domain.*;
import com.github.houbb.core.identity.application.port.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
public class JdbcScimGroupMemberRepository implements ScimGroupMemberRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcScimGroupMemberRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void save(ScimGroupMember m) {
        jdbcTemplate.update(
                "INSERT INTO identity_scim_group_member (group_id, external_identity_id, membership_id, created_at) " +
                "VALUES (?, ?, ?, ?)",
                m.getGroupId(), m.getExternalIdentityId(), m.getMembershipId(), m.getCreatedAt());
    }

    @Override
    public List<ScimGroupMember> findByGroupId(String groupId) {
        return jdbcTemplate.query(
                "SELECT * FROM identity_scim_group_member WHERE group_id = ?", new SgmRowMapper(), groupId);
    }

    @Override
    public List<ScimGroupMember> findByExternalIdentityId(String externalIdentityId) {
        return jdbcTemplate.query(
                "SELECT * FROM identity_scim_group_member WHERE external_identity_id = ?",
                new SgmRowMapper(), externalIdentityId);
    }

    @Override
    public void deleteByGroupId(String groupId) {
        jdbcTemplate.update("DELETE FROM identity_scim_group_member WHERE group_id = ?", groupId);
    }

    @Override
    public void deleteByGroupIdAndExternalIdentityId(String groupId, String externalIdentityId) {
        jdbcTemplate.update(
                "DELETE FROM identity_scim_group_member WHERE group_id = ? AND external_identity_id = ?",
                groupId, externalIdentityId);
    }

    static class SgmRowMapper implements RowMapper<ScimGroupMember> {
        @Override
        public ScimGroupMember mapRow(ResultSet rs, int rowNum) throws SQLException {
            ScimGroupMember m = new ScimGroupMember();
            m.setGroupId(rs.getString("group_id"));
            m.setExternalIdentityId(rs.getString("external_identity_id"));
            m.setMembershipId(getStr(rs, "membership_id"));
            m.setCreatedAt(rs.getLong("created_at"));
            return m;
        }
    }

    private static String getStr(ResultSet rs, String c) throws SQLException {
        try { return rs.getString(c); } catch (SQLException e) { return null; }
    }
}