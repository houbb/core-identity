package com.github.houbb.core.identity.application.port;

import com.github.houbb.core.identity.application.domain.AccessPackageEntitlement;

import java.util.List;

/**
 * Repository for identity_access_package_entitlement.
 */
public interface AccessPackageEntitlementRepository {

    void save(AccessPackageEntitlement mapping);

    void saveBatch(List<AccessPackageEntitlement> mappings);

    List<String> findEntitlementIdsByPackageId(String packageId);

    List<String> findPackageIdsByEntitlementId(String entitlementId);

    void deleteAllByPackageId(String packageId);

    void deleteByPackageAndEntitlement(String packageId, String entitlementId);
}
