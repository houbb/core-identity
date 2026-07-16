package com.github.houbb.core.identity.application.port;

import com.github.houbb.core.identity.application.domain.Entitlement;

import java.util.List;
import java.util.Optional;

/**
 * Repository for identity_entitlement.
 */
public interface EntitlementRepository {

    void save(Entitlement entitlement);

    Optional<Entitlement> findById(String id);

    Optional<Entitlement> findByCode(String code);

    List<Entitlement> findByOrganizationId(String organizationId);

    List<Entitlement> findByOrganizationIdAndType(String organizationId, String entitlementType);

    List<Entitlement> findByTargetId(String targetId);

    List<Entitlement> findAllActive();

    void update(Entitlement entitlement);

    void updateStatus(String id, String status, long now, long version);
}
