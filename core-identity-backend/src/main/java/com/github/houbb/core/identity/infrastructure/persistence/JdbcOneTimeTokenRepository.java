package com.github.houbb.core.identity.infrastructure.persistence;

import com.github.houbb.core.identity.application.domain.OneTimeToken;
import com.github.houbb.core.identity.application.port.OneTimeTokenRepository;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

@Repository
public class JdbcOneTimeTokenRepository implements OneTimeTokenRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcOneTimeTokenRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void save(OneTimeToken token) {
        jdbcTemplate.update(
                "INSERT INTO identity_one_time_token (id, user_id, token_type, token_hash, status, expires_at, " +
                "used_at, metadata_json, created_at, updated_at, version) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                token.getId(), token.getUserId(), token.getTokenType(), token.getTokenHash(),
                token.getStatus(), token.getExpiresAt(), token.getUsedAt(), token.getMetadataJson(),
                token.getCreatedAt(), token.getUpdatedAt(), token.getVersion()
        );
    }

    @Override
    public Optional<OneTimeToken> findByTokenHash(String tokenHash) {
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(
                    "SELECT * FROM identity_one_time_token WHERE token_hash = ?",
                    new OneTimeTokenRowMapper(), tokenHash));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<OneTimeToken> findActiveByUserAndType(String userId, String tokenType) {
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(
                    "SELECT * FROM identity_one_time_token WHERE user_id = ? AND token_type = ? " +
                    "AND status = 'ACTIVE' AND expires_at > ? ORDER BY created_at DESC LIMIT 1",
                    new OneTimeTokenRowMapper(), userId, tokenType, System.currentTimeMillis()));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public void markUsed(String id, long usedAt, long version) {
        jdbcTemplate.update(
                "UPDATE identity_one_time_token SET status = 'USED', used_at = ?, updated_at = ?, " +
                "version = version + 1 WHERE id = ? AND version = ?",
                usedAt, System.currentTimeMillis(), id, version
        );
    }

    @Override
    public void revokeAllForUser(String userId) {
        jdbcTemplate.update(
                "UPDATE identity_one_time_token SET status = 'REVOKED', updated_at = ? " +
                "WHERE user_id = ? AND status = 'ACTIVE'",
                System.currentTimeMillis(), userId
        );
    }

    @Override
    public void revokeAllForUserAndType(String userId, String tokenType) {
        jdbcTemplate.update(
                "UPDATE identity_one_time_token SET status = 'REVOKED', updated_at = ? " +
                "WHERE user_id = ? AND token_type = ? AND status = 'ACTIVE'",
                System.currentTimeMillis(), userId, tokenType
        );
    }

    @Override
    public void expireBefore(long timestamp) {
        jdbcTemplate.update(
                "UPDATE identity_one_time_token SET status = 'REVOKED', updated_at = ? " +
                "WHERE status = 'ACTIVE' AND expires_at < ?",
                System.currentTimeMillis(), timestamp
        );
    }

    static class OneTimeTokenRowMapper implements RowMapper<OneTimeToken> {
        @Override
        public OneTimeToken mapRow(ResultSet rs, int rowNum) throws SQLException {
            OneTimeToken t = new OneTimeToken();
            t.setId(rs.getString("id"));
            t.setUserId(rs.getString("user_id"));
            t.setTokenType(rs.getString("token_type"));
            t.setTokenHash(rs.getString("token_hash"));
            t.setStatus(rs.getString("status"));
            t.setExpiresAt(rs.getLong("expires_at"));
            t.setUsedAt(JdbcUserRepository.getNullableLong(rs, "used_at"));
            t.setMetadataJson(rs.getString("metadata_json"));
            t.setCreatedAt(rs.getLong("created_at"));
            t.setUpdatedAt(rs.getLong("updated_at"));
            t.setVersion(rs.getLong("version"));
            return t;
        }
    }
}