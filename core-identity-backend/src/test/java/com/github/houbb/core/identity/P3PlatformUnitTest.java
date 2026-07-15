package com.github.houbb.core.identity;

import com.github.houbb.core.identity.application.domain.*;
import com.github.houbb.core.identity.application.port.*;
import com.github.houbb.core.identity.application.service.*;
import org.junit.jupiter.api.*;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("P3 Platform Identity Tests")
class P3PlatformUnitTest {

    // Stubs for all repositories
    private StubScopeRepository scopeRepo;
    private StubScopePermissionRepository scopePermRepo;
    private StubSigningKeyRepository signingKeyRepo;
    private StubOAuthClientRepository clientRepo;
    private StubOAuthClientSecretRepository secretRepo;
    private StubAuthorizationCodeRepository authCodeRepo;
    private StubAuthorizationGrantRepository grantRepo;
    private StubRefreshTokenFamilyRepository familyRepo;
    private StubRefreshTokenRepository rtRepo;
    private StubTokenRevocationRepository revRepo;
    private StubApiKeyRepository apiKeyRepo;
    private StubServiceAccountRepository saRepo;

    // Services
    private ScopeCatalogService scopeCatalogService;
    private SigningKeyManager signingKeyManager;
    private OAuthTokenService tokenService;
    private OAuthClientService clientService;
    private OAuthAuthorizationService oauthService;
    private ApiKeyService apiKeyService;
    private ServiceAccountService saService;

    @BeforeEach
    void setUp() {
        scopeRepo = new StubScopeRepository();
        scopePermRepo = new StubScopePermissionRepository();
        signingKeyRepo = new StubSigningKeyRepository();
        clientRepo = new StubOAuthClientRepository();
        secretRepo = new StubOAuthClientSecretRepository();
        authCodeRepo = new StubAuthorizationCodeRepository();
        grantRepo = new StubAuthorizationGrantRepository();
        familyRepo = new StubRefreshTokenFamilyRepository();
        rtRepo = new StubRefreshTokenRepository();
        revRepo = new StubTokenRevocationRepository();
        apiKeyRepo = new StubApiKeyRepository();
        saRepo = new StubServiceAccountRepository();

        scopeCatalogService = new ScopeCatalogServiceImpl(scopeRepo, scopePermRepo);
        signingKeyManager = new SigningKeyManager(signingKeyRepo, "dev-master-key-32-bytes-for-aes256!");
        tokenService = new OAuthTokenService(signingKeyManager, "http://localhost:8101");
        clientService = new OAuthClientService(clientRepo, secretRepo);

        oauthService = new OAuthAuthorizationService(authCodeRepo, clientService, tokenService,
                grantRepo, familyRepo, rtRepo, revRepo, saRepo, scopeRepo, 120, 900, 604800, 600);

        apiKeyService = new ApiKeyService(apiKeyRepo, tokenService, 900);
        saService = new ServiceAccountService(saRepo, tokenService);

        // Create a signing key so tokens can be issued
        SigningKey key = signingKeyManager.createKey();
        signingKeyManager.activateKey(key.getKeyId());
    }

    // ===== P3.1: Scope Catalog =====

    @Nested
    @DisplayName("P3.1 Scope Catalog")
    class ScopeCatalogTests {
        @Test
        @DisplayName("should sync scopes idempotently")
        void shouldSyncScopes() {
            var entries = List.of(
                    new ScopeCatalogService.ScopeManifestEntry("read", "Read Scope", "core-storage", "LOW", "Read data", "Read data"),
                    new ScopeCatalogService.ScopeManifestEntry("write", "Write Scope", "core-storage", "MEDIUM", "Write data", "Write data")
            );
            List<Scope> result = scopeCatalogService.syncScopes("core-storage", "1.0", entries, "test");
            assertEquals(2, result.size());
            assertTrue(result.stream().anyMatch(s -> "read".equals(s.getScopeCode())));

            // Second sync — idempotent
            List<Scope> result2 = scopeCatalogService.syncScopes("core-storage", "1.0", entries, "test");
            assertEquals(2, result2.size());
        }

        @Test
        @DisplayName("should return assignable scopes")
        void shouldReturnAssignableScopes() {
            var entries = List.of(
                    new ScopeCatalogService.ScopeManifestEntry("r1", "R1", "svc", "LOW", "c", "d"),
                    new ScopeCatalogService.ScopeManifestEntry("r2", "R2", "svc2", "HIGH", "c2", "d2")
            );
            scopeCatalogService.syncScopes("svc", "1.0", entries.subList(0, 1), "test");
            scopeCatalogService.syncScopes("svc2", "1.0", entries.subList(1, 2), "test");

            List<Scope> filtered = scopeCatalogService.getAssignableScopes("svc", null, null);
            assertEquals(1, filtered.size());
            assertEquals("r1", filtered.get(0).getScopeCode());
        }
    }

    // ===== P3.2: Signing Key & JWT =====

    @Nested
    @DisplayName("P3.2 Signing Key & JWT")
    class SigningKeyTests {
        @Test
        @DisplayName("should create and activate signing key")
        void shouldCreateAndActivateKey() {
            SigningKey key = signingKeyManager.createKey();
            assertNotNull(key.getKeyId());
            assertEquals("PENDING", key.getStatus());

            signingKeyManager.activateKey(key.getKeyId());
            assertEquals("sig-", key.getKeyId().substring(0, 4));
        }

        @Test
        @DisplayName("should issue and validate JWT access token")
        void shouldIssueAndValidateJwt() {
            String token = tokenService.issueAccessToken("user-123", "user", "core-identity",
                    "oc_abc", "org-456", "openid profile", 900);

            assertNotNull(token);
            assertTrue(token.split("\\.").length == 3);

            OAuthTokenService.TokenClaims claims = tokenService.validateToken(token);
            assertEquals("user-123", claims.subject());
            assertEquals("user", claims.subjectType());
            assertTrue(claims.audience().contains("core-identity"));
            assertEquals("openid profile", claims.scope());
        }

        @Test
        @DisplayName("should return JWKS keys")
        void shouldReturnJwksKeys() {
            List<Map<String, Object>> jwks = tokenService.getJwksKeysAsMap();
            assertFalse(jwks.isEmpty());
            Map<String, Object> jwk = jwks.get(0);
            assertEquals("RSA", jwk.get("kty"));
            assertNotNull(jwk.get("n"));
            assertNotNull(jwk.get("e"));
        }
    }

    // ===== P3.3: OAuth Client Registration =====

    @Nested
    @DisplayName("P3.3 OAuth Client")
    class OAuthClientTests {
        @Test
        @DisplayName("should create confidential client with secret")
        void shouldCreateClient() {
            var result = clientService.createClient("USER", "user-1", "My App", "Test app", "CONFIDENTIAL", null, "user-1");
            assertNotNull(result.client().getClientId());
            assertTrue(result.client().getClientId().startsWith("oc_"));
            assertNotNull(result.rawSecret());
            assertTrue(result.rawSecret().startsWith("ocs_"));
        }

        @Test
        @DisplayName("should validate client secret")
        void shouldValidateSecret() {
            var result = clientService.createClient("USER", "user-1", "My App", "Test", "CONFIDENTIAL", null, "user-1");
            assertTrue(clientService.validateClientSecret(result.client().getClientId(), result.rawSecret()));
            assertFalse(clientService.validateClientSecret(result.client().getClientId(), "wrong"));
        }

        @Test
        @DisplayName("should rotate client secret")
        void shouldRotateSecret() {
            var result = clientService.createClient("USER", "user-1", "My App", "Test", "CONFIDENTIAL", null, "user-1");
            String newSecret = clientService.rotateSecret(result.client().getClientId());
            assertNotNull(newSecret);
            assertNotEquals(result.rawSecret(), newSecret);
            assertTrue(clientService.validateClientSecret(result.client().getClientId(), newSecret));
        }
    }

    // ===== P3.4: Authorization Code + PKCE =====

    @Nested
    @DisplayName("P3.4 Authorization Code + PKCE")
    class AuthCodeTests {
        private String clientId;
        private String rawSecret;

        @BeforeEach
        void createClient() {
            var result = clientService.createClient("USER", "user-1", "Test App", "desc", "CONFIDENTIAL", null, "user-1");
            clientId = result.client().getClientId();
            rawSecret = result.rawSecret();
        }

        @Test
        @DisplayName("should generate authorization code redirect")
        void shouldGenerateAuthCode() {
            String codeChallenge = "E9Melhoa2OwvFrEMTJguCHaoeK1t8URWbuGJSstw-cM";
            var result = oauthService.authorize(clientId, "https://app.example.com/callback", "code",
                    "openid profile", "state123", codeChallenge, "S256", null, "user-123", null);
            assertNotNull(result.redirectUrl());
            assertTrue(result.redirectUrl().contains("code=oa_"));
            assertTrue(result.redirectUrl().contains("state=state123"));
        }

        @Test
        @DisplayName("should reject inactive client")
        void shouldRejectInactiveClient() {
            assertThrows(OAuthClientService.OAuthClientNotFoundException.class, () -> {
                oauthService.authorize("invalid-client", "https://cb.com", "code", "openid", null, null, null, null, "u", null);
            });
        }
    }

    // ===== P3.5: Refresh Token =====

    @Nested
    @DisplayName("P3.5 Refresh Token")
    class RefreshTokenTests {
        private String clientId;
        private String rawSecret;

        @BeforeEach
        void createClient() {
            var result = clientService.createClient("USER", "user-1", "Test App", "desc", "CONFIDENTIAL", null, "user-1");
            clientId = result.client().getClientId();
            rawSecret = result.rawSecret();
        }

        @Test
        @DisplayName("should issue refresh token and use it to get new access token")
        void shouldRotateRefreshToken() {
            // First get auth code
            String codeChallenge = "E9Melhoa2OwvFrEMTJguCHaoeK1t8URWbuGJSstw-cM";
            var authResult = oauthService.authorize(clientId, "https://app.example.com/cb", "code",
                    "openid offline_access", "s", codeChallenge, "S256", null, "user-123", null);

            // Extract code from redirect URL
            String redirectUrl = authResult.redirectUrl();
            String code = redirectUrl.substring(redirectUrl.indexOf("code=oa_") + 5);
            if (code.contains("&")) code = code.substring(0, code.indexOf("&"));

            // Exchange code for tokens — use the actual client secret
            String codeVerifier = "dBjftJeZ4CVP-mB92K27uhbUJU1p1r_wW1gFWFOEjXk";
            var tokenResp = oauthService.token("authorization_code", code, clientId,
                    rawSecret, "https://app.example.com/cb", codeVerifier, null);

            assertNotNull(tokenResp.accessToken());
            assertNotNull(tokenResp.refreshToken());

            // Use refresh token
            var refreshed = oauthService.token("refresh_token", null, clientId, rawSecret,
                    null, null, tokenResp.refreshToken());
            assertNotNull(refreshed.accessToken());
        }

        @Test
        @DisplayName("should reject reused refresh token (rotation detection)")
        void shouldDetectReuse() {
            // Create grant + refresh
            String codeChallenge = "E9Melhoa2OwvFrEMTJguCHaoeK1t8URWbuGJSstw-cM";
            var authResult = oauthService.authorize(clientId, "https://app.example.com/cb", "code",
                    "openid offline_access", "s", codeChallenge, "S256", null, "user-123", null);
            String code = authResult.redirectUrl().substring(authResult.redirectUrl().indexOf("code=oa_") + 5);
            if (code.contains("&")) code = code.substring(0, code.indexOf("&"));
            String codeVerifier = "dBjftJeZ4CVP-mB92K27uhbUJU1p1r_wW1gFWFOEjXk";
            var tokenResp = oauthService.token("authorization_code", code, clientId,
                    rawSecret, "https://app.example.com/cb", codeVerifier, null);

            // First refresh — should work
            var r1 = oauthService.token("refresh_token", null, clientId, rawSecret, null, null, tokenResp.refreshToken());
            assertNotNull(r1.accessToken());

            // Reuse old refresh token — should fail with reuse detection
            assertThrows(Exception.class, () -> {
                oauthService.token("refresh_token", null, clientId, rawSecret, null, null, tokenResp.refreshToken());
            });
        }
    }

    // ===== P3.6: Service Account =====

    @Nested
    @DisplayName("P3.6 Service Account")
    class ServiceAccountTests {
        @Test
        @DisplayName("should create service account for organization")
        void shouldCreateServiceAccount() {
            ServiceAccount sa = saService.createAccount("org-1", "CI Bot", "For CI/CD", "admin-1");
            assertNotNull(sa.getId());
            assertEquals("org-1", sa.getOrganizationId());
            assertEquals("CI Bot", sa.getName());
            assertEquals("ACTIVE", sa.getStatus());
        }

        @Test
        @DisplayName("should list organization accounts")
        void shouldListOrgAccounts() {
            saService.createAccount("org-1", "Bot-1", "desc", "admin");
            saService.createAccount("org-1", "Bot-2", "desc", "admin");
            saService.createAccount("org-2", "Bot-3", "desc", "admin");

            List<ServiceAccount> org1Accounts = saService.listOrgAccounts("org-1");
            assertEquals(2, org1Accounts.size());
        }
    }

    // ===== P3.7: API Key =====

    @Nested
    @DisplayName("P3.7 API Key")
    class ApiKeyTests {
        @Test
        @DisplayName("should create API key and exchange for JWT")
        void shouldCreateAndExchange() {
            var result = apiKeyService.createKey("My Key", "USER", "user-1", "org-1", null, null, null);
            assertNotNull(result.rawKey());
            assertTrue(result.rawKey().startsWith("ak_"));

            String jwt = apiKeyService.exchangeKeyForToken(result.rawKey());
            assertNotNull(jwt);
            assertTrue(jwt.split("\\.").length == 3);
        }

        @Test
        @DisplayName("should revoke API key")
        void shouldRevokeKey() {
            var result = apiKeyService.createKey("My Key", "USER", "user-1", null, null, null, null);
            apiKeyService.revokeKey(result.key().getId());
            assertThrows(Exception.class, () -> apiKeyService.exchangeKeyForToken(result.rawKey()));
        }

        @Test
        @DisplayName("should list user keys")
        void shouldListUserKeys() {
            apiKeyService.createKey("K1", "USER", "user-1", null, null, null, null);
            apiKeyService.createKey("K2", "USER", "user-2", null, null, null, null);
            assertEquals(1, apiKeyService.getUserKeys("user-1").size());
        }
    }

    // ===== Token Introspection =====

    @Nested
    @DisplayName("Token Introspection")
    class IntrospectionTests {
        @Test
        @DisplayName("should introspect active token")
        void shouldIntrospectActiveToken() {
            String token = tokenService.issueAccessToken("sub", "user", "aud", "cid", "oid", "s", 3600);
            var ir = oauthService.introspect(token);
            assertTrue(ir.active());
        }

        @Test
        @DisplayName("should return inactive for invalid token")
        void shouldIntrospectInvalid() {
            var ir = oauthService.introspect("invalid.token.here");
            assertFalse(ir.active());
        }
    }

    // ========================================
    // STUB REPOSITORY CLASSES
    // ========================================

    static class StubScopeRepository implements ScopeRepository {
        private final Map<String, Scope> store = new LinkedHashMap<>();
        @Override public void save(Scope s) { store.put(s.getId(), s); }
        @Override public Optional<Scope> findById(String id) { return Optional.ofNullable(store.get(id)); }
        @Override public Optional<Scope> findByCode(String code) { return store.values().stream().filter(s -> s.getScopeCode().equals(code)).findFirst(); }
        @Override public List<Scope> findByService(String svc) { return store.values().stream().filter(s -> s.getSourceService().equals(svc)).collect(java.util.stream.Collectors.toList()); }
        @Override public List<Scope> findByIds(List<String> ids) { return store.values().stream().filter(s -> ids.contains(s.getId())).collect(java.util.stream.Collectors.toList()); }
        @Override public List<Scope> findAllAssignable() { return store.values().stream().filter(s -> s.getAssignable() == 1 && "ACTIVE".equals(s.getStatus())).collect(java.util.stream.Collectors.toList()); }
        @Override public List<Scope> findAll() { return new ArrayList<>(store.values()); }
        @Override public void update(Scope s) { store.put(s.getId(), s); }
        @Override public void updateStatus(String id, String status, long now, long version) { store.get(id).setStatus(status); }
    }

    static class StubScopePermissionRepository implements ScopePermissionRepository {
        private final List<ScopePermission> store = new ArrayList<>();
        @Override public void save(ScopePermission sp) { store.add(sp); }
        @Override public void saveAll(List<ScopePermission> sps) { store.addAll(sps); }
        @Override public List<ScopePermission> findByScopeId(String sid) { return store.stream().filter(s -> s.getScopeId().equals(sid)).collect(java.util.stream.Collectors.toList()); }
        @Override public List<ScopePermission> findByPermissionId(String pid) { return store.stream().filter(s -> s.getPermissionId().equals(pid)).collect(java.util.stream.Collectors.toList()); }
        @Override public List<ScopePermission> findByScopeIds(List<String> ids) { return store.stream().filter(s -> ids.contains(s.getScopeId())).collect(java.util.stream.Collectors.toList()); }
        @Override public void deleteByScopeId(String sid) { store.removeIf(s -> s.getScopeId().equals(sid)); }
        @Override public void deleteByScopeIdAndPermissionId(String sid, String pid) { store.removeIf(s -> s.getScopeId().equals(sid) && s.getPermissionId().equals(pid)); }
    }

    static class StubSigningKeyRepository implements SigningKeyRepository {
        private final Map<String, SigningKey> store = new LinkedHashMap<>();
        @Override public void save(SigningKey k) { store.put(k.getId(), k); }
        @Override public Optional<SigningKey> findById(String id) { return Optional.ofNullable(store.get(id)); }
        @Override public Optional<SigningKey> findByKeyId(String kid) { return store.values().stream().filter(k -> k.getKeyId().equals(kid)).findFirst(); }
        @Override public List<SigningKey> findByStatus(String status) { return store.values().stream().filter(k -> k.getStatus().equals(status)).collect(java.util.stream.Collectors.toList()); }
        @Override public List<SigningKey> findAllActive() { return store.values().stream().filter(k -> "ACTIVE".equals(k.getStatus()) || "RETIRING".equals(k.getStatus())).collect(java.util.stream.Collectors.toList()); }
        @Override public List<SigningKey> findAll() { return new ArrayList<>(store.values()); }
        @Override public void update(SigningKey k) { store.put(k.getId(), k); }
    }

    static class StubOAuthClientRepository implements OAuthClientRepository {
        private final Map<String, OAuthClient> store = new LinkedHashMap<>();
        private final Map<String, OAuthClient> byClientId = new LinkedHashMap<>();
        @Override public void save(OAuthClient c) { store.put(c.getId(), c); byClientId.put(c.getClientId(), c); }
        @Override public Optional<OAuthClient> findById(String id) { return Optional.ofNullable(store.get(id)); }
        @Override public Optional<OAuthClient> findByClientId(String clientId) { return Optional.ofNullable(byClientId.get(clientId)); }
        @Override public List<OAuthClient> findByOwner(String ot, String oid) { return store.values().stream().filter(c -> ot.equals(c.getOwnerType()) && oid.equals(c.getOwnerId())).collect(java.util.stream.Collectors.toList()); }
        @Override public List<OAuthClient> findAll() { return new ArrayList<>(store.values()); }
        @Override public List<OAuthClient> findByStatus(String s) { return store.values().stream().filter(c -> s.equals(c.getStatus())).collect(java.util.stream.Collectors.toList()); }
        @Override public void update(OAuthClient c) { store.put(c.getId(), c); byClientId.put(c.getClientId(), c); }
        @Override public void updateStatus(String id, String s, String rs, long n, long v) { OAuthClient c = store.get(id); if (c != null) { c.setStatus(s); c.setReviewStatus(rs); } }
    }

    static class StubOAuthClientSecretRepository implements OAuthClientSecretRepository {
        private final List<OAuthClientSecret> store = new ArrayList<>();
        @Override public void save(OAuthClientSecret s) { store.add(s); }
        @Override public Optional<OAuthClientSecret> findById(String id) { return store.stream().filter(s -> s.getId().equals(id)).findFirst(); }
        @Override public List<OAuthClientSecret> findByClientId(String cid) { return store.stream().filter(s -> s.getClientId().equals(cid)).collect(java.util.stream.Collectors.toList()); }
        @Override public Optional<OAuthClientSecret> findActiveByClientId(String cid) { return store.stream().filter(s -> s.getClientId().equals(cid) && "ACTIVE".equals(s.getStatus())).sorted((a,b) -> Long.compare(b.getCreatedAt(), a.getCreatedAt())).findFirst(); }
        @Override public void update(OAuthClientSecret s) { for (int i = 0; i < store.size(); i++) { if (store.get(i).getId().equals(s.getId())) { store.set(i, s); break; } } }
        @Override public void revokeByClientId(String cid, long now) { store.forEach(s -> { if (s.getClientId().equals(cid) && "ACTIVE".equals(s.getStatus())) { s.setStatus("REVOKED"); s.setRevokedAt(now); } }); }
        @Override public void updateLastUsedAt(String id, long time) { store.stream().filter(s -> s.getId().equals(id)).forEach(s -> s.setLastUsedAt(time)); }
    }

    static class StubAuthorizationCodeRepository implements AuthorizationCodeRepository {
        private final Map<String, AuthorizationCode> store = new LinkedHashMap<>();
        @Override public void save(AuthorizationCode c) { store.put(c.getCodeHash(), c); }
        @Override public Optional<AuthorizationCode> findByCodeHash(String h) { return Optional.ofNullable(store.get(h)); }
        @Override public void markUsed(String id, long now) { store.values().stream().filter(c -> c.getId().equals(id)).forEach(c -> { c.setStatus("USED"); c.setUsedAt(now); }); }
        @Override public void deleteExpired(long before) { store.entrySet().removeIf(e -> e.getValue().getExpiresAt() < before); }
    }

    static class StubAuthorizationGrantRepository implements AuthorizationGrantRepository {
        private final Map<String, AuthorizationGrant> store = new LinkedHashMap<>();
        @Override public void save(AuthorizationGrant g) { store.put(g.getId(), g); }
        @Override public Optional<AuthorizationGrant> findById(String id) { return Optional.ofNullable(store.get(id)); }
        @Override public Optional<AuthorizationGrant> findByUserAndClient(String uid, String cid) { return store.values().stream().filter(g -> uid.equals(g.getUserId()) && cid.equals(g.getClientId())).findFirst(); }
        @Override public List<AuthorizationGrant> findByUserId(String uid) { return store.values().stream().filter(g -> uid.equals(g.getUserId())).collect(java.util.stream.Collectors.toList()); }
        @Override public void update(AuthorizationGrant g) { store.put(g.getId(), g); }
    }

    static class StubRefreshTokenFamilyRepository implements RefreshTokenFamilyRepository {
        private final Map<String, RefreshTokenFamily> store = new LinkedHashMap<>();
        @Override public void save(RefreshTokenFamily f) { store.put(f.getId(), f); }
        @Override public Optional<RefreshTokenFamily> findById(String id) { return Optional.ofNullable(store.get(id)); }
        @Override public void update(RefreshTokenFamily f) { store.put(f.getId(), f); }
        @Override public List<RefreshTokenFamily> findByUserId(String uid) { return store.values().stream().filter(f -> uid.equals(f.getUserId())).collect(java.util.stream.Collectors.toList()); }
        @Override public List<RefreshTokenFamily> findByGrantId(String gid) { return store.values().stream().filter(f -> gid.equals(f.getGrantId())).collect(java.util.stream.Collectors.toList()); }
    }

    static class StubRefreshTokenRepository implements RefreshTokenRepository {
        private final Map<String, RefreshToken> store = new LinkedHashMap<>();
        @Override public void save(RefreshToken rt) { store.put(rt.getTokenHash(), rt); }
        @Override public Optional<RefreshToken> findByTokenHash(String h) { return Optional.ofNullable(store.get(h)); }
        @Override public Optional<RefreshToken> findById(String id) { return store.values().stream().filter(r -> r.getId().equals(id)).findFirst(); }
        @Override public void update(RefreshToken rt) { store.put(rt.getTokenHash(), rt); }
        @Override public List<RefreshToken> findByFamilyId(String fid) { return store.values().stream().filter(r -> fid.equals(r.getFamilyId())).collect(java.util.stream.Collectors.toList()); }
        @Override public void deleteExpired(long before) { store.entrySet().removeIf(e -> e.getValue().getExpiresAt() < before); }
    }

    static class StubTokenRevocationRepository implements TokenRevocationRepository {
        private final Set<String> revoked = new HashSet<>();
        @Override public void save(String jti, String sid, String reason, long exp) { revoked.add(jti); }
        @Override public boolean isRevoked(String jti) { return revoked.contains(jti); }
        @Override public void deleteExpired(long before) {}
    }

    static class StubApiKeyRepository implements ApiKeyRepository {
        private final Map<String, ApiKey> byHash = new LinkedHashMap<>();
        private final Map<String, ApiKey> byId = new LinkedHashMap<>();
        @Override public void save(ApiKey k) { byHash.put(k.getKeyHash(), k); byId.put(k.getId(), k); }
        @Override public Optional<ApiKey> findById(String id) { return Optional.ofNullable(byId.get(id)); }
        @Override public Optional<ApiKey> findByPrefix(String p) { return byId.values().stream().filter(k -> p.equals(k.getKeyPrefix()) && "ACTIVE".equals(k.getStatus())).findFirst(); }
        @Override public Optional<ApiKey> findByHash(String h) { return Optional.ofNullable(byHash.get(h)); }
        @Override public List<ApiKey> findByOwner(String ot, String oid) { return byId.values().stream().filter(k -> ot.equals(k.getOwnerType()) && oid.equals(k.getOwnerId())).collect(java.util.stream.Collectors.toList()); }
        @Override public List<ApiKey> findByOrg(String oid) { return byId.values().stream().filter(k -> oid.equals(k.getOrganizationId())).collect(java.util.stream.Collectors.toList()); }
        @Override public void update(ApiKey k) { byHash.put(k.getKeyHash(), k); byId.put(k.getId(), k); }
        @Override public void revoke(String id, long now) { ApiKey k = byId.get(id); if (k != null) k.setStatus("REVOKED"); }
    }

    static class StubServiceAccountRepository implements ServiceAccountRepository {
        private final Map<String, ServiceAccount> store = new LinkedHashMap<>();
        @Override public void save(ServiceAccount sa) { store.put(sa.getId(), sa); }
        @Override public Optional<ServiceAccount> findById(String id) { return Optional.ofNullable(store.get(id)); }
        @Override public List<ServiceAccount> findByOrg(String oid) { return store.values().stream().filter(s -> oid.equals(s.getOrganizationId())).collect(java.util.stream.Collectors.toList()); }
        @Override public void update(ServiceAccount sa) { store.put(sa.getId(), sa); }
    }
}