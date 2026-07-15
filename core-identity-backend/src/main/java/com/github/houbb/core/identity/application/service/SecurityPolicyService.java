package com.github.houbb.core.identity.application.service;

import com.github.houbb.core.identity.application.domain.SecurityPolicy;
import com.github.houbb.core.identity.application.port.SecurityPolicyRepository;

import java.util.Optional;
import java.util.UUID;

/**
 * Security policy service — CRUD for organization security policies.
 */
public class SecurityPolicyService {

    private final SecurityPolicyRepository policyRepo;

    public SecurityPolicyService(SecurityPolicyRepository policyRepo) {
        this.policyRepo = policyRepo;
    }

    public Optional<SecurityPolicy> findByOrganizationId(String organizationId) {
        return policyRepo.findByOrganizationId(organizationId);
    }

    public SecurityPolicy createOrUpdate(String organizationId, SecurityPolicy policy) {
        long now = System.currentTimeMillis();
        Optional<SecurityPolicy> existing = policyRepo.findByOrganizationId(organizationId);

        if (existing.isPresent()) {
            SecurityPolicy p = existing.get();
            p.setName(policy.getName());
            p.setMinimumAuthLevel(policy.getMinimumAuthLevel());
            p.setPhishingResistantRequired(policy.getPhishingResistantRequired());
            p.setAllowedAuthenticatorTypesJson(policy.getAllowedAuthenticatorTypesJson());
            p.setPrivilegedRolesOnly(policy.getPrivilegedRolesOnly());
            p.setTrustedDeviceDays(policy.getTrustedDeviceDays());
            p.setSessionIdleSeconds(policy.getSessionIdleSeconds());
            p.setSessionAbsoluteSeconds(policy.getSessionAbsoluteSeconds());
            p.setReauthSeconds(policy.getReauthSeconds());
            p.setUpdatedAt(now);
            policyRepo.update(p);
            return p;
        } else {
            policy.setId(UUID.randomUUID().toString());
            policy.setOrganizationId(organizationId);
            policy.setStatus("DRAFT");
            policy.setCreatedAt(now);
            policy.setUpdatedAt(now);
            policy.setVersion(1);
            policyRepo.save(policy);
            return policy;
        }
    }

    public void publish(String organizationId) {
        SecurityPolicy p = policyRepo.findByOrganizationId(organizationId)
                .orElseThrow(() -> new RuntimeException("Policy not found"));
        long now = System.currentTimeMillis();
        p.setStatus("ENFORCING");
        p.setGracePeriodEndsAt(now + 14 * 24 * 3600 * 1000L);
        p.setPublishedAt(now);
        p.setUpdatedAt(now);
        policyRepo.update(p);
    }

    public void suspend(String organizationId) {
        SecurityPolicy p = policyRepo.findByOrganizationId(organizationId)
                .orElseThrow(() -> new RuntimeException("Policy not found"));
        p.setStatus("SUSPENDED");
        p.setUpdatedAt(System.currentTimeMillis());
        policyRepo.update(p);
    }
}
