package com.github.houbb.core.identity.application.service;

import com.github.houbb.core.identity.application.port.ComplianceDataRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * 合规与证据服务 — 管理合规控制、框架映射、评估、Finding 和证据。
 */
public class ComplianceService {

    private static final Logger log = LoggerFactory.getLogger(ComplianceService.class);

    private final ComplianceDataRepository repo;

    public ComplianceService(ComplianceDataRepository repo) {
        this.repo = repo;
    }

    // ========== Control ==========

    @Transactional
    public Map<String, Object> createControl(String controlCode, String name, String description,
                                              String controlType, String ownerUserId, String frequency) {
        long now = System.currentTimeMillis();
        String id = UUID.randomUUID().toString();
        repo.insertControl(id, controlCode, name, description, controlType, ownerUserId, frequency, now);
        return Map.of("id", id, "controlCode", controlCode, "status", "PLANNED");
    }

    public List<Map<String, Object>> listControls() {
        return repo.findAllControls();
    }

    @Transactional
    public void updateControlStatus(String controlId, String status) {
        long now = System.currentTimeMillis();
        repo.updateControlStatus(controlId, status, now);
    }

    // ========== Framework ==========

    @Transactional
    public Map<String, Object> importFramework(String frameworkCode, String name, String version, String publisher) {
        long now = System.currentTimeMillis();
        String id = UUID.randomUUID().toString();
        repo.insertFramework(id, frameworkCode, name, version, publisher, now);
        return Map.of("id", id, "frameworkCode", frameworkCode, "name", name);
    }

    @Transactional
    public void mapControlToFramework(String controlId, String frameworkId, String requirementCode) {
        repo.upsertControlMapping(controlId, frameworkId, requirementCode);
    }

    // ========== Finding ==========

    @Transactional
    public Map<String, Object> createFinding(String controlId, String title, String description,
                                              String severity, String ownerUserId, long dueAt) {
        long now = System.currentTimeMillis();
        String id = UUID.randomUUID().toString();
        repo.insertFinding(id, controlId, title, description, severity, ownerUserId, dueAt, now);
        return Map.of("id", id, "title", title, "severity", severity, "status", "OPEN");
    }

    public List<Map<String, Object>> listOpenFindings() {
        return repo.findOpenFindingsWithControls();
    }

    @Transactional
    public void resolveFinding(String findingId, String resolution) {
        long now = System.currentTimeMillis();
        repo.updateFindingResolved(findingId, now);
    }

    // ========== Evidence ==========

    @Transactional
    public Map<String, Object> recordEvidence(String controlId, String evidenceType, String sourceService,
                                               String sourceReference, String contentLocation,
                                               String checksum, String collectedBy) {
        long now = System.currentTimeMillis();
        String id = UUID.randomUUID().toString();
        repo.insertEvidence(id, controlId, evidenceType, sourceService, sourceReference,
                contentLocation, checksum, now, collectedBy);
        return Map.of("id", id, "evidenceType", evidenceType, "status", "VALID");
    }

    public List<Map<String, Object>> listEvidenceByControl(String controlId) {
        return repo.findEvidenceByControl(controlId);
    }

    // ========== Assessment ==========

    @Transactional
    public Map<String, Object> createAssessment(String controlId, String assessedBy,
                                                 String result, String findingsSummary) {
        long now = System.currentTimeMillis();
        String id = UUID.randomUUID().toString();
        repo.insertAssessment(id, controlId, assessedBy, now, result, findingsSummary);
        return Map.of("id", id, "result", result, "assessmentDate", now);
    }

    public List<Map<String, Object>> listAssessmentsByControl(String controlId) {
        return repo.findAssessmentsByControl(controlId);
    }

    public static class ServiceException extends RuntimeException {
        private final String errorCode;
        public ServiceException(String errorCode, String message) {
            super(message);
            this.errorCode = errorCode;
        }
        public String getErrorCode() { return errorCode; }
    }
}
