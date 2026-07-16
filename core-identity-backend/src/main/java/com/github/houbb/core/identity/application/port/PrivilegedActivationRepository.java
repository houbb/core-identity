package com.github.houbb.core.identity.application.port;

import com.github.houbb.core.identity.application.domain.PrivilegedActivation;

import java.util.List;
import java.util.Optional;

/**
 * Repository for identity_privileged_activation.
 */
public interface PrivilegedActivationRepository {

    void save(PrivilegedActivation activation);

    Optional<PrivilegedActivation> findById(String id);

    List<PrivilegedActivation> findByUserId(String userId);

    List<PrivilegedActivation> findActiveByUserId(String userId);

    Optional<PrivilegedActivation> findActiveByUserIdAndRole(String userId, String roleId);

    List<PrivilegedActivation> findExpiringActivations(long beforeTimestamp);

    void update(PrivilegedActivation activation);

    void end(String id, long endedAt, long now, long version);
}