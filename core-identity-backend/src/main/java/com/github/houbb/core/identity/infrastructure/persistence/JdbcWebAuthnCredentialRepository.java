package com.github.houbb.core.identity.infrastructure.persistence;

import com.github.houbb.core.identity.application.domain.WebAuthnCredential;
import com.github.houbb.core.identity.application.port.WebAuthnCredentialRepository;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

@Repository
public class JdbcWebAuthnCredentialRepository implements WebAuthnCredentialRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcWebAuthnCredentialRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void save(WebAuthnCredential c) {
        jdbcTemplate.update(
                "INSERT INTO identity_webauthn_credential (authenticator_id, credential_id, public_key, " +
                "user_handle, sign_count, aaguid, transports_json, attachment, discoverable, backup_eligible, " +
                "backup_state, attestation_format, created_origin, rp_id, created_at, last_used_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                c.getAuthenticatorId(), c.getCredentialId(), c.getPublicKey(),
                c.getUserHandle(), c.getSignCount(), c.getAaguid(), c.getTransportsJson(),
                c.getAttachment(), c.getDiscoverable(), c.getBackupEligible(),
                c.getBackupState(), c.getAttestationFormat(), c.getCreatedOrigin(),
                c.getRpId(), c.getCreatedAt(), c.getLastUsedAt()
        );
    }

    @Override
    public Optional<WebAuthnCredential> findByCredentialId(String credentialId) {
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(
                    "SELECT * FROM identity_webauthn_credential WHERE credential_id = ?",
                    new WebAuthnCredentialRowMapper(), credentialId));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<WebAuthnCredential> findByAuthenticatorId(String authenticatorId) {
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(
                    "SELECT * FROM identity_webauthn_credential WHERE authenticator_id = ?",
                    new WebAuthnCredentialRowMapper(), authenticatorId));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public void updateSignCount(String authenticatorId, long signCount, long lastUsedAt) {
        jdbcTemplate.update(
                "UPDATE identity_webauthn_credential SET sign_count = ?, last_used_at = ? WHERE authenticator_id = ?",
                signCount, lastUsedAt, authenticatorId
        );
    }

    static class WebAuthnCredentialRowMapper implements RowMapper<WebAuthnCredential> {
        @Override
        public WebAuthnCredential mapRow(ResultSet rs, int rowNum) throws SQLException {
            WebAuthnCredential c = new WebAuthnCredential();
            c.setAuthenticatorId(rs.getString("authenticator_id"));
            c.setCredentialId(rs.getString("credential_id"));
            c.setPublicKey(rs.getString("public_key"));
            c.setUserHandle(rs.getString("user_handle"));
            c.setSignCount(rs.getLong("sign_count"));
            c.setAaguid(rs.getString("aaguid"));
            c.setTransportsJson(rs.getString("transports_json"));
            c.setAttachment(rs.getString("attachment"));
            c.setDiscoverable(rs.getInt("discoverable"));
            c.setBackupEligible(rs.getInt("backup_eligible"));
            c.setBackupState(rs.getInt("backup_state"));
            c.setAttestationFormat(rs.getString("attestation_format"));
            c.setCreatedOrigin(rs.getString("created_origin"));
            c.setRpId(rs.getString("rp_id"));
            c.setCreatedAt(rs.getLong("created_at"));
            c.setLastUsedAt(JdbcUserRepository.getNullableLong(rs, "last_used_at"));
            return c;
        }
    }
}
