package com.github.houbb.core.identity.infrastructure.persistence;

import com.github.houbb.core.identity.application.domain.AccessRequest;
import com.github.houbb.core.identity.application.port.AccessRequestRepository;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Repository
public class JdbcAccessRequestRepository implements AccessRequestRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcAccessRequestRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void save(AccessRequest request) {
        jdbcTemplate.update(
                "INSERT INTO identity_access_request (id, requester_user_id, target_subject_type, " +
                "target_subject_id, organization_id, access_package_id, business_reason, " +
                "ticket_reference, requested_start_at, requested_end_at, status, risk_level, " +
                "sod_result, submitted_at, completed_at, created_at, updated_at, version) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                request.getId(), request.getRequesterUserId(), request.getTargetSubjectType(),
                request.getTargetSubjectId(), request.getOrganizationId(), request.getAccessPackageId(),
                request.getBusinessReason(), request.getTicketReference(),
                request.getRequestedStartAt(), request.getRequestedEndAt(),
                request.getStatus(), request.getRiskLevel(), request.getSodResult(),
                request.getSubmittedAt(), request.getCompletedAt(),
                request.getCreatedAt(), request.getUpdatedAt(), request.getVersion()
        );
    }

    @Override
    public Optional<AccessRequest> findById(String id) {
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(
                    "SELECT * FROM identity_access_request WHERE id = ?",
                    new AccessRequestRowMapper(), id));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<AccessRequest> findByRequesterId(String requesterUserId) {
        return jdbcTemplate.query(
                "SELECT * FROM identity_access_request WHERE requester_user_id = ? ORDER BY created_at DESC",
                new AccessRequestRowMapper(), requesterUserId);
    }

    @Override
    public List<AccessRequest> findByOrganizationId(String organizationId) {
        return jdbcTemplate.query(
                "SELECT * FROM identity_access_request WHERE organization_id = ? ORDER BY created_at DESC",
                new AccessRequestRowMapper(), organizationId);
    }

    @Override
    public List<AccessRequest> findByStatus(String status) {
        return jdbcTemplate.query(
                "SELECT * FROM identity_access_request WHERE status = ? ORDER BY created_at DESC",
                new AccessRequestRowMapper(), status);
    }

    @Override
    public List<AccessRequest> findByOrgAndStatus(String organizationId, String status) {
        return jdbcTemplate.query(
                "SELECT * FROM identity_access_request WHERE organization_id = ? AND status = ? ORDER BY created_at DESC",
                new AccessRequestRowMapper(), organizationId, status);
    }

    @Override
    public List<AccessRequest> findPendingByOrg(String organizationId) {
        return jdbcTemplate.query(
                "SELECT * FROM identity_access_request WHERE organization_id = ? AND status IN ('SUBMITTED','IN_REVIEW') ORDER BY created_at ASC",
                new AccessRequestRowMapper(), organizationId);
    }

    @Override
    public void update(AccessRequest request) {
        jdbcTemplate.update(
                "UPDATE identity_access_request SET status = ?, risk_level = ?, sod_result = ?, " +
                "completed_at = ?, updated_at = ?, version = version + 1 WHERE id = ? AND version = ?",
                request.getStatus(), request.getRiskLevel(), request.getSodResult(),
                request.getCompletedAt(), request.getUpdatedAt(),
                request.getId(), request.getVersion()
        );
    }

    @Override
    public void updateStatus(String id, String status, long completedAt, long now, long version) {
        jdbcTemplate.update(
                "UPDATE identity_access_request SET status = ?, completed_at = ?, " +
                "updated_at = ?, version = version + 1 WHERE id = ? AND version = ?",
                status, completedAt, now, id, version);
    }

    static class AccessRequestRowMapper implements RowMapper<AccessRequest> {
        @Override
        public AccessRequest mapRow(ResultSet rs, int rowNum) throws SQLException {
            AccessRequest req = new AccessRequest();
            req.setId(rs.getString("id"));
            req.setRequesterUserId(rs.getString("requester_user_id"));
            req.setTargetSubjectType(rs.getString("target_subject_type"));
            req.setTargetSubjectId(rs.getString("target_subject_id"));
            req.setOrganizationId(rs.getString("organization_id"));
            req.setAccessPackageId(rs.getString("access_package_id"));
            req.setBusinessReason(rs.getString("business_reason"));
            req.setTicketReference(rs.getString("ticket_reference"));
            req.setRequestedStartAt(getLongOrNull(rs, "requested_start_at"));
            req.setRequestedEndAt(getLongOrNull(rs, "requested_end_at"));
            req.setStatus(rs.getString("status"));
            req.setRiskLevel(rs.getString("risk_level"));
            req.setSodResult(rs.getString("sod_result"));
            req.setSubmittedAt(getLongOrNull(rs, "submitted_at"));
            req.setCompletedAt(getLongOrNull(rs, "completed_at"));
            req.setCreatedAt(rs.getLong("created_at"));
            req.setUpdatedAt(rs.getLong("updated_at"));
            req.setVersion(rs.getLong("version"));
            return req;
        }

        private Long getLongOrNull(ResultSet rs, String column) throws SQLException {
            long val = rs.getLong(column);
            return rs.wasNull() ? null : val;
        }
    }
}