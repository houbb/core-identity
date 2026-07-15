package com.github.houbb.core.identity.infrastructure.persistence;

import com.github.houbb.core.identity.application.domain.SecurityEvent;
import com.github.houbb.core.identity.application.port.SecurityEventRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
public class JdbcSecurityEventRepository implements SecurityEventRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcSecurityEventRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void save(SecurityEvent e) {
        jdbcTemplate.update(
                "INSERT INTO identity_security_event (id, user_id, organization_id, event_type, severity, " +
                "status, source, risk_assessment_id, title, description, metadata_json, detected_at, " +
                "resolved_at, resolved_by, resolution, created_at, updated_at, version) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                e.getId(), e.getUserId(), e.getOrganizationId(), e.getEventType(), e.getSeverity(),
                e.getStatus(), e.getSource(), e.getRiskAssessmentId(), e.getTitle(), e.getDescription(),
                e.getMetadataJson(), e.getDetectedAt(), e.getResolvedAt(), e.getResolvedBy(),
                e.getResolution(), e.getCreatedAt(), e.getUpdatedAt(), e.getVersion()
        );
    }

    @Override
    public List<SecurityEvent> findByUserId(String userId, int limit) {
        return jdbcTemplate.query(
                "SELECT * FROM identity_security_event WHERE user_id = ? ORDER BY detected_at DESC LIMIT ?",
                new SecurityEventRowMapper(), userId, limit);
    }

    @Override
    public List<SecurityEvent> findByOrganizationId(String organizationId, int limit) {
        return jdbcTemplate.query(
                "SELECT * FROM identity_security_event WHERE organization_id = ? ORDER BY detected_at DESC LIMIT ?",
                new SecurityEventRowMapper(), organizationId, limit);
    }

    @Override
    public List<SecurityEvent> findRecentHighSeverity(int limit) {
        return jdbcTemplate.query(
                "SELECT * FROM identity_security_event WHERE severity IN ('HIGH','CRITICAL') " +
                "AND status = 'OPEN' ORDER BY detected_at DESC LIMIT ?",
                new SecurityEventRowMapper(), limit);
    }

    static class SecurityEventRowMapper implements RowMapper<SecurityEvent> {
        @Override
        public SecurityEvent mapRow(ResultSet rs, int rowNum) throws SQLException {
            SecurityEvent e = new SecurityEvent();
            e.setId(rs.getString("id"));
            e.setUserId(rs.getString("user_id"));
            e.setOrganizationId(rs.getString("organization_id"));
            e.setEventType(rs.getString("event_type"));
            e.setSeverity(rs.getString("severity"));
            e.setStatus(rs.getString("status"));
            e.setSource(rs.getString("source"));
            e.setRiskAssessmentId(rs.getString("risk_assessment_id"));
            e.setTitle(rs.getString("title"));
            e.setDescription(rs.getString("description"));
            e.setMetadataJson(rs.getString("metadata_json"));
            e.setDetectedAt(rs.getLong("detected_at"));
            e.setResolvedAt(JdbcUserRepository.getNullableLong(rs, "resolved_at"));
            e.setResolvedBy(rs.getString("resolved_by"));
            e.setResolution(rs.getString("resolution"));
            e.setCreatedAt(rs.getLong("created_at"));
            e.setUpdatedAt(rs.getLong("updated_at"));
            e.setVersion(rs.getLong("version"));
            return e;
        }
    }
}
