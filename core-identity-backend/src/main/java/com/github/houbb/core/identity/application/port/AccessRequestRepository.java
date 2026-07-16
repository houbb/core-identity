package com.github.houbb.core.identity.application.port;

import com.github.houbb.core.identity.application.domain.AccessRequest;

import java.util.List;
import java.util.Optional;

/**
 * Repository for identity_access_request.
 */
public interface AccessRequestRepository {

    void save(AccessRequest request);

    Optional<AccessRequest> findById(String id);

    List<AccessRequest> findByRequesterId(String requesterUserId);

    List<AccessRequest> findByOrganizationId(String organizationId);

    List<AccessRequest> findByStatus(String status);

    List<AccessRequest> findByOrgAndStatus(String organizationId, String status);

    List<AccessRequest> findPendingByOrg(String organizationId);

    void update(AccessRequest request);

    void updateStatus(String id, String status, long completedAt, long now, long version);
}
