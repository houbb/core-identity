package com.github.houbb.core.identity.application.port;

import com.github.houbb.core.identity.application.domain.ProvisioningLog;

import java.util.List;

/**
 * Repository for identity_provisioning_log.
 */
public interface ProvisioningLogRepository {
    void save(ProvisioningLog log);
    List<ProvisioningLog> findByJobId(String jobId);
    List<ProvisioningLog> findByConnectionIdAndResult(String connectionId, String result);
}
