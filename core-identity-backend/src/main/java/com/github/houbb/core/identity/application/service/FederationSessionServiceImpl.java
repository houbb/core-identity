package com.github.houbb.core.identity.application.service;

import com.github.houbb.core.identity.application.domain.FederatedSession;
import com.github.houbb.core.identity.application.port.FederatedSessionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

/**
 * Federation Session Service implementation — links Core sessions with upstream IdP sessions.
 */
public class FederationSessionServiceImpl implements FederationSessionService {

    private static final Logger log = LoggerFactory.getLogger(FederationSessionServiceImpl.class);

    private final FederatedSessionRepository fedSessionRepo;

    public FederationSessionServiceImpl(FederatedSessionRepository fedSessionRepo) {
        this.fedSessionRepo = fedSessionRepo;
    }

    @Override
    public void createFederatedSession(String sessionId, String connectionId, String externalIdentityId,
                                        String upstreamSessionId, String upstreamSubject,
                                        long upstreamAuthTime, String upstreamAcr, String upstreamAmrJson, long now) {
        FederatedSession fs = new FederatedSession();
        fs.setId(UUID.randomUUID().toString());
        fs.setSessionId(sessionId);
        fs.setConnectionId(connectionId);
        fs.setExternalIdentityId(externalIdentityId);
        fs.setUpstreamSessionId(upstreamSessionId);
        fs.setUpstreamSubject(upstreamSubject);
        fs.setUpstreamAuthTime(upstreamAuthTime);
        fs.setUpstreamAcr(upstreamAcr);
        fs.setUpstreamAmrJson(upstreamAmrJson);
        fs.setLogoutStatus("NONE");
        fs.setCreatedAt(now);
        fs.setUpdatedAt(now);
        fs.setVersion(1);
        fedSessionRepo.save(fs);

        log.debug("Federated session created: coreSession={}, upstreamSubject={}, connection={}",
                sessionId, upstreamSubject, connectionId);
    }

    @Override
    public void revokeByExternalIdentity(String externalIdentityId, long now) {
        fedSessionRepo.findByExternalIdentityId(externalIdentityId).ifPresent(fs -> {
            fedSessionRepo.updateLogoutStatus(fs.getId(), "REVOKED", now, fs.getVersion());
            log.info("Federated session revoked: extIdentity={}", externalIdentityId);
        });
    }
}