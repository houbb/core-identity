package com.github.houbb.core.identity.infrastructure.configuration;

import com.github.houbb.core.identity.application.port.*;
import com.github.houbb.core.identity.application.service.*;
import com.github.houbb.core.identity.infrastructure.cache.CaffeineCacheManager;
import com.github.houbb.core.identity.infrastructure.security.BCryptPasswordHasher;
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
}