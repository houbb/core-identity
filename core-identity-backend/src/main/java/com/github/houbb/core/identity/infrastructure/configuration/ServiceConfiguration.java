package com.github.houbb.core.identity.infrastructure.configuration;

import com.github.houbb.core.identity.application.port.*;
import com.github.houbb.core.identity.application.service.*;
import com.github.houbb.core.identity.infrastructure.cache.CaffeineCacheManager;
import com.github.houbb.core.identity.infrastructure.security.BCryptPasswordHasher;
import com.github.houbb.core.identity.infrastructure.security.TotpSecretEncryptor;
import org.springframework.beans.factory.annotation.Value;
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

    @Bean
    public CaffeineCacheManager caffeineCacheManager() {
        return new CaffeineCacheManager();
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
}