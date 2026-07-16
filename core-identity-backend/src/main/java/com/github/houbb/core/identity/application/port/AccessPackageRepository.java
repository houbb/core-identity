package com.github.houbb.core.identity.application.port;

import com.github.houbb.core.identity.application.domain.AccessPackage;

import java.util.List;
import java.util.Optional;

/**
 * Repository for identity_access_package.
 */
public interface AccessPackageRepository {

    void save(AccessPackage accessPackage);

    Optional<AccessPackage> findById(String id);

    Optional<AccessPackage> findByOrgAndCode(String organizationId, String packageCode);

    List<AccessPackage> findByOrgId(String organizationId);

    List<AccessPackage> findByOrgIdAndType(String organizationId, String packageType);

    List<AccessPackage> findRequestableByOrg(String organizationId);

    void update(AccessPackage accessPackage);

    void updateStatus(String id, String status, long now, long version);

    void deleteById(String id, long version);
}
