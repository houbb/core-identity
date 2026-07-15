package com.github.houbb.core.identity.application.port;

import com.github.houbb.core.identity.application.domain.Audience;

import java.util.List;
import java.util.Optional;

/**
 * Repository for identity_audience.
 */
public interface AudienceRepository {

    void save(Audience audience);

    Optional<Audience> findById(String id);

    Optional<Audience> findByCode(String audienceCode);

    List<Audience> findAllActive();

    List<Audience> findAll();

    void update(Audience audience);

    void updateStatus(String id, String status, long now, long version);
}