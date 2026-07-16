package com.github.houbb.core.identity.api.publicapi.controller;

import com.github.houbb.core.identity.application.domain.AccessPackage;
import com.github.houbb.core.identity.application.service.AccessPackageService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * Public API for access package management within an organization.
 */
@RestController
@RequestMapping("/api/v1/identity/organizations/{organizationId}")
public class AccessPackageController {

    private final AccessPackageService packageService;

    public AccessPackageController(AccessPackageService packageService) {
        this.packageService = packageService;
    }

    @GetMapping("/access-packages")
    public ResponseEntity<Map<String, Object>> listPackages(@PathVariable String organizationId) {
        List<AccessPackage> packages = packageService.listByOrganization(organizationId);
        List<Map<String, Object>> result = new ArrayList<>();
        for (AccessPackage pkg : packages) {
            result.add(toMap(pkg));
        }
        return ResponseEntity.ok(Map.of("packages", result, "total", result.size()));
    }

    @PostMapping("/access-packages")
    public ResponseEntity<Map<String, Object>> createPackage(
            @PathVariable String organizationId,
            @RequestBody Map<String, Object> body) {
        String name = (String) body.get("name");
        String description = (String) body.getOrDefault("description", "");
        String packageType = (String) body.getOrDefault("packageType", "STANDARD");
        String riskLevel = (String) body.getOrDefault("riskLevel", "LOW");
        String ownerUserId = (String) body.get("ownerUserId");
        long defaultDurationSeconds = body.get("defaultDurationSeconds") instanceof Number
                ? ((Number) body.get("defaultDurationSeconds")).longValue() : 0;
        long maxDurationSeconds = body.get("maxDurationSeconds") instanceof Number
                ? ((Number) body.get("maxDurationSeconds")).longValue() : 0;
        String requiredAuthLevel = (String) body.getOrDefault("requiredAuthLevel", "AUTH_LEVEL_1");

        @SuppressWarnings("unchecked")
        List<String> entitlementIds = (List<String>) body.get("entitlementIds");

        AccessPackage pkg = packageService.createPackage(organizationId, name, description,
                packageType, riskLevel, ownerUserId, defaultDurationSeconds, maxDurationSeconds,
                requiredAuthLevel, entitlementIds);
        return ResponseEntity.status(201).body(toMap(pkg));
    }

    @GetMapping("/access-packages/{packageId}")
    public ResponseEntity<Map<String, Object>> getPackage(
            @PathVariable String organizationId,
            @PathVariable String packageId) {
        AccessPackage pkg = packageService.getById(packageId);
        return ResponseEntity.ok(toMap(pkg));
    }

    @PatchMapping("/access-packages/{packageId}")
    public ResponseEntity<Map<String, Object>> updatePackage(
            @PathVariable String organizationId,
            @PathVariable String packageId,
            @RequestBody Map<String, Object> body) {
        String name = (String) body.get("name");
        String description = (String) body.get("description");
        String packageType = (String) body.get("packageType");
        String riskLevel = (String) body.get("riskLevel");
        String ownerUserId = (String) body.get("ownerUserId");
        long defaultDurationSeconds = body.get("defaultDurationSeconds") instanceof Number
                ? ((Number) body.get("defaultDurationSeconds")).longValue() : -1;
        long maxDurationSeconds = body.get("maxDurationSeconds") instanceof Number
                ? ((Number) body.get("maxDurationSeconds")).longValue() : -1;
        String requiredAuthLevel = (String) body.get("requiredAuthLevel");

        AccessPackage pkg = packageService.updatePackage(packageId, name, description,
                packageType, riskLevel, ownerUserId, defaultDurationSeconds, maxDurationSeconds,
                requiredAuthLevel);
        return ResponseEntity.ok(toMap(pkg));
    }

    @DeleteMapping("/access-packages/{packageId}")
    public ResponseEntity<Map<String, Object>> deletePackage(
            @PathVariable String organizationId,
            @PathVariable String packageId) {
        packageService.deletePackage(packageId);
        return ResponseEntity.ok(Map.of("message", "套餐已删除", "packageId", packageId));
    }

    @PutMapping("/access-packages/{packageId}/entitlements")
    public ResponseEntity<Map<String, Object>> setEntitlements(
            @PathVariable String organizationId,
            @PathVariable String packageId,
            @RequestBody Map<String, Object> body) {
        @SuppressWarnings("unchecked")
        List<String> entitlementIds = (List<String>) body.get("entitlementIds");
        packageService.setEntitlements(packageId, entitlementIds);
        return ResponseEntity.ok(Map.of(
                "message", "权益已更新",
                "packageId", packageId,
                "entitlementCount", entitlementIds != null ? entitlementIds.size() : 0
        ));
    }

    private Map<String, Object> toMap(AccessPackage pkg) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", pkg.getId());
        map.put("organizationId", pkg.getOrganizationId());
        map.put("packageCode", pkg.getPackageCode());
        map.put("name", pkg.getName());
        map.put("description", pkg.getDescription());
        map.put("packageType", pkg.getPackageType());
        map.put("riskLevel", pkg.getRiskLevel());
        map.put("requestable", pkg.getRequestable() == 1);
        map.put("defaultDurationSeconds", pkg.getDefaultDurationSeconds());
        map.put("maxDurationSeconds", pkg.getMaxDurationSeconds());
        map.put("requiredAuthLevel", pkg.getRequiredAuthLevel());
        map.put("ownerUserId", pkg.getOwnerUserId());
        map.put("status", pkg.getStatus());

        // 附加权益列表
        List<String> entIds = packageService.getEntitlementIds(pkg.getId());
        map.put("entitlementCount", entIds.size());
        map.put("entitlementIds", entIds);

        map.put("createdAt", pkg.getCreatedAt());
        map.put("updatedAt", pkg.getUpdatedAt());
        return map;
    }
}
