package com.github.houbb.core.identity.infrastructure.persistence;

import com.github.houbb.core.identity.application.domain.AccessPackageEntitlement;
import com.github.houbb.core.identity.application.port.AccessPackageEntitlementRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class JdbcAccessPackageEntitlementRepository implements AccessPackageEntitlementRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcAccessPackageEntitlementRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void save(AccessPackageEntitlement mapping) {
        jdbcTemplate.update(
                "INSERT INTO identity_access_package_entitlement (package_id, entitlement_id, created_at) " +
                "VALUES (?, ?, ?)",
                mapping.getPackageId(), mapping.getEntitlementId(), mapping.getCreatedAt()
        );
    }

    @Override
    public void saveBatch(List<AccessPackageEntitlement> mappings) {
        for (AccessPackageEntitlement m : mappings) {
            save(m);
        }
    }

    @Override
    public List<String> findEntitlementIdsByPackageId(String packageId) {
        return jdbcTemplate.queryForList(
                "SELECT entitlement_id FROM identity_access_package_entitlement WHERE package_id = ?",
                String.class, packageId);
    }

    @Override
    public List<String> findPackageIdsByEntitlementId(String entitlementId) {
        return jdbcTemplate.queryForList(
                "SELECT package_id FROM identity_access_package_entitlement WHERE entitlement_id = ?",
                String.class, entitlementId);
    }

    @Override
    public void deleteAllByPackageId(String packageId) {
        jdbcTemplate.update(
                "DELETE FROM identity_access_package_entitlement WHERE package_id = ?",
                packageId);
    }

    @Override
    public void deleteByPackageAndEntitlement(String packageId, String entitlementId) {
        jdbcTemplate.update(
                "DELETE FROM identity_access_package_entitlement WHERE package_id = ? AND entitlement_id = ?",
                packageId, entitlementId);
    }
}