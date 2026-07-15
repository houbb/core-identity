package com.github.houbb.core.identity.application.service;

import com.github.houbb.core.identity.application.domain.Organization;

import java.util.List;
import java.util.Map;

/**
 * Organization management service — creation, query, context, lifecycle.
 */
public interface OrganizationService {

    /**
     * Create a TEAM organization with owner membership, built-in roles, audit, and outbox in one transaction.
     */
    Organization createTeamOrg(String name, String description, String ownerUserId);

    /**
     * Update organization profile.
     */
    Organization updateOrganization(String organizationId, String name, String description);

    /**
     * Get organization detail.
     */
    Organization getOrganization(String organizationId);

    /**
     * List all organizations the user belongs to (sorted by last accessed).
     */
    List<Organization> getMyOrganizations(String userId);

    /**
     * Get the count of my active organizations.
     */
    int getMyOrganizationCount(String userId);

    /**
     * Record organization switch for session preference.
     */
    void switchOrganization(String userId, String organizationId);

    /**
     * Get the current organization context for a user (validates membership).
     */
    OrganizationContext getOrganizationContext(String userId, String organizationId);

    /**
     * Transfer ownership to another member.
     */
    void transferOwnership(String organizationId, String currentOwnerId,
                           String newOwnerUserId, String password);

    /**
     * Request organization deletion (enters PENDING_DELETION status).
     */
    void requestDeletion(String organizationId, String userId, String password);

    /**
     * Cancel a pending deletion request.
     */
    void cancelDeletion(String organizationId, String userId);

    /**
     * Suspend an organization (admin action).
     */
    void suspendOrganization(String organizationId, String reason, String operatorId);

    /**
     * Reactivate a suspended organization (admin action).
     */
    void reactivateOrganization(String organizationId, String operatorId);

    /**
     * Generate a unique slug from a name.
     */
    String generateSlug(String name);

    /**
     * Organization context result.
     */
    record OrganizationContext(
            String organizationId,
            String membershipId,
            String status,
            List<String> roleIds
    ) {}

    /**
     * Create Team Org command.
     */
    record CreateTeamOrgCommand(String name, String description, String ownerUserId) {}

    /**
     * Organization detail DTO.
     */
    record OrganizationDTO(
            String id,
            String organizationType,
            String name,
            String slug,
            String description,
            String status,
            String ownerUserId,
            String logoObjectId,
            int memberCount,
            String currentUserRole,
            long authorizationVersion,
            long createdAt
    ) {}
}