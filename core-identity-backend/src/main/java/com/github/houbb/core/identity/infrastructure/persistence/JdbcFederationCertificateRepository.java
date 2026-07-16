package com.github.houbb.core.identity.infrastructure.persistence;

import com.github.houbb.core.identity.application.domain.FederationCertificate;
import com.github.houbb.core.identity.application.port.FederationCertificateRepository;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Repository
public class JdbcFederationCertificateRepository implements FederationCertificateRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcFederationCertificateRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void save(FederationCertificate c) {
        jdbcTemplate.update(
                "INSERT INTO identity_federation_certificate (id, connection_id, certificate_type, " +
                "certificate_pem, encrypted_private_key, key_version, fingerprint, status, " +
                "valid_from, valid_until, created_at, updated_at, version) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                c.getId(), c.getConnectionId(), c.getCertificateType(),
                c.getCertificatePem(), c.getEncryptedPrivateKey(), c.getKeyVersion(),
                c.getFingerprint(), c.getStatus(),
                c.getValidFrom(), c.getValidUntil(),
                c.getCreatedAt(), c.getUpdatedAt(), c.getVersion()
        );
    }

    @Override
    public Optional<FederationCertificate> findById(String id) {
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(
                    "SELECT * FROM identity_federation_certificate WHERE id = ?", new FcRowMapper(), id));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<FederationCertificate> findByConnectionId(String connectionId) {
        return jdbcTemplate.query(
                "SELECT * FROM identity_federation_certificate WHERE connection_id = ? ORDER BY created_at ASC",
                new FcRowMapper(), connectionId);
    }

    @Override
    public List<FederationCertificate> findByConnectionIdAndStatus(String connectionId, String status) {
        return jdbcTemplate.query(
                "SELECT * FROM identity_federation_certificate WHERE connection_id = ? AND status = ? ORDER BY created_at ASC",
                new FcRowMapper(), connectionId, status);
    }

    @Override
    public List<FederationCertificate> findByStatus(String status) {
        return jdbcTemplate.query(
                "SELECT * FROM identity_federation_certificate WHERE status = ? ORDER BY created_at ASC",
                new FcRowMapper(), status);
    }

    @Override
    public List<FederationCertificate> findExpiringBefore(long threshold) {
        return jdbcTemplate.query(
                "SELECT * FROM identity_federation_certificate WHERE valid_until <= ? AND status = 'ACTIVE' ORDER BY valid_until ASC",
                new FcRowMapper(), threshold);
    }

    @Override
    public void update(FederationCertificate c) {
        jdbcTemplate.update(
                "UPDATE identity_federation_certificate SET certificate_type = ?, certificate_pem = ?, " +
                "encrypted_private_key = ?, key_version = ?, fingerprint = ?, status = ?, " +
                "valid_from = ?, valid_until = ?, " +
                "updated_at = ?, version = version + 1 WHERE id = ? AND version = ?",
                c.getCertificateType(), c.getCertificatePem(), c.getEncryptedPrivateKey(),
                c.getKeyVersion(), c.getFingerprint(), c.getStatus(),
                c.getValidFrom(), c.getValidUntil(),
                c.getUpdatedAt(), c.getId(), c.getVersion()
        );
    }

    @Override
    public void updateStatus(String id, String status, long now, long version) {
        jdbcTemplate.update(
                "UPDATE identity_federation_certificate SET status = ?, updated_at = ?, " +
                "version = version + 1 WHERE id = ? AND version = ?",
                status, now, id, version);
    }

    static class FcRowMapper implements RowMapper<FederationCertificate> {
        @Override
        public FederationCertificate mapRow(ResultSet rs, int rowNum) throws SQLException {
            FederationCertificate c = new FederationCertificate();
            c.setId(rs.getString("id"));
            c.setConnectionId(rs.getString("connection_id"));
            c.setCertificateType(rs.getString("certificate_type"));
            c.setCertificatePem(getStringOrNull(rs, "certificate_pem"));
            c.setEncryptedPrivateKey(getStringOrNull(rs, "encrypted_private_key"));
            c.setKeyVersion(getStringOrNull(rs, "key_version"));
            c.setFingerprint(getStringOrNull(rs, "fingerprint"));
            c.setStatus(rs.getString("status"));
            c.setValidFrom(getLongOrNull(rs, "valid_from"));
            c.setValidUntil(getLongOrNull(rs, "valid_until"));
            c.setCreatedAt(rs.getLong("created_at"));
            c.setUpdatedAt(rs.getLong("updated_at"));
            c.setVersion(rs.getLong("version"));
            return c;
        }
    }

    private static String getStringOrNull(ResultSet rs, String column) throws SQLException {
        try { return rs.getString(column); } catch (SQLException e) { return null; }
    }

    private static Long getLongOrNull(ResultSet rs, String column) throws SQLException {
        try { long val = rs.getLong(column); return rs.wasNull() ? null : val; } catch (SQLException e) { return null; }
    }
}
