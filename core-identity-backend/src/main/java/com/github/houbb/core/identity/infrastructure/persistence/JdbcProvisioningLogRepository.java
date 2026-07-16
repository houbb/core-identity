package com.github.houbb.core.identity.infrastructure.persistence;

import com.github.houbb.core.identity.application.domain.ProvisioningLog;
import com.github.houbb.core.identity.application.port.ProvisioningLogRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
public class JdbcProvisioningLogRepository implements ProvisioningLogRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcProvisioningLogRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void save(ProvisioningLog l) {
        jdbcTemplate.update(
                "INSERT INTO identity_provisioning_log (id, job_id, resource_type, external_id, " +
                "operation, result, error_code, error_message, request_id, occurred_at, created_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                l.getId(), l.getJobId(), l.getResourceType(), l.getExternalId(),
                l.getOperation(), l.getResult(), l.getErrorCode(), l.getErrorMessage(),
                l.getRequestId(), l.getOccurredAt(), l.getCreatedAt());
    }

    @Override
    public List<ProvisioningLog> findByJobId(String jobId) {
        return jdbcTemplate.query(
                "SELECT * FROM identity_provisioning_log WHERE job_id = ? ORDER BY occurred_at DESC",
                new PlRowMapper(), jobId);
    }

    @Override
    public List<ProvisioningLog> findByConnectionIdAndResult(String connectionId, String result) {
        // Note: provisioning_log doesn't have connection_id directly. Filter by job's connection.
        return jdbcTemplate.query(
                "SELECT pl.* FROM identity_provisioning_log pl " +
                "INNER JOIN identity_provisioning_job pj ON pl.job_id = pj.id " +
                "WHERE pj.connection_id = ? AND pl.result = ? ORDER BY pl.occurred_at DESC",
                new PlRowMapper(), connectionId, result);
    }

    static class PlRowMapper implements RowMapper<ProvisioningLog> {
        @Override
        public ProvisioningLog mapRow(ResultSet rs, int rowNum) throws SQLException {
            ProvisioningLog l = new ProvisioningLog();
            l.setId(rs.getString("id"));
            l.setJobId(getStr(rs, "job_id"));
            l.setResourceType(rs.getString("resource_type"));
            l.setExternalId(getStr(rs, "external_id"));
            l.setOperation(rs.getString("operation"));
            l.setResult(rs.getString("result"));
            l.setErrorCode(getStr(rs, "error_code"));
            l.setErrorMessage(getStr(rs, "error_message"));
            l.setRequestId(getStr(rs, "request_id"));
            l.setOccurredAt(rs.getLong("occurred_at"));
            l.setCreatedAt(rs.getLong("created_at"));
            return l;
        }
    }

    private static String getStr(ResultSet rs, String c) throws SQLException {
        try { return rs.getString(c); } catch (SQLException e) { return null; }
    }
}