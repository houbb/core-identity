package com.github.houbb.core.identity.application.port;

import com.github.houbb.core.identity.application.domain.PlatformOperator;

import java.util.Optional;

/**
 * Repository for identity_platform_operator.
 */
public interface PlatformOperatorRepository {

    void save(PlatformOperator operator);

    Optional<PlatformOperator> findByUserId(String userId);

    void update(PlatformOperator operator);

    void disable(String id, long disabledAt, long version);
}