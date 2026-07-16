package com.github.houbb.core.identity.application.port;

import com.github.houbb.core.identity.application.domain.JitPolicy;

import java.util.Optional;

/**
 * Repository for identity_jit_policy.
 */
public interface JitPolicyRepository {
    void save(JitPolicy policy);
    Optional<JitPolicy> findById(String id);
    Optional<JitPolicy> findByConnectionId(String connectionId);
    void update(JitPolicy policy);
}
