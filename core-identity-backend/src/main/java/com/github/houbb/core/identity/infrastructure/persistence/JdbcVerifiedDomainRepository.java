package com.github.houbb.core.identity.infrastructure.persistence;

import com.github.houbb.core.identity.application.domain.VerifiedDomain;
import com.github.houbb.core.identity.application.port.VerifiedDomainRepository;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Repository
public class JdbcVerifiedDomainRepository implements VerifiedDomainRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcVerifiedDomainRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void save(VerifiedDomain domain) {
        jdbcTemplate.update(
                "INSERT INTO identity_verified_domain (id, organization_id, domain_name, status, " +
                "verification_method, verified_at, last_checked_at, expires_at, conflict_reason, " +
                "created_by, created_at, updated_at, version) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                domain.getId(), domain.getOrganizationId(), domain.getDomainName(),
                domain.getStatus(), domain.getVerificationMethod(), domain.getVerifiedAt(),
                domain.getLastCheckedAt(), domain.getExpiresAt(), domain.getConflictReason(),
                domain.getCreatedBy(), domain.getCreatedAt(), domain.getUpdatedAt(), domain.getVersion()
        );
    }

    @Override
    public Optional<VerifiedDomain> findById(String id) {
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(
                    "SELECT * FROM identity_verified_domain WHERE id = ?",
                    new VerifiedDomainRowMapper(), id));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<VerifiedDomain> findByDomainName(String domainName) {
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(
                    "SELECT * FROM identity_verified_domain WHERE domain_name = ?",
                    new VerifiedDomainRowMapper(), domainName));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<VerifiedDomain> findByOrganizationId(String organizationId) {
        return jdbcTemplate.query(
                "SELECT * FROM identity_verified_domain WHERE organization_id = ?",
                new VerifiedDomainRowMapper(), organizationId);
    }

    @Override
    public List<VerifiedDomain> findByStatus(String status) {
        return jdbcTemplate.query(
                "SELECT * FROM identity_verified_domain WHERE status = ?",
                new VerifiedDomainRowMapper(), status);
    }

    @Override
    public void update(VerifiedDomain domain) {
        jdbcTemplate.update(
                "UPDATE identity_verified_domain SET organization_id = ?, domain_name = ?, status = ?, " +
                "verification_method = ?, verified_at = ?, last_checked_at = ?, expires_at = ?, " +
                "conflict_reason = ?, created_by = ?, updated_at = ?, " +
                "version = version + 1 WHERE id = ? AND version = ?",
                domain.getOrganizationId(), domain.getDomainName(), domain.getStatus(),
                domain.getVerificationMethod(), domain.getVerifiedAt(), domain.getLastCheckedAt(),
                domain.getExpiresAt(), domain.getConflictReason(), domain.getCreatedBy(),
                domain.getUpdatedAt(), domain.getId(), domain.getVersion()
        );
    }

    @Override
    public void updateStatus(String id, String status, long now, long version) {
        jdbcTemplate.update(
                "UPDATE identity_verified_domain SET status = ?, updated_at = ?, version = version + 1 " +
                "WHERE id = ? AND version = ?",
                status, now, id, version);
    }

    static class VerifiedDomainRowMapper implements RowMapper<VerifiedDomain> {
        @Override
        public VerifiedDomain mapRow(ResultSet rs, int rowNum) throws SQLException {
            VerifiedDomain d = new VerifiedDomain();
            d.setId(rs.getString("id"));
            d.setOrganizationId(rs.getString("organization_id"));
            d.setDomainName(rs.getString("domain_name"));
            d.setStatus(rs.getString("status"));
            d.setVerificationMethod(getStringOrNull(rs, "verification_method"));
            d.setVerifiedAt(getLongOrNull(rs, "verified_at"));
            d.setLastCheckedAt(getLongOrNull(rs, "last_checked_at"));
            d.setExpiresAt(getLongOrNull(rs, "expires_at"));
            d.setConflictReason(getStringOrNull(rs, "conflict_reason"));
            d.setCreatedBy(getStringOrNull(rs, "created_by"));
            d.setCreatedAt(rs.getLong("created_at"));
            d.setUpdatedAt(rs.getLong("updated_at"));
            d.setVersion(rs.getLong("version"));
            return d;
        }
    }

    private static String getStringOrNull(ResultSet rs, String column) throws SQLException {
        try {
            return rs.getString(column);
        } catch (SQLException e) {
            return null;
        }
    }

    private static Long getLongOrNull(ResultSet rs, String column) throws SQLException {
        try {
            long val = rs.getLong(column);
            return rs.wasNull() ? null : val;
        } catch (SQLException e) {
            return null;
        }
    }
}
