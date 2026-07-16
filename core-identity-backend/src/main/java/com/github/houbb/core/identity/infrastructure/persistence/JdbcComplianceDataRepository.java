package com.github.houbb.core.identity.infrastructure.persistence;

import com.github.houbb.core.identity.application.port.ComplianceDataRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public class JdbcComplianceDataRepository implements ComplianceDataRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcComplianceDataRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // ── Control ──────────────────────────────────────────────────

    @Override
    public void insertControl(String id, String controlCode, String name, String description,
                              String controlType, String ownerUserId, String frequency, long now) {
        jdbcTemplate.update(
                "INSERT INTO identity_compliance_control (id, control_code, name, description, " +
                "control_type, owner_user_id, frequency, created_at, updated_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
                id, controlCode, name, description, controlType, ownerUserId, frequency, now, now
        );
    }

    @Override
    public List<Map<String, Object>> findAllControls() {
        return jdbcTemplate.queryForList(
                "SELECT * FROM identity_compliance_control ORDER BY control_code");
    }

    @Override
    public void updateControlStatus(String id, String status, long now) {
        jdbcTemplate.update(
                "UPDATE identity_compliance_control SET status = ?, updated_at = ? WHERE id = ?",
                status, now, id);
    }

    // ── Framework ────────────────────────────────────────────────

    @Override
    public void insertFramework(String id, String frameworkCode, String name, String version,
                                String publisher, long now) {
        jdbcTemplate.update(
                "INSERT INTO identity_compliance_framework (id, framework_code, name, version, " +
                "publisher, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?)",
                id, frameworkCode, name, version, publisher, now, now
        );
    }

    // ── Mapping ──────────────────────────────────────────────────

    @Override
    public void upsertControlMapping(String controlId, String frameworkId, String requirementCode) {
        jdbcTemplate.update(
                "INSERT INTO identity_control_mapping (control_id, framework_id, requirement_code) " +
                "VALUES (?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE requirement_code = VALUES(requirement_code)",
                controlId, frameworkId, requirementCode
        );
    }

    // ── Finding ──────────────────────────────────────────────────

    @Override
    public void insertFinding(String id, String controlId, String title, String description,
                              String severity, String ownerUserId, long dueAt, long now) {
        jdbcTemplate.update(
                "INSERT INTO identity_control_finding (id, control_id, title, description, " +
                "severity, owner_user_id, due_at, created_at, updated_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
                id, controlId, title, description, severity, ownerUserId, dueAt, now, now
        );
    }

    @Override
    public List<Map<String, Object>> findOpenFindingsWithControls() {
        return jdbcTemplate.queryForList(
                "SELECT f.*, c.control_code, c.name AS control_name " +
                "FROM identity_control_finding f " +
                "JOIN identity_compliance_control c ON f.control_id = c.id " +
                "WHERE f.status = 'OPEN' " +
                "ORDER BY f.severity DESC, f.due_at");
    }

    @Override
    public void updateFindingResolved(String id, long now) {
        jdbcTemplate.update(
                "UPDATE identity_control_finding SET status = 'RESOLVED', updated_at = ? WHERE id = ?",
                now, id);
    }

    // ── Evidence ─────────────────────────────────────────────────

    @Override
    public void insertEvidence(String id, String controlId, String evidenceType, String sourceService,
                               String sourceReference, String contentLocation, String checksum,
                               long collectedAt, String collectedBy) {
        jdbcTemplate.update(
                "INSERT INTO identity_evidence (id, control_id, evidence_type, source_service, " +
                "source_reference, content_location, checksum, collected_at, collected_by) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
                id, controlId, evidenceType, sourceService, sourceReference,
                contentLocation, checksum, collectedAt, collectedBy
        );
    }

    @Override
    public List<Map<String, Object>> findEvidenceByControl(String controlId) {
        return jdbcTemplate.queryForList(
                "SELECT * FROM identity_evidence WHERE control_id = ? ORDER BY collected_at DESC",
                controlId);
    }

    // ── Assessment ───────────────────────────────────────────────

    @Override
    public void insertAssessment(String id, String controlId, String assessedBy, long assessmentDate,
                                 String result, String findingsSummary) {
        jdbcTemplate.update(
                "INSERT INTO identity_control_assessment (id, control_id, assessed_by, assessment_date, " +
                "result, findings_summary) VALUES (?, ?, ?, ?, ?, ?)",
                id, controlId, assessedBy, assessmentDate, result, findingsSummary
        );
    }

    @Override
    public List<Map<String, Object>> findAssessmentsByControl(String controlId) {
        return jdbcTemplate.queryForList(
                "SELECT * FROM identity_control_assessment WHERE control_id = ? ORDER BY assessment_date DESC",
                controlId);
    }
}
