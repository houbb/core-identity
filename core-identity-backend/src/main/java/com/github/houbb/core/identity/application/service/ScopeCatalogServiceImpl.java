package com.github.houbb.core.identity.application.service;

import com.github.houbb.core.identity.application.domain.Scope;
import com.github.houbb.core.identity.application.domain.ScopePermission;
import com.github.houbb.core.identity.application.port.ScopePermissionRepository;
import com.github.houbb.core.identity.application.port.ScopeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

public class ScopeCatalogServiceImpl implements ScopeCatalogService {

    private static final Logger log = LoggerFactory.getLogger(ScopeCatalogServiceImpl.class);

    private final ScopeRepository scopeRepo;
    private final ScopePermissionRepository scopePermissionRepo;

    public ScopeCatalogServiceImpl(ScopeRepository scopeRepo,
                                  ScopePermissionRepository scopePermissionRepo) {
        this.scopeRepo = scopeRepo;
        this.scopePermissionRepo = scopePermissionRepo;
    }

    @Override
    @Transactional
    public List<Scope> syncScopes(String serviceName, String manifestVersion,
                                  List<ScopeManifestEntry> entries, String syncedBy) {
        long now = System.currentTimeMillis();
        Set<String> incomingCodes = entries.stream()
                .map(ScopeManifestEntry::code)
                .collect(Collectors.toSet());

        List<Scope> existingScopes = scopeRepo.findByService(serviceName);
        Map<String, Scope> existingByCode = existingScopes.stream()
                .collect(Collectors.toMap(Scope::getScopeCode, s -> s));

        List<Scope> results = new ArrayList<>();
        int created = 0, updated = 0, deprecated = 0;

        for (ScopeManifestEntry entry : entries) {
            Scope existing = existingByCode.get(entry.code());
            if (existing != null) {
                boolean changed = !Objects.equals(existing.getName(), entry.name()) ||
                        !Objects.equals(existing.getDescription(), entry.description()) ||
                        !Objects.equals(existing.getRiskLevel(), entry.riskLevel()) ||
                        !Objects.equals(existing.getConsentDisplay(), entry.consentDisplay()) ||
                        !Objects.equals(existing.getAudienceCode(), entry.audienceCode());
                if (changed) {
                    existing.setName(entry.name());
                    existing.setDescription(entry.description());
                    existing.setRiskLevel(entry.riskLevel() != null ? entry.riskLevel() : "LOW");
                    existing.setConsentDisplay(entry.consentDisplay());
                    existing.setAudienceCode(entry.audienceCode());
                    existing.setUpdatedAt(now);
                    scopeRepo.update(existing);
                    updated++;
                }
                if ("DEPRECATED".equals(existing.getStatus()) || "DISABLED".equals(existing.getStatus())) {
                    scopeRepo.updateStatus(existing.getId(), "ACTIVE", now, existing.getVersion());
                    existing.setStatus("ACTIVE");
                }
                results.add(existing);
            } else {
                String scopeId = UUID.randomUUID().toString();
                Scope scope = new Scope();
                scope.setId(scopeId);
                scope.setScopeCode(entry.code());
                scope.setSourceService(serviceName);
                scope.setAudienceCode(entry.audienceCode());
                scope.setName(entry.name());
                scope.setDescription(entry.description());
                scope.setRiskLevel(entry.riskLevel() != null ? entry.riskLevel() : "LOW");
                scope.setConsentDisplay(entry.consentDisplay());
                scope.setAssignable(1);
                scope.setStatus("ACTIVE");
                scope.setCreatedAt(now);
                scope.setUpdatedAt(now);
                scope.setVersion(1);
                scopeRepo.save(scope);
                created++;
                results.add(scope);
            }
        }

        for (Scope existing : existingScopes) {
            if (!incomingCodes.contains(existing.getScopeCode()) &&
                    "ACTIVE".equals(existing.getStatus())) {
                scopeRepo.updateStatus(existing.getId(), "DEPRECATED", now, existing.getVersion());
                deprecated++;
            }
        }

        log.info("Scope sync for {}: {} created, {} updated, {} deprecated (total {} scopes)",
                serviceName, created, updated, deprecated, results.size());

        return results;
    }

    @Override
    @Transactional
    public void syncScopePermissions(String scopeId, List<String> permissionIds, String syncedBy) {
        long now = System.currentTimeMillis();

        // Get existing mappings
        List<ScopePermission> existing = scopePermissionRepo.findByScopeId(scopeId);
        Set<String> existingPermIds = existing.stream()
                .map(ScopePermission::getPermissionId)
                .collect(Collectors.toSet());

        // Add new mappings
        for (String permId : permissionIds) {
            if (!existingPermIds.contains(permId)) {
                ScopePermission sp = new ScopePermission();
                sp.setId(UUID.randomUUID().toString());
                sp.setScopeId(scopeId);
                sp.setPermissionId(permId);
                sp.setCreatedAt(now);
                scopePermissionRepo.save(sp);
            }
        }

        // Remove mappings no longer present
        Set<String> newPermIds = new HashSet<>(permissionIds);
        for (ScopePermission sp : existing) {
            if (!newPermIds.contains(sp.getPermissionId())) {
                scopePermissionRepo.deleteByScopeIdAndPermissionId(scopeId, sp.getPermissionId());
            }
        }

        log.info("Scope permissions synced for scope {}: {} permissions", scopeId, permissionIds.size());
    }

    @Override
    public List<Scope> getAssignableScopes(String service, String audienceCode, String riskLevel) {
        return scopeRepo.findAllAssignable().stream()
                .filter(s -> service == null || service.equals(s.getSourceService()))
                .filter(s -> audienceCode == null || audienceCode.equals(s.getAudienceCode()))
                .filter(s -> riskLevel == null || riskLevel.equals(s.getRiskLevel()))
                .collect(Collectors.toList());
    }

    @Override
    public List<Scope> getScopesByIds(List<String> scopeIds) {
        return scopeRepo.findByIds(scopeIds);
    }

    @Override
    public List<Scope> getAllScopes() {
        return scopeRepo.findAll();
    }

    @Override
    public List<ScopePermission> getScopePermissions(String scopeId) {
        return scopePermissionRepo.findByScopeId(scopeId);
    }

    @Override
    public List<ScopePermission> getScopePermissionsForScopes(List<String> scopeIds) {
        return scopePermissionRepo.findByScopeIds(scopeIds);
    }
}