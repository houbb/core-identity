package com.github.houbb.core.identity.application.port;

import java.util.List;
import java.util.Map;

/**
 * Repository for compliance tables: identity_compliance_control, identity_compliance_framework,
 * identity_control_mapping, identity_control_assessment, identity_control_finding, identity_evidence.
 */
public interface ComplianceDataRepository {

    // Control
    void insertControl(String id, String controlCode, String name, String description,
                       String controlType, String ownerUserId, String frequency, long now);

    List<Map<String, Object>> findAllControls();

    void updateControlStatus(String id, String status, long now);

    // Framework
    void insertFramework(String id, String frameworkCode, String name, String version, String publisher, long now);

    // Mapping
    void upsertControlMapping(String controlId, String frameworkId, String requirementCode);

    // Finding
    void insertFinding(String id, String controlId, String title, String description, String severity,
                       String ownerUserId, long dueAt, long now);

    List<Map<String, Object>> findOpenFindingsWithControls();

    void updateFindingResolved(String id, long now);

    // Evidence
    void insertEvidence(String id, String controlId, String evidenceType, String sourceService,
                        String sourceReference, String contentLocation, String checksum,
                        long collectedAt, String collectedBy);

    List<Map<String, Object>> findEvidenceByControl(String controlId);

    // Assessment
    void insertAssessment(String id, String controlId, String assessedBy, long assessmentDate,
                          String result, String findingsSummary);

    List<Map<String, Object>> findAssessmentsByControl(String controlId);
}