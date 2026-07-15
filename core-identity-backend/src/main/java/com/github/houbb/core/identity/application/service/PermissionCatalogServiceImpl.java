package com.github.houbb.core.identity.application.service;

import com.github.houbb.core.identity.application.domain.Permission;
import com.github.houbb.core.identity.application.domain.PermissionSource;
import com.github.houbb.core.identity.application.port.PermissionRepository;
import com.github.houbb.core.identity.application.port.PermissionSourceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.stream.Collectors;

public class PermissionCatalogServiceImpl implements PermissionCatalogService {

    private static final Logger log = LoggerFactory.getLogger(PermissionCatalogServiceImpl.class);

    private final PermissionRepository permissionRepo;
    private final PermissionSourceRepository sourceRepo;

    public PermissionCatalogServiceImpl(PermissionRepository permissionRepo,
                                        PermissionSourceRepository sourceRepo) {
        this.permissionRepo = permissionRepo;
        this.sourceRepo = sourceRepo;
    }

    @Override
    @Transactional
    public List<Permission> syncPermissions(String serviceName, String manifestVersion,
                                            List<PermissionManifestEntry> entries, String syncedBy) {
        long now = System.currentTimeMillis();
        Set<String> incomingCodes = entries.stream()
                .map(PermissionManifestEntry::code)
                .collect(Collectors.toSet());

        // Compute checksum of the manifest
        String checksum = computeChecksum(manifestVersion, entries);

        // Get existing source record
        PermissionSource source = sourceRepo.findByServiceName(serviceName).orElse(null);

        if (source != null && checksum.equals(source.getChecksum())) {
            log.info("Permission manifest for {} unchanged (checksum {}), skipping sync", serviceName, checksum);
            return permissionRepo.findByService(serviceName);
        }

        // Get existing permissions for this service
        List<Permission> existingPermissions = permissionRepo.findByService(serviceName);
        Map<String, Permission> existingByCode = existingPermissions.stream()
                .collect(Collectors.toMap(Permission::getPermissionCode, p -> p));

        List<Permission> results = new ArrayList<>();
        int created = 0, updated = 0, deprecated = 0;

        // Process incoming entries
        for (PermissionManifestEntry entry : entries) {
            Permission existing = existingByCode.get(entry.code());
            if (existing != null) {
                // Update if name or description changed
                boolean changed = !Objects.equals(existing.getName(), entry.name()) ||
                        !Objects.equals(existing.getDescription(), entry.description()) ||
                        !Objects.equals(existing.getRiskLevel(), entry.riskLevel());
                if (changed) {
                    existing.setName(entry.name());
                    existing.setDescription(entry.description());
                    existing.setRiskLevel(entry.riskLevel());
                    existing.setSourceVersion(manifestVersion);
                    existing.setUpdatedAt(now);
                    permissionRepo.update(existing);
                    updated++;
                }
                // Reactivate if previously deprecated
                if ("DEPRECATED".equals(existing.getStatus()) || "DISABLED".equals(existing.getStatus())) {
                    permissionRepo.updateStatus(existing.getId(), "ACTIVE", now, existing.getVersion());
                    existing.setStatus("ACTIVE");
                }
                results.add(existing);
            } else {
                // Create new permission
                String permId = UUID.randomUUID().toString();
                String[] parts = parsePermissionCode(entry.code());
                Permission perm = new Permission();
                perm.setId(permId);
                perm.setPermissionCode(entry.code());
                perm.setSourceService(serviceName);
                perm.setResource(parts[0]);
                perm.setAction(parts[1]);
                perm.setName(entry.name());
                perm.setDescription(entry.description());
                perm.setRiskLevel(entry.riskLevel() != null ? entry.riskLevel() : "LOW");
                perm.setAssignable(1);
                perm.setStatus("ACTIVE");
                perm.setSourceVersion(manifestVersion);
                perm.setCreatedAt(now);
                perm.setUpdatedAt(now);
                perm.setVersion(1);
                permissionRepo.save(perm);
                created++;
                results.add(perm);
            }
        }

        // Mark removed permissions as DEPRECATED
        for (Permission existing : existingPermissions) {
            if (!incomingCodes.contains(existing.getPermissionCode()) &&
                    "ACTIVE".equals(existing.getStatus())) {
                permissionRepo.updateStatus(existing.getId(), "DEPRECATED", now, existing.getVersion());
                deprecated++;
            }
        }

        // Upsert source record
        if (source == null) {
            source = new PermissionSource();
            source.setId(UUID.randomUUID().toString());
            source.setServiceName(serviceName);
            source.setManifestVersion(manifestVersion);
            source.setChecksum(checksum);
            source.setLastSyncedAt(now);
            source.setLastSyncedBy(syncedBy);
            source.setStatus("ACTIVE");
            source.setCreatedAt(now);
            source.setUpdatedAt(now);
            source.setVersion(1);
            sourceRepo.save(source);
        } else {
            sourceRepo.updateSyncInfo(source.getId(), manifestVersion, checksum, now, syncedBy, source.getVersion());
        }

        log.info("Permission sync for {}: {} created, {} updated, {} deprecated (total {} perms)",
                serviceName, created, updated, deprecated, results.size());

        return results;
    }

    @Override
    public List<Permission> getAssignablePermissions(String service, String resource,
                                                      String riskLevel, String search) {
        return permissionRepo.findAssignableByService(service, resource, riskLevel, search);
    }

    @Override
    public List<Permission> getAllPermissions() {
        return permissionRepo.findAll();
    }

    /**
     * Parse permission code "service.resource.action" ignoring the service prefix
     * since it's stored in source_service already. Returns [resource, action].
     */
    private String[] parsePermissionCode(String code) {
        String[] parts = code.split("\\.");
        if (parts.length >= 3) {
            return new String[]{parts[parts.length - 2], parts[parts.length - 1]};
        }
        return new String[]{code, "execute"};
    }

    private String computeChecksum(String version, List<PermissionManifestEntry> entries) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(version.getBytes(StandardCharsets.UTF_8));
            for (PermissionManifestEntry entry : entries) {
                md.update(entry.code().getBytes(StandardCharsets.UTF_8));
            }
            byte[] hash = md.digest();
            String encoded = Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
            return encoded.substring(0, Math.min(44, encoded.length()));
        } catch (NoSuchAlgorithmException e) {
            return version + "-" + entries.size();
        }
    }
}