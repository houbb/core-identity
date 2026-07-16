package com.github.houbb.core.identity.infrastructure.configuration;

import com.github.houbb.core.identity.application.port.*;
import com.github.houbb.core.identity.application.service.*;
import com.github.houbb.core.identity.infrastructure.cache.CaffeineCacheManager;
import com.github.houbb.core.identity.infrastructure.cache.CompositeCacheManager;
import com.github.houbb.core.identity.infrastructure.observability.GracefulShutdownHandler;
import com.github.houbb.core.identity.infrastructure.security.BCryptPasswordHasher;
import com.github.houbb.core.identity.infrastructure.security.TotpSecretEncryptor;
import com.github.houbb.core.identity.infrastructure.session.DatabaseSessionStore;
import com.github.houbb.core.identity.infrastructure.session.RedisSessionStore;
import com.github.houbb.core.identity.infrastructure.task.DatabaseLeaderElection;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Application service bean configuration.
 */
@Configuration
public class ServiceConfiguration {

    @Value("${core.identity.instance-name:Core Identity}")
    private String instanceName;

    @Value("${core.identity.edition:COMMUNITY}")
    private String edition;

    @Value("${core.internal-auth.signing-key}")
    private String signingKey;

    @Value("${core.internal-auth.issuer:core-identity}")
    private String issuer;

    @Value("${core.internal-auth.token-ttl-seconds:600}")
    private int tokenTtlSeconds;

    @Value("${core.oauth.master-key:dev-master-key-32-bytes-for-aes256!}")
    private String oauthMasterKey;

    @Value("${core.oauth.issuer-base:http://localhost:8101}")
    private String oauthIssuerBase;

    @Value("${core.oauth.access-token-ttl-seconds:900}")
    private int accessTokenTtlSeconds;

    @Value("${core.oauth.refresh-token-ttl-seconds:604800}")
    private int refreshTokenTtlSeconds;

    @Value("${core.oauth.auth-code-ttl-seconds:120}")
    private int authCodeTtlSeconds;

    @Value("${core.oauth.id-token-ttl-seconds:600}")
    private int idTokenTtlSeconds;

    @Value("${core.idempotency.record-ttl-hours:24}")
    private int recordTtlHours;

    // === Foundation beans ===

    @Bean
    public SystemInfoService systemInfoService() {
        return new SystemInfoServiceImpl("0.2.0", "v1", instanceName, edition);
    }

    @Bean
    public InternalTokenService internalTokenService(InternalClientRepository clientRepository) {
        return new InternalTokenServiceImpl(clientRepository, signingKey, issuer, tokenTtlSeconds);
    }

    @Bean
    public AuditService auditService(AuditEventRepository repository) {
        return new AuditServiceImpl(repository);
    }

    @Bean
    public OutboxService outboxService(OutboxEventRepository repository) {
        return new OutboxServiceImpl(repository);
    }

    @Bean
    public IdempotencyService idempotencyService(IdempotencyRecordRepository repository) {
        return new IdempotencyServiceImpl(repository, recordTtlHours);
    }

    @Bean
    public PasswordHasher passwordHasher() {
        return new BCryptPasswordHasher();
    }

    // === P3.2 Signing Key ===

    @Bean
    public SigningKeyManager signingKeyManager(SigningKeyRepository signingKeyRepository) {
        return new SigningKeyManager(signingKeyRepository, oauthMasterKey);
    }

    @Bean
    public OAuthTokenService oAuthTokenService(SigningKeyManager signingKeyManager) {
        return new OAuthTokenService(signingKeyManager, oauthIssuerBase);
    }

    // === P1 beans ===

    @Bean
    public AuthService authService(UserRepository userRepo,
                                   UserEmailRepository emailRepo,
                                   CredentialRepository credentialRepo,
                                   OrganizationRepository orgRepo,
                                   MembershipRepository membershipRepo,
                                   SessionRepository sessionRepo,
                                   OneTimeTokenRepository tokenRepo,
                                   LoginAttemptRepository loginAttemptRepo,
                                   PlatformOperatorRepository operatorRepo,
                                   AuditService auditService,
                                   OutboxService outboxService,
                                   IdempotencyService idempotencyService,
                                   PasswordHasher passwordHasher,
                                   IdentityNotificationPort notificationPort) {
        return new AuthServiceImpl(userRepo, emailRepo, credentialRepo, orgRepo, membershipRepo,
                sessionRepo, tokenRepo, loginAttemptRepo, operatorRepo,
                auditService, outboxService, idempotencyService, passwordHasher, notificationPort);
    }

    // === P2.1 Permission Catalog ===

    @Bean
    public PermissionCatalogService permissionCatalogService(PermissionRepository permissionRepo,
                                                             PermissionSourceRepository sourceRepo) {
        return new PermissionCatalogServiceImpl(permissionRepo, sourceRepo);
    }

    // === P2.2 Role Management ===

    @Bean
    public RoleService roleService(RoleRepository roleRepo,
                                   RolePermissionRepository rolePermissionRepo,
                                   MembershipRoleRepository membershipRoleRepo,
                                   PermissionRepository permissionRepo) {
        return new RoleServiceImpl(roleRepo, rolePermissionRepo, membershipRoleRepo, permissionRepo);
    }

    // === P2.3 Organization Management ===

    @Bean
    public OrganizationService organizationService(OrganizationRepository orgRepo,
                                                    MembershipRepository membershipRepo,
                                                    MembershipRoleRepository membershipRoleRepo,
                                                    RoleService roleService,
                                                    SessionRepository sessionRepo,
                                                    AuditService auditService,
                                                    OutboxService outboxService,
                                                    PasswordHasher passwordHasher,
                                                    CredentialRepository credentialRepo,
                                                    CaffeineCacheManager cacheManager) {
        return new OrganizationServiceImpl(orgRepo, membershipRepo, membershipRoleRepo, roleService,
                sessionRepo, auditService, outboxService, passwordHasher, credentialRepo, cacheManager);
    }

    // === P2.4 Membership Management ===

    @Bean
    public MembershipService membershipService(MembershipRepository membershipRepo,
                                                MembershipRoleRepository membershipRoleRepo,
                                                OrganizationRepository orgRepo,
                                                RoleRepository roleRepo,
                                                UserRepository userRepo,
                                                UserEmailRepository emailRepo,
                                                AuditService auditService,
                                                OutboxService outboxService,
                                                CaffeineCacheManager cacheManager) {
        return new MembershipServiceImpl(membershipRepo, membershipRoleRepo, orgRepo, roleRepo,
                userRepo, emailRepo, auditService, outboxService, cacheManager);
    }

    // === P2.4 Invitation Service ===

    @Bean
    public InvitationService invitationService(InvitationRepository invitationRepo,
                                                MembershipRepository membershipRepo,
                                                MembershipRoleRepository membershipRoleRepo,
                                                OrganizationRepository orgRepo,
                                                RoleRepository roleRepo,
                                                UserEmailRepository emailRepo,
                                                AuditService auditService,
                                                OutboxService outboxService) {
        return new InvitationServiceImpl(invitationRepo, membershipRepo, membershipRoleRepo,
                orgRepo, roleRepo, emailRepo, auditService, outboxService);
    }

    // === P3.1 Scope Catalog ===

    @Bean
    public ScopeCatalogService scopeCatalogService(ScopeRepository scopeRepo,
                                                   ScopePermissionRepository scopePermissionRepo) {
        return new ScopeCatalogServiceImpl(scopeRepo, scopePermissionRepo);
    }

    // === P3.3 OAuth Client ===

    @Bean
    public OAuthClientService oAuthClientService(OAuthClientRepository clientRepo,
                                                  OAuthClientSecretRepository secretRepo) {
        return new OAuthClientService(clientRepo, secretRepo);
    }

    // === P3.4-5 OAuth Flow ===

    @Bean
    public OAuthAuthorizationService oAuthAuthorizationService(
            AuthorizationCodeRepository codeRepo, OAuthClientService clientService,
            OAuthTokenService tokenService, AuthorizationGrantRepository grantRepo,
            RefreshTokenFamilyRepository familyRepo, RefreshTokenRepository rtRepo,
            TokenRevocationRepository revRepo, ServiceAccountRepository saRepo,
            ScopeRepository scopeRepo) {
        return new OAuthAuthorizationService(codeRepo, clientService, tokenService,
                grantRepo, familyRepo, rtRepo, revRepo, saRepo, scopeRepo,
                authCodeTtlSeconds, accessTokenTtlSeconds, refreshTokenTtlSeconds, idTokenTtlSeconds);
    }

    // === P3.6-7 API Key & Service Account ===

    @Bean
    public ApiKeyService apiKeyService(ApiKeyRepository keyRepo, OAuthTokenService tokenService) {
        return new ApiKeyService(keyRepo, tokenService, accessTokenTtlSeconds);
    }

    @Bean
    public ServiceAccountService serviceAccountService(ServiceAccountRepository saRepo,
                                                       OAuthTokenService tokenService) {
        return new ServiceAccountService(saRepo, tokenService);
    }

    // === P2.5 Authorization ===

    @Bean
    public AuthorizationService authorizationService(OrganizationRepository orgRepo,
                                                      MembershipRepository membershipRepo,
                                                      MembershipRoleRepository membershipRoleRepo,
                                                      RolePermissionRepository rolePermissionRepo,
                                                      RoleRepository roleRepo,
                                                      CaffeineCacheManager cacheManager) {
        return new AuthorizationServiceImpl(orgRepo, membershipRepo, membershipRoleRepo,
                rolePermissionRepo, roleRepo, cacheManager);
    }

    // === P4.1 Authenticator ===

    @Bean
    public AuthenticatorService authenticatorService(AuthenticatorRepository authenticatorRepo) {
        return new AuthenticatorServiceImpl(authenticatorRepo);
    }

    // === P4.2 TOTP + Recovery Codes ===

    @Value("${core.totp.encryption-key:dev-totp-key-32-bytes-for-aes256!!}")
    private String totpEncryptionKey;

    @Bean
    public TotpSecretEncryptor totpSecretEncryptor() {
        return new TotpSecretEncryptor(totpEncryptionKey);
    }

    @Bean
    public RecoveryCodeService recoveryCodeService(RecoveryCodeSetRepository codeSetRepo,
                                                    RecoveryCodeRepository codeRepo) {
        return new RecoveryCodeServiceImpl(codeSetRepo, codeRepo);
    }

    @Bean
    public TotpService totpService(TotpAuthenticatorRepository totpRepo,
                                   AuthenticatorRepository authenticatorRepo,
                                   AuthenticatorService authenticatorService,
                                   RecoveryCodeService recoveryCodeService,
                                   TotpSecretEncryptor encryptor) {
        return new TotpServiceImpl(totpRepo, authenticatorRepo, authenticatorService, recoveryCodeService, encryptor);
    }

    // === P4.3 WebAuthn / Passkey ===

    @Value("${core.webauthn.rp-id:localhost}")
    private String webauthnRpId;

    @Value("${core.webauthn.rp-name:Core Identity}")
    private String webauthnRpName;

    @Value("${core.webauthn.origin:http://localhost:5173}")
    private String webauthnOrigin;

    @Bean
    public WebAuthnService webAuthnService(WebAuthnCredentialRepository credentialRepo,
                                          AuthenticatorService authenticatorService,
                                          AuthenticatorRepository authenticatorRepo,
                                          UserRepository userRepo,
                                          UserEmailRepository emailRepo,
                                          AuthenticationChallengeRepository challengeRepo) {
        return new WebAuthnServiceImpl(credentialRepo, authenticatorService, authenticatorRepo,
                userRepo, emailRepo, challengeRepo, webauthnRpId, webauthnRpName, webauthnOrigin);
    }

    // === P4.5 Risk Engine ===

    @Value("${core.risk.medium-threshold:30}")
    private int riskMediumThreshold;

    @Value("${core.risk.high-threshold:60}")
    private int riskHighThreshold;

    @Value("${core.risk.critical-threshold:85}")
    private int riskCriticalThreshold;

    @Bean
    public RiskEngine riskEngine(SecurityEventRepository securityEventRepo) {
        return new RiskEngine(securityEventRepo, riskMediumThreshold, riskHighThreshold, riskCriticalThreshold);
    }

    // === P4.6 Account Recovery ===

    @Bean
    public AccountRecoveryService accountRecoveryService(AccountRecoveryRepository recoveryRepo,
                                                         UserRepository userRepo,
                                                         SessionRepository sessionRepo,
                                                         SecurityEventRepository securityEventRepo) {
        return new AccountRecoveryService(recoveryRepo, userRepo, sessionRepo, securityEventRepo);
    }

    // === P4.7 Security Policy ===

    @Bean
    public SecurityPolicyService securityPolicyService(SecurityPolicyRepository policyRepo) {
        return new SecurityPolicyService(policyRepo);
    }

    // ====================================================================
    // === P5 Enterprise SSO & Federation ================================
    // ====================================================================

    @Value("${core.federation.oidc-state-ttl-seconds:300}")
    private int oidcStateTtlSeconds;

    @Value("${core.federation.saml-clock-skew-seconds:60}")
    private int samlClockSkewSeconds;

    @Value("${core.federation.encryption-key:dev-federation-key-32-bytes-aes256}")
    private String federationEncryptionKey;

    @Bean
    public FederationService federationService(FederationConnectionRepository connRepo,
                                                VerifiedDomainRepository domainRepo,
                                                DomainVerificationRepository verificationRepo,
                                                OrganizationRepository orgRepo,
                                                AuditService auditService,
                                                OutboxService outboxService) {
        return new FederationServiceImpl(connRepo, domainRepo, verificationRepo, orgRepo,
                auditService, outboxService);
    }

    // ====================================================================
    // === P6 Governance =================================================
    // ====================================================================

    @Bean
    public AccessPackageService accessPackageService(AccessPackageRepository packageRepo,
                                                      AccessPackageEntitlementRepository pkgEntRepo) {
        return new AccessPackageServiceImpl(packageRepo, pkgEntRepo);
    }

    @Bean
    public AccessRequestService accessRequestService(AccessRequestRepository requestRepo,
                                                      AccessPackageRepository packageRepo) {
        return new AccessRequestServiceImpl(requestRepo, packageRepo);
    }

    @Bean
    public ApprovalService approvalService(ApprovalInstanceRepository instanceRepo,
                                            ApprovalStepRepository stepRepo,
                                            ApprovalDecisionRepository decisionRepo) {
        return new ApprovalService(instanceRepo, stepRepo, decisionRepo);
    }

    @Bean
    public PrivilegedAccessService privilegedAccessService(PrivilegedActivationRepository activationRepo) {
        return new PrivilegedAccessServiceImpl(activationRepo);
    }

    @Bean
    public SodService sodService(SodPolicyRepository policyRepo, SodDataRepository dataRepo) {
        return new SodService(policyRepo, dataRepo);
    }

    @Bean
    public AccessReviewService accessReviewService(AccessReviewDataRepository reviewDataRepo) {
        return new AccessReviewService(reviewDataRepo);
    }

    @Bean
    public AdminRoleService adminRoleService(PlatformOperatorRoleRepository roleRepo) {
        return new AdminRoleService(roleRepo);
    }

    @Bean
    public ComplianceService complianceService(ComplianceDataRepository complianceDataRepo) {
        return new ComplianceService(complianceDataRepo);
    }

    @Bean
    public PrivacyService privacyService(PrivacyDataRepository privacyDataRepo) {
        return new PrivacyService(privacyDataRepo);
    }

    // ====================================================================
    // === P7.0-7.1 Cluster, Session Store, and Deployment ===============
    // ====================================================================

    @Value("${core.cluster.enabled:false}")
    private boolean clusterEnabled;

    @Value("${core.cluster.region:default}")
    private String clusterRegion;

    @Value("${core.cluster.availability-zone:default}")
    private String clusterAvailabilityZone;

    @Bean
    public ClusterNodeService clusterNodeService(ClusterNodeRepository clusterNodeRepo) {
        return new ClusterNodeService(clusterNodeRepo, clusterEnabled,
                clusterRegion, clusterAvailabilityZone);
    }

    /**
     * SessionStore: database-backed (always available).
     * This is used as the delegate when Redis is configured but unavailable.
     */
    @Bean
    public DatabaseSessionStore databaseSessionStore(SessionRepository sessionRepo) {
        return new DatabaseSessionStore(sessionRepo);
    }

    /**
     * SessionStore: Redis-backed with DB fallback.
     * Active when core.session.store-type=redis.
     */
    @Bean
    @ConditionalOnProperty(name = "core.session.store-type", havingValue = "redis")
    public RedisSessionStore redisSessionStore(DatabaseSessionStore databaseSessionStore) {
        return new RedisSessionStore(databaseSessionStore);
    }

    @Value("${core.session.store-type:database}")
    private String sessionStoreType;

    /**
     * Primary SessionStore bean.
     * Uses RedisSessionStore when configured, otherwise DatabaseSessionStore.
     */
    @Bean
    public SessionStore sessionStore(
            @org.springframework.beans.factory.annotation.Qualifier("databaseSessionStore") SessionStore databaseStore) {
        if ("redis".equals(sessionStoreType)) {
            // Return RedisSessionStore if available, fall back to database
            try {
                return new RedisSessionStore(databaseStore);
            } catch (Exception e) {
                return databaseStore;
            }
        }
        return databaseStore;
    }

    // ====================================================================
    // === P7.2 Cache Manager =============================================
    // ====================================================================

    @Value("${core.cache.type:caffeine}")
    private String cacheType;

    @Bean
    public CaffeineCacheManager caffeineCacheManager() {
        return new CaffeineCacheManager();
    }

    @Bean
    public CompositeCacheManager compositeCacheManager(CaffeineCacheManager caffeine) {
        // P7.2: In standalone/standard mode, only Caffeine.
        // In enterprise mode with Redis, CompositeCacheManager wraps Redis + Caffeine.
        // Redis integration is deferred; for now, pass null as fallback.
        return new CompositeCacheManager(caffeine, null);
    }

    // ====================================================================
    // === P7.3 Outbox Relay ==============================================
    // ====================================================================

    @Value("${core.outbox.relay.enabled:false}")
    private boolean outboxRelayEnabled;

    @Bean
    public OutboxRelayService outboxRelayService(OutboxEventRepository outboxRepo,
                                                  EventSubscriptionRepository subRepo,
                                                  EventDeliveryAttemptRepository deliveryRepo) {
        return new OutboxRelayService(outboxRepo, subRepo, deliveryRepo, outboxRelayEnabled);
    }

    // ====================================================================
    // === P7.4 Leader Election & Distributed Jobs ========================
    // ====================================================================

    @Bean
    public DatabaseLeaderElection databaseLeaderElection(RuntimeLeaseRepository leaseRepo) {
        return new DatabaseLeaderElection(leaseRepo);
    }

    // ====================================================================
    // === P7.5 Graceful Shutdown =========================================
    // ====================================================================

    @Bean
    public GracefulShutdownHandler gracefulShutdownHandler(ClusterNodeService clusterNodeService,
                                                            LeaderElectionPort leaderElection) {
        return new GracefulShutdownHandler(clusterNodeService, leaderElection);
    }

    // ====================================================================
    // === P7.6 Degradation Manager =======================================
    // ====================================================================

    @Bean
    public DegradationManager degradationManager() {
        return new DegradationManager();
    }
}