package com.github.houbb.core.identity.application.port;

import java.util.List;
import java.util.Map;

/**
 * Repository for SoD-related tables: identity_sod_policy_item, identity_sod_conflict, identity_sod_exception.
 */
public interface SodDataRepository {

    // Policy items
    void insertPolicyItem(String id, String policyId, String leftEntitlementId, String rightEntitlementId, String riskLevel);

    List<Map<String, Object>> findPolicyItemsByStatus(String policyStatus);

    // Conflicts
    void insertConflict(String id, String policyId, String subjectId, String leftGrantId, String rightGrantId, long detectedAt);

    List<Map<String, Object>> findOpenConflictsByPolicyAndSubject(String policyId, String subjectId);

    List<Map<String, Object>> findConflictsByOrg(String organizationId);

    void updateConflictStatus(String id, String status, String resolution, long resolvedAt);

    // Exceptions
    void insertException(String id, String conflictId, String reason, String compensatingControl,
                         String approvedBy, long validFrom, long expiresAt);

    // Grants (for SoD detection)
    List<Map<String, Object>> findActiveGrantsBySubject(String subjectId);
}