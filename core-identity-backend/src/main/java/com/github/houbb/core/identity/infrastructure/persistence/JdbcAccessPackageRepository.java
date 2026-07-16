package com.github.houbb.core.identity.infrastructure.persistence;

import com.github.houbb.core.identity.application.domain.AccessPackage;
import com.github.houbb.core.identity.application.port.AccessPackageRepository;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Repository
public class JdbcAccessPackageRepository implements AccessPackageRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcAccessPackageRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void save(AccessPackage pkg) {
        jdbcTemplate.update(
                "INSERT INTO identity_access_package (id, organization_id, package_code, name, " +
                "description, package_type, risk_level, requestable, default_duration_seconds, " +
                "max_duration_seconds, required_auth_level, owner_user_id, approval_policy_json, " +
                "eligibility_policy_json, status, created_at, updated_at, version) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                pkg.getId(), pkg.getOrganizationId(), pkg.getPackageCode(), pkg.getName(),
                pkg.getDescription(), pkg.getPackageType(), pkg.getRiskLevel(), pkg.getRequestable(),
                pkg.getDefaultDurationSeconds(), pkg.getMaxDurationSeconds(), pkg.getRequiredAuthLevel(),
                pkg.getOwnerUserId(), pkg.getApprovalPolicyJson(), pkg.getEligibilityPolicyJson(),
                pkg.getStatus(), pkg.getCreatedAt(), pkg.getUpdatedAt(), pkg.getVersion()
        );
    }

    @Override
    public Optional<AccessPackage> findById(String id) {
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(
                    "SELECT * FROM identity_access_package WHERE id = ?",
                    new AccessPackageRowMapper(), id));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<AccessPackage> findByOrgAndCode(String organizationId, String packageCode) {
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(
                    "SELECT * FROM identity_access_package WHERE organization_id = ? AND package_code = ?",
                    new AccessPackageRowMapper(), organizationId, packageCode));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<AccessPackage> findByOrgId(String organizationId) {
        return jdbcTemplate.query(
                "SELECT * FROM identity_access_package WHERE organization_id = ? ORDER BY name",
                new AccessPackageRowMapper(), organizationId);
    }

    @Override
    public List<AccessPackage> findByOrgIdAndType(String organizationId, String packageType) {
        return jdbcTemplate.query(
                "SELECT * FROM identity_access_package WHERE organization_id = ? AND package_type = ? ORDER BY name",
                new AccessPackageRowMapper(), organizationId, packageType);
    }

    @Override
    public List<AccessPackage> findRequestableByOrg(String organizationId) {
        return jdbcTemplate.query(
                "SELECT * FROM identity_access_package WHERE organization_id = ? AND requestable = 1 AND status = 'ACTIVE' ORDER BY name",
                new AccessPackageRowMapper(), organizationId);
    }

    @Override
    public void update(AccessPackage pkg) {
        jdbcTemplate.update(
                "UPDATE identity_access_package SET name = ?, description = ?, package_type = ?, " +
                "risk_level = ?, requestable = ?, default_duration_seconds = ?, max_duration_seconds = ?, " +
                "required_auth_level = ?, owner_user_id = ?, approval_policy_json = ?, " +
                "eligibility_policy_json = ?, status = ?, updated_at = ?, version = version + 1 " +
                "WHERE id = ? AND version = ?",
                pkg.getName(), pkg.getDescription(), pkg.getPackageType(),
                pkg.getRiskLevel(), pkg.getRequestable(), pkg.getDefaultDurationSeconds(),
                pkg.getMaxDurationSeconds(), pkg.getRequiredAuthLevel(), pkg.getOwnerUserId(),
                pkg.getApprovalPolicyJson(), pkg.getEligibilityPolicyJson(),
                pkg.getStatus(), pkg.getUpdatedAt(), pkg.getId(), pkg.getVersion()
        );
    }

    @Override
    public void updateStatus(String id, String status, long now, long version) {
        jdbcTemplate.update(
                "UPDATE identity_access_package SET status = ?, updated_at = ?, version = version + 1 " +
                "WHERE id = ? AND version = ?",
                status, now, id, version);
    }

    @Override
    public void deleteById(String id, long version) {
        jdbcTemplate.update(
                "DELETE FROM identity_access_package WHERE id = ? AND version = ?",
                id, version);
    }

    static class AccessPackageRowMapper implements RowMapper<AccessPackage> {
        @Override
        public AccessPackage mapRow(ResultSet rs, int rowNum) throws SQLException {
            AccessPackage pkg = new AccessPackage();
            pkg.setId(rs.getString("id"));
            pkg.setOrganizationId(rs.getString("organization_id"));
            pkg.setPackageCode(rs.getString("package_code"));
            pkg.setName(rs.getString("name"));
            pkg.setDescription(rs.getString("description"));
            pkg.setPackageType(rs.getString("package_type"));
            pkg.setRiskLevel(rs.getString("risk_level"));
            pkg.setRequestable(rs.getInt("requestable"));
            pkg.setDefaultDurationSeconds(getLongOrNull(rs, "default_duration_seconds"));
            pkg.setMaxDurationSeconds(getLongOrNull(rs, "max_duration_seconds"));
            pkg.setRequiredAuthLevel(rs.getString("required_auth_level"));
            pkg.setOwnerUserId(rs.getString("owner_user_id"));
            pkg.setApprovalPolicyJson(rs.getString("approval_policy_json"));
            pkg.setEligibilityPolicyJson(rs.getString("eligibility_policy_json"));
            pkg.setStatus(rs.getString("status"));
            pkg.setCreatedAt(rs.getLong("created_at"));
            pkg.setUpdatedAt(rs.getLong("updated_at"));
            pkg.setVersion(rs.getLong("version"));
            return pkg;
        }

        private Long getLongOrNull(ResultSet rs, String column) throws SQLException {
            long val = rs.getLong(column);
            return rs.wasNull() ? null : val;
        }
    }
}