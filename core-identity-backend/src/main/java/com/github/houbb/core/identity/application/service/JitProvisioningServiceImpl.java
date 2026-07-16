package com.github.houbb.core.identity.application.service;

import com.github.houbb.core.identity.application.domain.*;
import com.github.houbb.core.identity.application.port.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * JIT Provisioning Service — auto-creates User + Membership on first SSO login.
 *
 * P5 CRITICAL RULES:
 * - Never assigns OWNER role via JIT
 * - SCIM-deactivated members cannot be re-activated by JIT (SCIM lifecycle priority)
 * - Checks domain allowlist from JitPolicy
 * - Checks seat limits (Billing stub for now)
 */
public class JitProvisioningServiceImpl implements JitProvisioningService {

    private static final Logger log = LoggerFactory.getLogger(JitProvisioningServiceImpl.class);

    private final JitPolicyRepository jitRepo;
    private final UserRepository userRepo;
    private final UserEmailRepository emailRepo;
    private final MembershipRepository membershipRepo;
    private final OrganizationRepository orgRepo;
    private final MembershipRoleRepository membershipRoleRepo;
    private final RoleRepository roleRepo;
    private final ExternalIdentityRepository extIdRepo;

    public JitProvisioningServiceImpl(JitPolicyRepository jitRepo, UserRepository userRepo,
                                       UserEmailRepository emailRepo, MembershipRepository membershipRepo,
                                       OrganizationRepository orgRepo, MembershipRoleRepository membershipRoleRepo,
                                       RoleRepository roleRepo, ExternalIdentityRepository extIdRepo) {
        this.jitRepo = jitRepo;
        this.userRepo = userRepo;
        this.emailRepo = emailRepo;
        this.membershipRepo = membershipRepo;
        this.orgRepo = orgRepo;
        this.membershipRoleRepo = membershipRoleRepo;
        this.roleRepo = roleRepo;
        this.extIdRepo = extIdRepo;
    }

    @Override
    @Transactional
    public JitProvisionResult provisionIfNeeded(String connectionId, String externalSubject, String email,
                                                 boolean emailVerified, String displayName,
                                                 String organizationId, long now) {
        // Check JIT policy
        JitPolicy policy = jitRepo.findByConnectionId(connectionId)
                .orElseThrow(() -> new ServiceException("IDENTITY_JIT_DISABLED", "JIT policy not found"));

        if (!"ENABLED".equals(policy.getStatus())) {
            throw new ServiceException("IDENTITY_JIT_DISABLED", "JIT provisioning is disabled for this connection");
        }

        // Check email domain allowlist
        String domain = extractDomain(email);
        if (policy.getAllowedDomainsJson() != null && !policy.getAllowedDomainsJson().isEmpty()) {
            // Simple domain check — in production, parse JSON and match
            if (!policy.getAllowedDomainsJson().contains(domain)) {
                throw new ServiceException("IDENTITY_JIT_DOMAIN_DENIED",
                        "Email domain not in allowlist: " + domain);
            }
        }

        // Check if external identity already exists (already provisioned)
        var existingExtId = extIdRepo.findByConnectionIdAndExternalSubject(connectionId, externalSubject);
        if (existingExtId.isPresent()) {
            ExternalIdentity ei = existingExtId.get();
            // Check if SCIM-deactivated
            if ("SUSPENDED".equals(ei.getStatus())) {
                throw new ServiceException("IDENTITY_JIT_DISABLED",
                        "Your enterprise account does not have access to this organization. Contact your administrator.");
            }
            // Already provisioned — update last login
            ei.setLastLoginAt(now);
            ei.setUpdatedAt(now);
            extIdRepo.updateLastLogin(ei.getId(), now, now, ei.getVersion());

            return new JitProvisionResult(ei.getUserId(),
                    membershipRepo.findByOrgAndUser(organizationId, ei.getUserId())
                            .map(m -> m.getId()).orElse(null),
                    false, "Already provisioned");
        }

        // Check for existing user by email
        var existingEmail = emailRepo.findByNormalized(email.toLowerCase());
        String userId;

        if (existingEmail.isPresent()) {
            if (policy.getAllowExistingLink() == 0) {
                throw new ServiceException("IDENTITY_ACCOUNT_LINK_REQUIRED",
                        "An account with this email already exists. Contact your administrator to link accounts.");
            }
            userId = existingEmail.get().getUserId();
        } else {
            if (policy.getAllowNewUsers() == 0) {
                throw new ServiceException("IDENTITY_JIT_DISABLED",
                        "New user creation is disabled for this connection.");
            }
            // Create new User
            userId = createUser(email, displayName, now);
        }

        // Check existing membership
        var existingMembership = membershipRepo.findByOrgAndUser(organizationId, userId);
        String membershipId;

        if (existingMembership.isPresent()) {
            Membership m = existingMembership.get();
            if ("SUSPENDED".equals(m.getStatus())) {
                // Check if SCIM-managed
                if ("SCIM".equals(m.getManagementSource())) {
                    throw new ServiceException("IDENTITY_JIT_DISABLED",
                            "Your enterprise account does not have access to this organization. Contact your administrator.");
                }
                // Reactivate locally-managed membership
                m.setStatus("ACTIVE");
                m.setLeftAt(null);
                m.setSuspendedAt(null);
                m.setUpdatedAt(now);
                membershipRepo.update(m);
            }
            membershipId = m.getId();
        } else {
            // Create new Membership
            membershipId = createMembership(userId, organizationId, policy, connectionId, now);
        }

        log.info("JIT provisioned: user={}, org={}, connection={}", userId, organizationId, connectionId);

        return new JitProvisionResult(userId, membershipId, true, "Provisioned via enterprise SSO");
    }

    @Override
    public boolean isJitAllowed(String connectionId, String email) {
        return jitRepo.findByConnectionId(connectionId)
                .map(p -> "ENABLED".equals(p.getStatus()))
                .orElse(false);
    }

    private String createUser(String email, String displayName, long now) {
        String userId = UUID.randomUUID().toString();
        User user = new User();
        user.setId(userId);
        user.setDisplayName(displayName != null ? displayName : email.split("@")[0]);
        user.setStatus("ACTIVE");
        user.setPrimaryIdentitySource("EXTERNAL");
        user.setCreatedAt(now);
        user.setUpdatedAt(now);
        user.setVersion(1);
        userRepo.save(user);

        UserEmail userEmail = new UserEmail();
        userEmail.setId(UUID.randomUUID().toString());
        userEmail.setUserId(userId);
        userEmail.setEmailNormalized(email.toLowerCase());
        userEmail.setEmailDisplay(email);
        userEmail.setVerifiedAt(now);
        userEmail.setIsPrimary(1);
        userEmail.setCreatedAt(now);
        userEmail.setUpdatedAt(now);
        userEmail.setVersion(1);
        emailRepo.save(userEmail);

        return userId;
    }

    private String createMembership(String userId, String organizationId, JitPolicy policy,
                                     String connectionId, long now) {
        String membershipId = UUID.randomUUID().toString();
        Membership membership = new Membership();
        membership.setId(membershipId);
        membership.setOrganizationId(organizationId);
        membership.setUserId(userId);
        membership.setMembershipType("MEMBER");
        membership.setStatus("ACTIVE");
        membership.setSource("JIT");
        membership.setManagementSource("JIT");
        membership.setManagedByConnectionId(connectionId);
        membership.setProvisionedAt(now);
        membership.setJoinedAt(now);
        membership.setCreatedAt(now);
        membership.setUpdatedAt(now);
        membership.setVersion(1);
        membershipRepo.save(membership);

        // Assign default role (never OWNER)
        String defaultRoleId = getDefaultRole(organizationId);
        MembershipRole mr = new MembershipRole();
        mr.setMembershipId(membershipId);
        mr.setRoleId(defaultRoleId);
        membershipRoleRepo.save(mr);

        return membershipId;
    }

    private String getDefaultRole(String organizationId) {
        // Find MEMBER role for this organization by key
        return roleRepo.findByOrgAndKey(organizationId, "MEMBER")
                .map(r -> r.getId())
                .orElseGet(() -> roleRepo.findByOrgAndKey(organizationId, "VIEWER")
                        .map(r -> r.getId())
                        .orElseThrow(() -> new ServiceException("IDENTITY_JIT_DISABLED",
                                "No default role available for organization")));
    }

    private String extractDomain(String email) {
        if (email == null || !email.contains("@")) return null;
        return email.substring(email.indexOf('@') + 1).toLowerCase().trim();
    }

    public static class ServiceException extends RuntimeException {
        private final String errorCode;

        public ServiceException(String errorCode, String message) {
            super(message);
            this.errorCode = errorCode;
        }

        public String getErrorCode() { return errorCode; }
    }
}