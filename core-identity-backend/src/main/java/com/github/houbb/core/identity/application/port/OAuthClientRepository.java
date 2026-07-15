package com.github.houbb.core.identity.application.port;

import com.github.houbb.core.identity.application.domain.OAuthClient;
import java.util.List;
import java.util.Optional;

public interface OAuthClientRepository {
    void save(OAuthClient client);
    Optional<OAuthClient> findById(String id);
    Optional<OAuthClient> findByClientId(String clientId);
    List<OAuthClient> findByOwner(String ownerType, String ownerId);
    List<OAuthClient> findAll();
    List<OAuthClient> findByStatus(String status);
    void update(OAuthClient client);
    void updateStatus(String id, String status, String reviewStatus, long now, long version);
}