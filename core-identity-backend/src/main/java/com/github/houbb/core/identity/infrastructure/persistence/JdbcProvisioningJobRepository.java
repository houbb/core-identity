package com.github.houbb.core.identity.infrastructure.persistence;

import com.github.houbb.core.identity.application.domain.ProvisioningJob;
import com.github.houbb.core.identity.application.port.ProvisioningJobRepository;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Repository
public class JdbcProvisioningJobRepository implements ProvisioningJobRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcProvisioningJobRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void save(ProvisioningJob j) {
        jdbcTemplate.update(
                "INSERT INTO identity_provisioning_job (id, organization_id, connection_id, job_type, " +
                "status, total_items, success_items, failed_items, started_at, completed_at, " +
                "created_at, updated_at, version) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                j.getId(), j.getOrganizationId(), j.getConnectionId(), j.getJobType(),
                j.getStatus(), j.getTotalItems(), j.getSuccessItems(), j.getFailedItems(),
                j.getStartedAt(), j.getCompletedAt(), j.getCreatedAt(), j.getUpdatedAt(), j.getVersion());
    }

    @Override
    public Optional<ProvisioningJob> findById(String id) {
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(
                    "SELECT * FROM identity_provisioning_job WHERE id = ?", new PjRowMapper(), id));
        } catch (EmptyResultDataAccessException e) { return Optional.empty(); }
    }

    @Override
    public List<ProvisioningJob> findByOrganizationId(String organizationId) {
        return jdbcTemplate.query(
                "SELECT * FROM identity_provisioning_job WHERE organization_id = ? ORDER BY created_at DESC",
                new PjRowMapper(), organizationId);
    }

    @Override
    public List<ProvisioningJob> findByConnectionId(String connectionId) {
        return jdbcTemplate.query(
                "SELECT * FROM identity_provisioning_job WHERE connection_id = ? ORDER BY created_at DESC",
                new PjRowMapper(), connectionId);
    }

    @Override
    public List<ProvisioningJob> findByStatus(String status) {
        return jdbcTemplate.query(
                "SELECT * FROM identity_provisioning_job WHERE status = ? ORDER BY created_at ASC",
                new PjRowMapper(), status);
    }

    @Override
    public void update(ProvisioningJob j) {
        jdbcTemplate.update(
                "UPDATE identity_provisioning_job SET status = ?, success_items = ?, failed_items = ?, " +
                "completed_at = ?, updated_at = ?, version = version + 1 WHERE id = ? AND version = ?",
                j.getStatus(), j.getSuccessItems(), j.getFailedItems(),
                j.getCompletedAt(), j.getUpdatedAt(), j.getId(), j.getVersion());
    }

    @Override
    public void updateStatus(String id, String status, long now, long version) {
        jdbcTemplate.update(
                "UPDATE identity_provisioning_job SET status = ?, updated_at = ?, version = version + 1 " +
                "WHERE id = ? AND version = ?", status, now, id, version);
    }

    static class PjRowMapper implements RowMapper<ProvisioningJob> {
        @Override
        public ProvisioningJob mapRow(ResultSet rs, int rowNum) throws SQLException {
            ProvisioningJob j = new ProvisioningJob();
            j.setId(rs.getString("id"));
            j.setOrganizationId(rs.getString("organization_id"));
            j.setConnectionId(rs.getString("connection_id"));
            j.setJobType(rs.getString("job_type"));
            j.setStatus(rs.getString("status"));
            j.setTotalItems(rs.getInt("total_items"));
            j.setSuccessItems(rs.getInt("success_items"));
            j.setFailedItems(rs.getInt("failed_items"));
            j.setStartedAt(getLong(rs, "started_at"));
            j.setCompletedAt(getLong(rs, "completed_at"));
            j.setCreatedAt(rs.getLong("created_at"));
            j.setUpdatedAt(rs.getLong("updated_at"));
            j.setVersion(rs.getLong("version"));
            return j;
        }
    }

    private static Long getLong(ResultSet rs, String c) throws SQLException {
        try { long v = rs.getLong(c); return rs.wasNull() ? null : v; } catch (SQLException e) { return null; }
    }
}