package com.github.houbb.core.identity.application.port;

import com.github.houbb.core.identity.application.domain.Invitation;

import java.util.List;
import java.util.Optional;

/**
 * Repository for identity_invitation.
 */
public interface InvitationRepository {

    void save(Invitation invitation);

    Optional<Invitation> findById(String id);

    Optional<Invitation> findByTokenHash(String tokenHash);

    Optional<Invitation> findByOrgAndEmailAndPending(String organizationId, String emailNormalized);

    List<Invitation> findByOrgId(String organizationId);

    List<Invitation> findByOrgAndStatus(String organizationId, String status);

    void update(Invitation invitation);

    void updateStatus(String id, String status, Long actionAt, long now, long version);
}