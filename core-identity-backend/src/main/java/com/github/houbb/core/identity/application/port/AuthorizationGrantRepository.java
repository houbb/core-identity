package com.github.houbb.core.identity.application.port;

import com.github.houbb.core.identity.application.domain.AuthorizationGrant;
import java.util.List;
import java.util.Optional;

public interface AuthorizationGrantRepository {
    void save(AuthorizationGrant grant);
    Optional<AuthorizationGrant> findById(String id);
    Optional<AuthorizationGrant> findByUserAndClient(String userId, String clientId);
    List<AuthorizationGrant> findByUserId(String userId);
    void update(AuthorizationGrant grant);
}
