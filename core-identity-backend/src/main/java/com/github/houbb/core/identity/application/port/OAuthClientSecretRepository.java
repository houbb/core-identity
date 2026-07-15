package com.github.houbb.core.identity.application.port;

import com.github.houbb.core.identity.application.domain.OAuthClientSecret;
import java.util.List;
import java.util.Optional;

public interface OAuthClientSecretRepository {
    void save(OAuthClientSecret secret);
    Optional<OAuthClientSecret> findById(String id);
    List<OAuthClientSecret> findByClientId(String clientId);
    Optional<OAuthClientSecret> findActiveByClientId(String clientId);
    void update(OAuthClientSecret secret);
    void revokeByClientId(String clientId, long now);
    void updateLastUsedAt(String id, long lastUsedAt);
}