package com.github.houbb.core.identity.infrastructure.persistence;

import com.github.houbb.core.identity.application.domain.SigningKey;
import com.github.houbb.core.identity.application.port.SigningKeyRepository;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Repository
public class JdbcSigningKeyRepository implements SigningKeyRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcSigningKeyRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void save(SigningKey key) {
        jdbcTemplate.update(
                "INSERT INTO identity_signing_key (id, key_id, algorithm, public_key, " +
                "encrypted_private_key, status, active_from, retire_after, created_at, updated_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                key.getId(), key.getKeyId(), key.getAlgorithm(), key.getPublicKey(),
                key.getEncryptedPrivateKey(), key.getStatus(),
                key.getActiveFrom(), key.getRetireAfter(),
                key.getCreatedAt(), key.getUpdatedAt()
        );
    }

    @Override
    public Optional<SigningKey> findById(String id) {
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(
                    "SELECT * FROM identity_signing_key WHERE id = ?", new SigningKeyRowMapper(), id));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<SigningKey> findByKeyId(String keyId) {
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(
                    "SELECT * FROM identity_signing_key WHERE key_id = ?", new SigningKeyRowMapper(), keyId));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<SigningKey> findByStatus(String status) {
        return jdbcTemplate.query(
                "SELECT * FROM identity_signing_key WHERE status = ? ORDER BY created_at",
                new SigningKeyRowMapper(), status);
    }

    @Override
    public List<SigningKey> findAllActive() {
        return jdbcTemplate.query(
                "SELECT * FROM identity_signing_key WHERE status IN ('ACTIVE', 'RETIRING') ORDER BY created_at",
                new SigningKeyRowMapper());
    }

    @Override
    public List<SigningKey> findAll() {
        return jdbcTemplate.query(
                "SELECT * FROM identity_signing_key ORDER BY created_at",
                new SigningKeyRowMapper());
    }

    @Override
    public void update(SigningKey key) {
        jdbcTemplate.update(
                "UPDATE identity_signing_key SET status = ?, active_from = ?, retire_after = ?, " +
                "public_key = ?, encrypted_private_key = ?, updated_at = ? WHERE id = ?",
                key.getStatus(), key.getActiveFrom(), key.getRetireAfter(),
                key.getPublicKey(), key.getEncryptedPrivateKey(),
                key.getUpdatedAt(), key.getId()
        );
    }

    static class SigningKeyRowMapper implements RowMapper<SigningKey> {
        @Override
        public SigningKey mapRow(ResultSet rs, int rowNum) throws SQLException {
            SigningKey k = new SigningKey();
            k.setId(rs.getString("id"));
            k.setKeyId(rs.getString("key_id"));
            k.setAlgorithm(rs.getString("algorithm"));
            k.setPublicKey(rs.getString("public_key"));
            k.setEncryptedPrivateKey(rs.getString("encrypted_private_key"));
            k.setStatus(rs.getString("status"));
            k.setActiveFrom(rs.getLong("active_from"));
            k.setRetireAfter(rs.getLong("retire_after"));
            k.setCreatedAt(rs.getLong("created_at"));
            k.setUpdatedAt(rs.getLong("updated_at"));
            return k;
        }
    }
}