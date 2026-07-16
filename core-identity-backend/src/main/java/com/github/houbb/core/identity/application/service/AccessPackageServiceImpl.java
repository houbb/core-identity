package com.github.houbb.core.identity.application.service;

import com.github.houbb.core.identity.application.domain.AccessPackage;
import com.github.houbb.core.identity.application.domain.AccessPackageEntitlement;
import com.github.houbb.core.identity.application.port.AccessPackageEntitlementRepository;
import com.github.houbb.core.identity.application.port.AccessPackageRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AccessPackageServiceImpl implements AccessPackageService {

    private static final Logger log = LoggerFactory.getLogger(AccessPackageServiceImpl.class);

    private final AccessPackageRepository packageRepo;
    private final AccessPackageEntitlementRepository pkgEntRepo;

    public AccessPackageServiceImpl(AccessPackageRepository packageRepo,
                                    AccessPackageEntitlementRepository pkgEntRepo) {
        this.packageRepo = packageRepo;
        this.pkgEntRepo = pkgEntRepo;
    }

    @Override
    @Transactional
    public AccessPackage createPackage(String organizationId, String name, String description,
                                       String packageType, String riskLevel, String ownerUserId,
                                       long defaultDurationSeconds, long maxDurationSeconds,
                                       String requiredAuthLevel, List<String> entitlementIds) {
        long now = System.currentTimeMillis();

        if (name == null || name.trim().isEmpty()) {
            throw new ServiceException("IDENTITY_PACKAGE_NAME_REQUIRED", "套餐名称不能为空");
        }

        String packageCode = generatePackageCode(name, organizationId);

        AccessPackage pkg = new AccessPackage();
        pkg.setId(UUID.randomUUID().toString());
        pkg.setOrganizationId(organizationId);
        pkg.setPackageCode(packageCode);
        pkg.setName(name.trim());
        pkg.setDescription(description);
        pkg.setPackageType(packageType != null ? packageType : "STANDARD");
        pkg.setRiskLevel(riskLevel != null ? riskLevel : "LOW");
        pkg.setRequestable(1);
        pkg.setDefaultDurationSeconds(defaultDurationSeconds > 0 ? defaultDurationSeconds : 0);
        pkg.setMaxDurationSeconds(maxDurationSeconds > 0 ? maxDurationSeconds : 0);
        pkg.setRequiredAuthLevel(requiredAuthLevel != null ? requiredAuthLevel : "AUTH_LEVEL_1");
        pkg.setOwnerUserId(ownerUserId);
        pkg.setStatus("ACTIVE");
        pkg.setCreatedAt(now);
        pkg.setUpdatedAt(now);
        pkg.setVersion(1);

        packageRepo.save(pkg);

        // 关联权益
        if (entitlementIds != null && !entitlementIds.isEmpty()) {
            for (String entId : entitlementIds) {
                AccessPackageEntitlement ape = new AccessPackageEntitlement();
                ape.setPackageId(pkg.getId());
                ape.setEntitlementId(entId);
                ape.setCreatedAt(now);
                pkgEntRepo.save(ape);
            }
        }

        log.info("Created access package: {} ({}) in org {}", name, packageCode, organizationId);
        return pkg;
    }

    @Override
    @Transactional
    public AccessPackage updatePackage(String packageId, String name, String description,
                                       String packageType, String riskLevel, String ownerUserId,
                                       long defaultDurationSeconds, long maxDurationSeconds,
                                       String requiredAuthLevel) {
        AccessPackage pkg = getById(packageId);
        long now = System.currentTimeMillis();

        if (name != null && !name.trim().isEmpty()) {
            pkg.setName(name.trim());
        }
        if (description != null) {
            pkg.setDescription(description);
        }
        if (packageType != null) {
            pkg.setPackageType(packageType);
        }
        if (riskLevel != null) {
            pkg.setRiskLevel(riskLevel);
        }
        if (ownerUserId != null) {
            pkg.setOwnerUserId(ownerUserId);
        }
        if (defaultDurationSeconds >= 0) {
            pkg.setDefaultDurationSeconds(defaultDurationSeconds);
        }
        if (maxDurationSeconds >= 0) {
            pkg.setMaxDurationSeconds(maxDurationSeconds);
        }
        if (requiredAuthLevel != null) {
            pkg.setRequiredAuthLevel(requiredAuthLevel);
        }
        pkg.setUpdatedAt(now);
        packageRepo.update(pkg);

        log.info("Updated access package: id={}", packageId);
        return pkg;
    }

    @Override
    @Transactional
    public void setEntitlements(String packageId, List<String> entitlementIds) {
        getById(packageId); // 验证存在
        long now = System.currentTimeMillis();

        pkgEntRepo.deleteAllByPackageId(packageId);
        if (entitlementIds != null) {
            for (String entId : entitlementIds) {
                AccessPackageEntitlement ape = new AccessPackageEntitlement();
                ape.setPackageId(packageId);
                ape.setEntitlementId(entId);
                ape.setCreatedAt(now);
                pkgEntRepo.save(ape);
            }
        }

        log.info("Set {} entitlements for package {}", entitlementIds != null ? entitlementIds.size() : 0, packageId);
    }

    @Override
    public List<String> getEntitlementIds(String packageId) {
        return pkgEntRepo.findEntitlementIdsByPackageId(packageId);
    }

    @Override
    @Transactional
    public void deletePackage(String packageId) {
        AccessPackage pkg = getById(packageId);
        pkgEntRepo.deleteAllByPackageId(packageId);
        packageRepo.deleteById(packageId, pkg.getVersion());
        log.info("Deleted access package: id={}", packageId);
    }

    @Override
    @Transactional
    public void setPackageStatus(String packageId, String status) {
        AccessPackage pkg = getById(packageId);
        if (status.equals(pkg.getStatus())) {
            return;
        }
        long now = System.currentTimeMillis();
        packageRepo.updateStatus(packageId, status, now, pkg.getVersion());
        log.info("Updated package {} status to {}", packageId, status);
    }

    @Override
    public AccessPackage getById(String id) {
        return packageRepo.findById(id)
                .orElseThrow(() -> new ServiceException("IDENTITY_PACKAGE_NOT_FOUND",
                        "访问套餐 " + id + " 不存在"));
    }

    @Override
    public List<AccessPackage> listByOrganization(String organizationId) {
        return packageRepo.findByOrgId(organizationId);
    }

    @Override
    public List<AccessPackage> listRequestable(String organizationId) {
        return packageRepo.findRequestableByOrg(organizationId);
    }

    @Override
    public List<AccessPackage> listByOrganizationAndType(String organizationId, String packageType) {
        return packageRepo.findByOrgIdAndType(organizationId, packageType);
    }

    private String generatePackageCode(String name, String organizationId) {
        String slug = "pkg-" + name.toLowerCase()
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-|-$", "");
        if (slug.length() > 60) {
            slug = slug.substring(0, 56) + "-" + (System.currentTimeMillis() % 10000);
        }
        String baseSlug = slug;
        int suffix = 1;
        while (packageRepo.findByOrgAndCode(organizationId, slug).isPresent()) {
            slug = baseSlug + "-" + suffix;
            suffix++;
        }
        return slug;
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
