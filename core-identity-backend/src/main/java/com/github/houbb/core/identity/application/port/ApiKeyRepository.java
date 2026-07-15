package com.github.houbb.core.identity.application.port;

import com.github.houbb.core.identity.application.domain.ApiKey;
import java.util.List;
import java.util.Optional;

public interface ApiKeyRepository {
    void save(ApiKey key);
    Optional<ApiKey> findById(String id);
    Optional<ApiKey> findByPrefix(String prefix);
    Optional<ApiKey> findByHash(String hash);
    List<ApiKey> findByOwner(String ownerType, String ownerId);
    List<ApiKey> findByOrg(String orgId);
    void update(ApiKey key);
    void revoke(String id, long now);
}
