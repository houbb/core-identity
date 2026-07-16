package com.github.houbb.core.identity.infrastructure.persistence;

import com.github.houbb.core.identity.application.domain.JitPolicy;
import com.github.houbb.core.identity.application.port.JitPolicyRepository;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

@Repository
public class JdbcJitPolicyRepository implements JitPolicyRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcJitPolicyRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void save(JitPolicy p) {
        jdbcTemplate.update(
                "INSERT INTO identity_jit_policy (id, connection_id, status, allow_new_users, " +
                "allow_existing_link, require_verified_email, allowed_domains_json, default_role_ids_json, " +
                "sync_profile_on_login, sync_groups_on_login, require_approval, " +
                "created_at, updated_at, version) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                p.getId(), p.getConnectionId(), p.getStatus(), p.getAllowNewUsers(),
                p.getAllowExistingLink(), p.getRequireVerifiedEmail(), p.getAllowedDomainsJson(),
                p.getDefaultRoleIdsJson(), p.getSyncProfileOnLogin(), p.getSyncGroupsOnLogin(),
                p.getRequireApproval(), p.getCreatedAt(), p.getUpdatedAt(), p.getVersion()
        );
    }

    @Override
    public Optional<JitPolicy> findById(String id) {
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(
                    "SELECT * FROM identity_jit_policy WHERE id = ?",
                    new JitPolicyRowMapper(), id));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<JitPolicy> findByConnectionId(String connectionId) {
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(
                    "SELECT * FROM identity_jit_policy WHERE connection_id = ?",
                    new JitPolicyRowMapper(), connectionId));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public void update(JitPolicy p) {
        jdbcTemplate.update(
                "UPDATE identity_jit_policy SET status = ?, allow_new_users = ?, " +
                "allow_existing_link = ?, require_verified_email = ?, allowed_domains_json = ?, " +
                "default_role_ids_json = ?, sync_profile_on_login = ?, sync_groups_on_login = ?, " +
                "require_approval = ?, updated_at = ?, version = version + 1 " +
                "WHERE id = ? AND version = ?",
                p.getStatus(), p.getAllowNewUsers(), p.getAllowExistingLink(),
                p.getRequireVerifiedEmail(), p.getAllowedDomainsJson(), p.getDefaultRoleIdsJson(),
                p.getSyncProfileOnLogin(), p.getSyncGroupsOnLogin(), p.getRequireApproval(),
                p.getUpdatedAt(), p.getId(), p.getVersion()
        );
    }

    static class JitPolicyRowMapper implements RowMapper<JitPolicy> {
        @Override
        public JitPolicy mapRow(ResultSet rs, int rowNum) throws SQLException {
            JitPolicy p = new JitPolicy();
            p.setId(rs.getString("id"));
            p.setConnectionId(rs.getString("connection_id"));
            p.setStatus(rs.getString("status"));
            p.setAllowNewUsers(rs.getInt("allow_new_users"));
            p.setAllowExistingLink(rs.getInt("allow_existing_link"));
            p.setRequireVerifiedEmail(rs.getInt("require_verified_email"));
            p.setAllowedDomainsJson(rs.getString("allowed_domains_json"));
            p.setDefaultRoleIdsJson(rs.getString("default_role_ids_json"));
            p.setSyncProfileOnLogin(rs.getInt("sync_profile_on_login"));
            p.setSyncGroupsOnLogin(rs.getInt("sync_groups_on_login"));
            p.setRequireApproval(rs.getInt("require_approval"));
            p.setCreatedAt(rs.getLong("created_at"));
            p.setUpdatedAt(rs.getLong("updated_at"));
            p.setVersion(rs.getLong("version"));
            return p;
        }
    }
}