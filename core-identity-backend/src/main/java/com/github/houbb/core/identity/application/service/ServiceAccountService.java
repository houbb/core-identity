package com.github.houbb.core.identity.application.service;

import com.github.houbb.core.identity.application.domain.*;
import com.github.houbb.core.identity.application.port.*;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.UUID;

public class ServiceAccountService {
    private final ServiceAccountRepository saRepo;
    private final OAuthTokenService tokenService;

    public ServiceAccountService(ServiceAccountRepository saRepo, OAuthTokenService tokenService) {
        this.saRepo=saRepo; this.tokenService=tokenService;
    }

    @Transactional
    public ServiceAccount createAccount(String orgId, String name, String description, String createdBy) {
        long now = System.currentTimeMillis();
        ServiceAccount sa = new ServiceAccount();
        sa.setId(UUID.randomUUID().toString());
        sa.setOrganizationId(orgId);
        sa.setAccountType("ORGANIZATION");
        sa.setName(name);
        sa.setDescription(description);
        sa.setStatus("ACTIVE");
        sa.setCreatedBy(createdBy);
        sa.setCreatedAt(now);
        sa.setUpdatedAt(now);
        sa.setVersion(1);
        saRepo.save(sa);
        return sa;
    }

    public List<ServiceAccount> listOrgAccounts(String orgId) { return saRepo.findByOrg(orgId); }
    @Transactional
    public void suspend(String saId) {
        ServiceAccount sa = saRepo.findById(saId).orElseThrow(() -> new RuntimeException("Not found"));
        sa.setStatus("SUSPENDED");
        sa.setUpdatedAt(System.currentTimeMillis());
        saRepo.update(sa);
    }
}