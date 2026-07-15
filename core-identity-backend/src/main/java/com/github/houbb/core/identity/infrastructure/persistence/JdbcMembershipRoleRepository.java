package com.github.houbb.core.identity.infrastructure.persistence;

import com.github.houbb.core.identity.application.domain.MembershipRole;
import com.github.houbb.core.identity.application.port.MembershipRoleRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Repository
public class JdbcMembershipRoleRepository implements MembershipRoleRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcMembershipRoleRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void save(MembershipRole mr) {
        jdbcTemplate.update(
                "INSERT INTO identity_membership_role (membership_id, role_id, assigned_by, created_at) " +
                "VALUES (?, ?, ?, ?)",
                mr.getMembershipId(), mr.getRoleId(), mr.getAssignedBy(), mr.getCreatedAt()
        );
    }

    @Override
    public void deleteByMembershipAndRole(String membershipId, String roleId) {
        jdbcTemplate.update(
                "DELETE FROM identity_membership_role WHERE membership_id = ? AND role_id = ?",
                membershipId, roleId);
    }

    @Override
    public void deleteAllByMembershipId(String membershipId) {
        jdbcTemplate.update(
                "DELETE FROM identity_membership_role WHERE membership_id = ?", membershipId);
    }

    @Override
    public List<MembershipRole> findByMembershipId(String membershipId) {
        return jdbcTemplate.query(
                "SELECT * FROM identity_membership_role WHERE membership_id = ?",
                new MembershipRoleRowMapper(), membershipId);
    }

    @Override
    public List<MembershipRole> findByRoleId(String roleId) {
        return jdbcTemplate.query(
                "SELECT * FROM identity_membership_role WHERE role_id = ?",
                new MembershipRoleRowMapper(), roleId);
    }

    @Override
    public List<String> findRoleIdsByMembershipId(String membershipId) {
        return jdbcTemplate.queryForList(
                "SELECT role_id FROM identity_membership_role WHERE membership_id = ?",
                String.class, membershipId);
    }

    @Override
    public List<String> findRoleIdsByMembershipIds(List<String> membershipIds) {
        if (membershipIds == null || membershipIds.isEmpty()) {
            return Collections.emptyList();
        }
        StringBuilder sql = new StringBuilder(
                "SELECT DISTINCT role_id FROM identity_membership_role WHERE membership_id IN (");
        List<Object> params = new ArrayList<>();
        for (int i = 0; i < membershipIds.size(); i++) {
            if (i > 0) sql.append(", ");
            sql.append("?");
            params.add(membershipIds.get(i));
        }
        sql.append(")");
        return jdbcTemplate.queryForList(sql.toString(), String.class, params.toArray());
    }

    @Override
    public int countByRoleId(String roleId) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM identity_membership_role WHERE role_id = ?",
                Integer.class, roleId);
        return count != null ? count : 0;
    }

    @Override
    @Transactional
    public void replaceRoles(String membershipId, List<String> roleIds, String assignedBy, long now) {
        jdbcTemplate.update(
                "DELETE FROM identity_membership_role WHERE membership_id = ?", membershipId);
        for (String roleId : roleIds) {
            jdbcTemplate.update(
                    "INSERT INTO identity_membership_role (membership_id, role_id, assigned_by, created_at) " +
                    "VALUES (?, ?, ?, ?)",
                    membershipId, roleId, assignedBy, now);
        }
    }

    static class MembershipRoleRowMapper implements RowMapper<MembershipRole> {
        @Override
        public MembershipRole mapRow(ResultSet rs, int rowNum) throws SQLException {
            MembershipRole mr = new MembershipRole();
            mr.setMembershipId(rs.getString("membership_id"));
            mr.setRoleId(rs.getString("role_id"));
            mr.setAssignedBy(rs.getString("assigned_by"));
            mr.setCreatedAt(rs.getLong("created_at"));
            return mr;
        }
    }
}