package com.github.houbb.core.identity.application.port;

import com.github.houbb.core.identity.application.domain.ProvisioningJob;

import java.util.List;
import java.util.Optional;

/**
 * Repository for identity_provisioning_job.
 */
public interface ProvisioningJobRepository {
    void save(ProvisioningJob job);
    Optional<ProvisioningJob> findById(String id);
    List<ProvisioningJob> findByOrganizationId(String organizationId);
    List<ProvisioningJob> findByConnectionId(String connectionId);
    List<ProvisioningJob> findByStatus(String status);
    void update(ProvisioningJob job);
    void updateStatus(String id, String status, long now, long version);
}
