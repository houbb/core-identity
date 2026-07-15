package com.github.houbb.core.identity.application.service;

import com.github.houbb.core.identity.application.domain.Invitation;
import com.github.houbb.core.identity.application.domain.Membership;

import java.util.List;

/**
 * Invitation management service.
 */
public interface InvitationService {

    Invitation createInvitation(String organizationId, String email, List<String> roleIds, String inviterId);

    Invitation resolveInvitation(String token);

    Membership acceptInvitation(String token, String userId);

    void declineInvitation(String token, String userId);

    void resendInvitation(String organizationId, String invitationId, String operatorId);

    void revokeInvitation(String organizationId, String invitationId, String operatorId);

    List<Invitation> getInvitations(String organizationId);
}