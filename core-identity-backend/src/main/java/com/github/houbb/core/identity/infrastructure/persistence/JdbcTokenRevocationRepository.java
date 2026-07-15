package com.github.houbb.core.identity.infrastructure.persistence;

import com.github.houbb.core.identity.application.port.TokenRevocationRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public class JdbcTokenRevocationRepository implements TokenRevocationRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcTokenRevocationRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void save(String jti, String subjectId, String reason, long expiresAt) {
        jdbcTemplate.update(
                "INSERT OR IGNORE INTO identity_token_revocation (id, token_jti, subject_id, reason, expires_at, created_at) " +
                "VALUES (?, ?, ?, ?, ?, ?)",
                UUID.randomUUID().toString(), jti, subjectId, reason, expiresAt, System.currentTimeMillis()
        );
    }

    @Override
    public boolean isRevoked(String jti) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM identity_token_revocation WHERE token_jti = ? AND expires_at > ?",
                Integer.class, jti, System.currentTimeMillis());
        return count != null && count > 0;
    }

    @Override
    public void deleteExpired(long beforeTimestamp) {
        jdbcTemplate.update(
                "DELETE FROM identity_token_revocation WHERE expires_at < ?", beforeTimestamp);
    }
}