package com.github.houbb.core.identity.infrastructure.persistence;

import com.github.houbb.core.identity.application.domain.Credential;
import com.github.houbb.core.identity.application.port.CredentialRepository;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

@Repository
public class JdbcCredentialRepository implements CredentialRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcCredentialRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void save(Credential credential) {
        jdbcTemplate.update(
                "INSERT INTO identity_credential (id, user_id, credential_type, secret_hash, algorithm, status, " +
                "must_change, password_changed_at, failed_attempt_count, " +
                "hash_policy_version, last_rehashed_at, compromised_detected_at, " +
                "created_at, updated_at, version) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                credential.getId(), credential.getUserId(), credential.getCredentialType(),
                credential.getSecretHash(), credential.getAlgorithm(), credential.getStatus(),
                credential.getMustChange(), credential.getPasswordChangedAt(), credential.getFailedAttemptCount(),
                credential.getHashPolicyVersion(), credential.getLastRehashedAt(), credential.getCompromisedDetectedAt(),
                credential.getCreatedAt(), credential.getUpdatedAt(), credential.getVersion()
        );
    }

    @Override
    public Optional<Credential> findByUserIdAndType(String userId, String credentialType) {
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(
                    "SELECT * FROM identity_credential WHERE user_id = ? AND credential_type = ?",
                    new CredentialRowMapper(), userId, credentialType));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

        @Override
    public void update(Credential credential) {
        jdbcTemplate.update(
                "UPDATE identity_credential SET secret_hash = ?, algorithm = ?, status = ?, must_change = ?, " +
                "password_changed_at = ?, failed_attempt_count = ?, hash_policy_version = ?, " +
                "last_rehashed_at = ?, compromised_detected_at = ?, updated_at = ?, version = version + 1 " +
                "WHERE id = ? AND version = ?",
                credential.getSecretHash(), credential.getAlgorithm(), credential.getStatus(),
                credential.getMustChange(), credential.getPasswordChangedAt(), credential.getFailedAttemptCount(),
                credential.getHashPolicyVersion(), credential.getLastRehashedAt(), credential.getCompromisedDetectedAt(),
                credential.getUpdatedAt(), credential.getId(), credential.getVersion()
        );
    }

    @Override
    public void incrementFailedAttempts(String id, int newCount, long version) {
        jdbcTemplate.update(
                "UPDATE identity_credential SET failed_attempt_count = ?, updated_at = ?, version = version + 1 " +
                "WHERE id = ? AND version = ?",
                newCount, System.currentTimeMillis(), id, version
        );
    }

    @Override
    public void updatePassword(String id, String secretHash, String algorithm, long passwordChangedAt, long version) {
        jdbcTemplate.update(
                "UPDATE identity_credential SET secret_hash = ?, algorithm = ?, password_changed_at = ?, " +
                "must_change = 0, failed_attempt_count = 0, status = 'ACTIVE', updated_at = ?, version = version + 1 " +
                "WHERE id = ? AND version = ?",
                secretHash, algorithm, passwordChangedAt, System.currentTimeMillis(), id, version
        );
    }

    @Override
    public void revokeByUserId(String userId) {
        jdbcTemplate.update(
                "UPDATE identity_credential SET status = 'DISABLED', updated_at = ? WHERE user_id = ?",
                System.currentTimeMillis(), userId
        );
    }

        static class CredentialRowMapper implements RowMapper<Credential> {
        @Override
        public Credential mapRow(ResultSet rs, int rowNum) throws SQLException {
            Credential c = new Credential();
            c.setId(rs.getString("id"));
            c.setUserId(rs.getString("user_id"));
            c.setCredentialType(rs.getString("credential_type"));
            c.setSecretHash(rs.getString("secret_hash"));
            c.setAlgorithm(rs.getString("algorithm"));
            c.setStatus(rs.getString("status"));
            c.setMustChange(rs.getInt("must_change"));
            c.setPasswordChangedAt(JdbcUserRepository.getNullableLong(rs, "password_changed_at"));
            c.setFailedAttemptCount(rs.getInt("failed_attempt_count"));
            c.setHashPolicyVersion(rs.getString("hash_policy_version"));
            c.setLastRehashedAt(JdbcUserRepository.getNullableLong(rs, "last_rehashed_at"));
            c.setCompromisedDetectedAt(JdbcUserRepository.getNullableLong(rs, "compromised_detected_at"));
            c.setCreatedAt(rs.getLong("created_at"));
            c.setUpdatedAt(rs.getLong("updated_at"));
            c.setVersion(rs.getLong("version"));
            return c;
        }
    }
}