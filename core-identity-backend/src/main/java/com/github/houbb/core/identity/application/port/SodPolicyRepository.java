package com.github.houbb.core.identity.application.port;

import com.github.houbb.core.identity.application.domain.SodPolicy;

import java.util.List;
import java.util.Optional;

public interface SodPolicyRepository {
    void save(SodPolicy policy);
    Optional<SodPolicy> findById(String id);
    List<SodPolicy> findByOrgId(String organizationId);
    List<SodPolicy> findActiveByOrgId(String organizationId);
    void update(SodPolicy policy);
    void updateStatus(String id, String status, long now, long version);
}