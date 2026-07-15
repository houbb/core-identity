package com.github.houbb.core.identity.infrastructure.persistence;

import com.github.houbb.core.identity.application.domain.Authenticator;
import com.github.houbb.core.identity.application.port.AuthenticatorRepository;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Repository
public class JdbcAuthenticatorRepository implements AuthenticatorRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcAuthenticatorRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void save(Authenticator a) {
        jdbcTemplate.update(
                "INSERT INTO identity_authenticator (id, user_id, authenticator_type, name, status, " +
                "assurance_level, phishing_resistant, user_verification_capable, enrolled_at, " +
                "last_used_at, compromised_at, revoked_at, created_at, updated_at, version) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                a.getId(), a.getUserId(), a.getAuthenticatorType(), a.getName(), a.getStatus(),
                a.getAssuranceLevel(), a.getPhishingResistant(), a.getUserVerificationCapable(),
                a.getEnrolledAt(), a.getLastUsedAt(), a.getCompromisedAt(), a.getRevokedAt(),
                a.getCreatedAt(), a.getUpdatedAt(), a.getVersion()
        );
    }

    @Override
    public Optional<Authenticator> findById(String id) {
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(
                    "SELECT * FROM identity_authenticator WHERE id = ?", new AuthenticatorRowMapper(), id));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<Authenticator> findByUserId(String userId) {
        return jdbcTemplate.query(
                "SELECT * FROM identity_authenticator WHERE user_id = ? ORDER BY created_at DESC",
                new AuthenticatorRowMapper(), userId);
    }

    @Override
    public List<Authenticator> findByUserIdAndStatus(String userId, String status) {
        return jdbcTemplate.query(
                "SELECT * FROM identity_authenticator WHERE user_id = ? AND status = ? ORDER BY created_at DESC",
                new AuthenticatorRowMapper(), userId, status);
    }

    @Override
    public Optional<Authenticator> findByUserIdAndType(String userId, String authenticatorType) {
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(
                    "SELECT * FROM identity_authenticator WHERE user_id = ? AND authenticator_type = ? AND status = 'ACTIVE'",
                    new AuthenticatorRowMapper(), userId, authenticatorType));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public void update(Authenticator a) {
        jdbcTemplate.update(
                "UPDATE identity_authenticator SET name = ?, status = ?, assurance_level = ?, " +
                "phishing_resistant = ?, user_verification_capable = ?, last_used_at = ?, " +
                "compromised_at = ?, revoked_at = ?, updated_at = ?, version = version + 1 " +
                "WHERE id = ? AND version = ?",
                a.getName(), a.getStatus(), a.getAssuranceLevel(),
                a.getPhishingResistant(), a.getUserVerificationCapable(), a.getLastUsedAt(),
                a.getCompromisedAt(), a.getRevokedAt(), a.getUpdatedAt(),
                a.getId(), a.getVersion()
        );
    }

    @Override
    public void updateStatus(String id, String status, long version) {
        jdbcTemplate.update(
                "UPDATE identity_authenticator SET status = ?, updated_at = ?, version = version + 1 " +
                "WHERE id = ? AND version = ?",
                status, System.currentTimeMillis(), id, version
        );
    }

    @Override
    public void updateLastUsedAt(String id, long lastUsedAt, long version) {
        jdbcTemplate.update(
                "UPDATE identity_authenticator SET last_used_at = ?, updated_at = ?, version = version + 1 " +
                "WHERE id = ? AND version = ?",
                lastUsedAt, System.currentTimeMillis(), id, version
        );
    }

    @Override
    public int countActiveByUserIdAndType(String userId, String authenticatorType) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM identity_authenticator WHERE user_id = ? AND authenticator_type = ? AND status = 'ACTIVE'",
                Integer.class, userId, authenticatorType);
        return count != null ? count : 0;
    }

    static class AuthenticatorRowMapper implements RowMapper<Authenticator> {
        @Override
        public Authenticator mapRow(ResultSet rs, int rowNum) throws SQLException {
            Authenticator a = new Authenticator();
            a.setId(rs.getString("id"));
            a.setUserId(rs.getString("user_id"));
            a.setAuthenticatorType(rs.getString("authenticator_type"));
            a.setName(rs.getString("name"));
            a.setStatus(rs.getString("status"));
            a.setAssuranceLevel(rs.getString("assurance_level"));
            a.setPhishingResistant(rs.getInt("phishing_resistant"));
            a.setUserVerificationCapable(rs.getInt("user_verification_capable"));
            a.setEnrolledAt(JdbcUserRepository.getNullableLong(rs, "enrolled_at"));
            a.setLastUsedAt(JdbcUserRepository.getNullableLong(rs, "last_used_at"));
            a.setCompromisedAt(JdbcUserRepository.getNullableLong(rs, "compromised_at"));
            a.setRevokedAt(JdbcUserRepository.getNullableLong(rs, "revoked_at"));
            a.setCreatedAt(rs.getLong("created_at"));
            a.setUpdatedAt(rs.getLong("updated_at"));
            a.setVersion(rs.getLong("version"));
            return a;
        }
    }
}
