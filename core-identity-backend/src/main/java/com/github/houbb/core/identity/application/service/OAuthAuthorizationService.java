package com.github.houbb.core.identity.application.service;

import com.github.houbb.core.identity.application.domain.*;
import com.github.houbb.core.identity.application.port.*;
import com.github.houbb.core.identity.infrastructure.util.TokenUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.*;

public class OAuthAuthorizationService {
    private static final Logger log = LoggerFactory.getLogger(OAuthAuthorizationService.class);
    private final AuthorizationCodeRepository codeRepo;
    private final OAuthClientService clientService;
    private final OAuthTokenService tokenService;
    private final AuthorizationGrantRepository grantRepo;
    private final RefreshTokenFamilyRepository familyRepo;
    private final RefreshTokenRepository rtRepo;
    private final TokenRevocationRepository revRepo;
    private final ServiceAccountRepository saRepo;
    private final ScopeRepository scopeRepo;
    private final int authCodeTtl;
    private final int accessTokenTtl;
    private final int refreshTokenTtl;
    private final int idTokenTtl;

    public OAuthAuthorizationService(AuthorizationCodeRepository codeRepo, OAuthClientService clientService,
                                     OAuthTokenService tokenService, AuthorizationGrantRepository grantRepo,
                                     RefreshTokenFamilyRepository familyRepo, RefreshTokenRepository rtRepo,
                                     TokenRevocationRepository revRepo, ServiceAccountRepository saRepo,
                                     ScopeRepository scopeRepo,
                                     int authCodeTtl, int accessTokenTtl, int refreshTokenTtl, int idTokenTtl) {
        this.codeRepo=codeRepo; this.clientService=clientService; this.tokenService=tokenService;
        this.grantRepo=grantRepo; this.familyRepo=familyRepo; this.rtRepo=rtRepo; this.revRepo=revRepo;
        this.saRepo=saRepo; this.scopeRepo=scopeRepo;
        this.authCodeTtl=authCodeTtl; this.accessTokenTtl=accessTokenTtl; this.refreshTokenTtl=refreshTokenTtl; this.idTokenTtl=idTokenTtl;
    }

    public record AuthorizeResult(String redirectUrl) {}
    public record TokenResponse(String accessToken, String tokenType, int expiresIn, String refreshToken, String idToken, String scope) {}
    public record IntrospectResponse(boolean active, String subject, String clientId, String scope, long exp, long iat) {}

    /** Create authorization code, return redirect URL with code */
    @Transactional
    public AuthorizeResult authorize(String clientId, String redirectUri, String responseType, String scope,
                                      String state, String codeChallenge, String codeChallengeMethod, String nonce,
                                      String userId, String organizationId, String audience) {
        OAuthClient client = clientService.getClient(clientId);
        if (!"ACTIVE".equals(client.getStatus())) throw new OAuthException("Client not active");
        if ("S256".equals(codeChallengeMethod) && (codeChallenge == null || codeChallenge.length() < 43)) throw new OAuthException("Invalid PKCE challenge");

        // Scope-audience cross-validation
        if (scope != null && !scope.isEmpty() && audience != null && !audience.isEmpty()) {
            String[] requestedScopes = scope.split("\\s+");
            for (String scopeCode : requestedScopes) {
                if (scopeCode.isEmpty()) continue;
                scopeRepo.findByCode(scopeCode).ifPresent(s -> {
                    if (s.getAudienceCode() != null && !s.getAudienceCode().equals(audience)) {
                        throw new OAuthException("Scope '" + scopeCode + "' is not valid for audience '" + audience + "'");
                    }
                });
            }
        }

        long now = System.currentTimeMillis();
        String rawCode = generateRandomCode();
        String codeHash = TokenUtils.hashToken(rawCode);

        AuthorizationCode ac = new AuthorizationCode();
        ac.setId(UUID.randomUUID().toString());
        ac.setCodeHash(codeHash);
        ac.setClientId(client.getId());
        ac.setUserId(userId);
        ac.setOrganizationId(organizationId);
        ac.setRedirectUri(redirectUri);
        ac.setAudience(audience);
        ac.setScopesJson(scope);
        ac.setCodeChallenge(codeChallenge);
        ac.setCodeChallengeMethod(codeChallengeMethod);
        ac.setNonce(nonce);
        ac.setStatus("ACTIVE");
        ac.setExpiresAt(now + authCodeTtl * 1000L);
        ac.setCreatedAt(now);
        ac.setVersion(1);
        codeRepo.save(ac);

        String separator = redirectUri.contains("?") ? "&" : "?";
        String redirectUrl = redirectUri + separator + "code=" + rawCode;
        if (state != null) redirectUrl += "&state=" + state;
        return new AuthorizeResult(redirectUrl);
    }

    /** Exchange code for tokens, or refresh token flow, or client credentials flow */
    @Transactional
    public TokenResponse token(String grantType, String code, String clientId, String clientSecret,
                                String redirectUri, String codeVerifier, String refreshToken,
                                String scope, String audience) {
        // Validate client
        OAuthClient client = clientService.getClient(clientId);
        if ("CONFIDENTIAL".equals(client.getClientType()) && !clientService.validateClientSecret(clientId, clientSecret))
            throw new OAuthException("Invalid client credentials");

        long now = System.currentTimeMillis();

        if ("authorization_code".equals(grantType)) {
            return tokenViaAuthCode(code, codeVerifier, redirectUri, client, now);
        } else if ("refresh_token".equals(grantType)) {
            return tokenViaRefresh(refreshToken, client, now);
        } else if ("client_credentials".equals(grantType)) {
            return tokenViaClientCredentials(client, scope, audience, now);
        }
        throw new OAuthException("Unsupported grant_type: " + grantType);
    }

    private TokenResponse tokenViaAuthCode(String code, String codeVerifier, String redirectUri,
                                            OAuthClient client, long now) {
        String codeHash = TokenUtils.hashToken(code);
        AuthorizationCode ac = codeRepo.findByCodeHash(codeHash)
                .orElseThrow(() -> new OAuthException("Invalid authorization code"));
        if (!"ACTIVE".equals(ac.getStatus())) throw new OAuthException("Code already used");
        if (ac.getExpiresAt() < now) throw new OAuthException("Code expired");
        if (!ac.getClientId().equals(client.getId())) throw new OAuthException("Code issued to different client");
        if (redirectUri != null && !ac.getRedirectUri().equals(redirectUri)) throw new OAuthException("redirect_uri mismatch");

        // PKCE verification
        if (ac.getCodeChallenge() != null) {
            if (codeVerifier == null) throw new OAuthException("code_verifier required");
            if (!verifyPkce(ac.getCodeChallenge(), codeVerifier, ac.getCodeChallengeMethod()))
                throw new OAuthException("PKCE verification failed");
        }

        codeRepo.markUsed(ac.getId(), now);

        // Create or find grant
        AuthorizationGrant grant = grantRepo.findByUserAndClient(ac.getUserId(), client.getId())
                .orElse(null);
        if (grant == null) {
            grant = new AuthorizationGrant();
            grant.setId(UUID.randomUUID().toString());
            grant.setClientId(client.getId());
            grant.setUserId(ac.getUserId());
            grant.setOrganizationId(ac.getOrganizationId());
            grant.setStatus("ACTIVE");
            grant.setFirstGrantedAt(now);
            grant.setCreatedAt(now);
            grant.setUpdatedAt(now);
            grant.setVersion(1);
            grantRepo.save(grant);
        } else {
            grant.setLastUsedAt(now);
            grant.setUpdatedAt(now);
            grantRepo.update(grant);
        }

        // Use audience from the auth code, or fall back to a default
        String effectiveAudience = ac.getAudience() != null ? ac.getAudience() : "core-identity";
        return issueTokens(ac.getUserId(), "user", effectiveAudience, client.getClientId(),
                ac.getOrganizationId(), ac.getScopesJson(), ac.getNonce(), grant.getId(), now);
    }

    private TokenResponse tokenViaRefresh(String rawRefreshToken, OAuthClient client, long now) {
        String rtHash = TokenUtils.hashToken(rawRefreshToken);
        RefreshToken rt = rtRepo.findByTokenHash(rtHash)
                .orElseThrow(() -> new OAuthException("Invalid refresh token"));
        if (!"ACTIVE".equals(rt.getStatus())) throw new OAuthException("Refresh token revoked");
        if (rt.getExpiresAt() < now) throw new OAuthException("Refresh token expired");

        RefreshTokenFamily family = familyRepo.findById(rt.getFamilyId())
                .orElseThrow(() -> new OAuthException("Refresh token family not found"));
        if (!"ACTIVE".equals(family.getStatus())) {
            // Reuse detection: if family is revoked but this RT was still being used, that's misuse
            revokeEntireFamily(rt.getFamilyId(), "Token family compromised — all tokens revoked");
            throw new OAuthException("Refresh token reuse detected. All tokens revoked.");
        }

        // Rotation: mark old RT as used, create new one
        rt.setStatus("ROTATED");
        rt.setUsedAt(now);
        rtRepo.update(rt);

        AuthorizationGrant grant = grantRepo.findById(family.getGrantId()).orElse(null);
        String orgId = grant != null ? grant.getOrganizationId() : null;
        return issueTokens(family.getUserId(), "user", "core-identity", client.getClientId(),
                orgId, "openid profile", null, family.getGrantId(), now);
    }

    private TokenResponse tokenViaClientCredentials(OAuthClient client, String scope, String audience, long now) {
        // Validate Service Account: the client's owner should be a Service Account for client_credentials
        String ownerId = client.getOwnerId();
        String ownerType = client.getOwnerType();

        if (!"SERVICE_ACCOUNT".equals(ownerType)) {
            throw new OAuthException("Client credentials grant requires a SERVICE_ACCOUNT owner. " +
                    "This client is owned by: " + ownerType);
        }

        ServiceAccount sa = saRepo.findById(ownerId)
                .orElseThrow(() -> new OAuthException("Service account not found: " + ownerId));
        if (!"ACTIVE".equals(sa.getStatus())) {
            throw new OAuthException("Service account is not active: " + sa.getId());
        }

        // Use provided scope/audience or defaults
        String effectiveScope = (scope != null && !scope.isEmpty()) ? scope : "system";
        String effectiveAudience = (audience != null && !audience.isEmpty()) ? audience : "core-identity";

        // Scope-audience cross-validation
        if (scope != null && !scope.isEmpty() && audience != null && !audience.isEmpty()) {
            String[] requestedScopes = scope.split("\\s+");
            for (String scopeCode : requestedScopes) {
                if (scopeCode.isEmpty()) continue;
                scopeRepo.findByCode(scopeCode).ifPresent(s -> {
                    if (s.getAudienceCode() != null && !s.getAudienceCode().equals(audience)) {
                        throw new OAuthException("Scope '" + scopeCode + "' is not valid for audience '" + audience + "'");
                    }
                });
            }
        }

        // Issue an access token where the subject is the service account ID,
        // subject_type is "service_account", and audience is the requested one
        String accessToken = tokenService.issueAccessToken(
                sa.getId(),           // subject = service account ID
                "service_account",    // subject_type
                effectiveAudience,    // audience
                client.getClientId(),
                sa.getOrganizationId(),
                effectiveScope,
                accessTokenTtl);

        // Update last used timestamp
        sa.setLastUsedAt(now);
        saRepo.update(sa);

        return new TokenResponse(accessToken, "Bearer", accessTokenTtl, null, null, effectiveScope);
    }

    private TokenResponse issueTokens(String subject, String subjectType, String audience, String clientId,
                                       String orgId, String scopes, String nonce, String grantId, long now) {
        String at = tokenService.issueAccessToken(subject, subjectType, audience, clientId, orgId, scopes, accessTokenTtl);
        String idt = null;
        if (scopes != null && scopes.contains("openid")) {
            idt = tokenService.issueIdToken(subject, audience, nonce, now / 1000, idTokenTtl);
        }

        // Issue refresh token
        String rawRt = generateRandomToken();
        String rtHash = TokenUtils.hashToken(rawRt);
        RefreshTokenFamily rtf = new RefreshTokenFamily();
        rtf.setId(UUID.randomUUID().toString());
        rtf.setGrantId(grantId != null ? grantId : UUID.randomUUID().toString());
        rtf.setClientId(clientId);
        rtf.setUserId(subject);
        rtf.setStatus("ACTIVE");
        rtf.setCreatedAt(now);
        familyRepo.save(rtf);

        RefreshToken rt = new RefreshToken();
        rt.setId(UUID.randomUUID().toString());
        rt.setFamilyId(rtf.getId());
        rt.setTokenHash(rtHash);
        rt.setStatus("ACTIVE");
        rt.setExpiresAt(now + refreshTokenTtl * 1000L);
        rt.setCreatedAt(now);
        rt.setVersion(1);
        rtRepo.save(rt);

        return new TokenResponse(at, "Bearer", accessTokenTtl, rawRt, idt, scopes);
    }

    // --- Convenience overload for backward compatibility with existing callers ---

    /**
     * Convenience overload that preserves the old 7-parameter signature for callers
     * that don't need scope/audience (authorization_code, refresh_token flows).
     */
    @Transactional
    public TokenResponse token(String grantType, String code, String clientId, String clientSecret,
                                String redirectUri, String codeVerifier, String refreshToken) {
        return token(grantType, code, clientId, clientSecret, redirectUri, codeVerifier, refreshToken, null, null);
    }

    /**
     * Convenience overload that preserves the old 10-parameter authorize signature
     * for callers that don't need audience.
     */
    @Transactional
    public AuthorizeResult authorize(String clientId, String redirectUri, String responseType, String scope,
                                      String state, String codeChallenge, String codeChallengeMethod, String nonce,
                                      String userId, String organizationId) {
        return authorize(clientId, redirectUri, responseType, scope, state, codeChallenge, codeChallengeMethod, nonce,
                userId, organizationId, null);
    }

    // --- End convenience overloads ---

    @Transactional
    public void revokeGrant(String grantId) {
        AuthorizationGrant grant = grantRepo.findById(grantId).orElse(null);
        if (grant != null) {
            long now = System.currentTimeMillis();
            grant.setStatus("REVOKED");
            grant.setRevokedAt(now);
            grant.setUpdatedAt(now);
            grantRepo.update(grant);
        }
        // Revoke associated families
        List<RefreshTokenFamily> families = familyRepo.findByGrantId(grantId);
        for (var f : families) revokeEntireFamily(f.getId(), "Grant revoked");
    }

    private void revokeEntireFamily(String familyId, String reason) {
        RefreshTokenFamily family = familyRepo.findById(familyId).orElse(null);
        if (family == null) return;
        long now = System.currentTimeMillis();
        family.setStatus("REVOKED");
        family.setRevokedReason(reason);
        family.setRevokedAt(now);
        familyRepo.update(family);

        List<RefreshToken> tokens = rtRepo.findByFamilyId(familyId);
        for (var rt : tokens) {
            if ("ACTIVE".equals(rt.getStatus())) {
                rt.setStatus("REVOKED");
                rt.setUsedAt(now);
                rtRepo.update(rt);
            }
        }
    }

    public IntrospectResponse introspect(String token) {
        try {
            var claims = tokenService.validateToken(token);
            long nowSec = System.currentTimeMillis() / 1000;
            boolean active = claims.exp() > nowSec && !revRepo.isRevoked(claims.jti());
            return new IntrospectResponse(active, claims.subject(), claims.clientId(), claims.scope(), claims.exp(), claims.iat());
        } catch (Exception e) {
            return new IntrospectResponse(false, null, null, null, 0, 0);
        }
    }

    public TokenResponse validateToken(String token) {
        try {
            var claims = tokenService.validateToken(token);
            return new TokenResponse(token, "Bearer", (int)(claims.exp() - System.currentTimeMillis() / 1000), null, null, claims.scope());
        } catch (Exception e) {
            throw new OAuthException("Invalid token: " + e.getMessage());
        }
    }
    public List<AuthorizationGrant> getUserGrants(String userId) { return grantRepo.findByUserId(userId); }

    // --- helpers ---
    private String generateRandomCode() { return "oa_" + UUID.randomUUID().toString().replace("-",""); }
    private String generateRandomToken() { return "ort_" + UUID.randomUUID().toString().replace("-","") + new SecureRandom().nextLong(); }
    private boolean verifyPkce(String challenge, String verifier, String method) {
        try {
            byte[] hash;
            if ("S256".equals(method)) {
                hash = MessageDigest.getInstance("SHA-256").digest(verifier.getBytes(StandardCharsets.US_ASCII));
            } else {
                hash = verifier.getBytes(StandardCharsets.US_ASCII);
            }
            String computed = Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
            return challenge.equals(computed);
        } catch (Exception e) {
            return false;
        }
    }
    public static class OAuthException extends RuntimeException { public OAuthException(String m) { super(m); } }
}