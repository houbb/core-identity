package com.github.houbb.core.identity.infrastructure.persistence;

import com.github.houbb.core.identity.application.domain.UserEmail;
import com.github.houbb.core.identity.application.port.UserEmailRepository;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

@Repository
public class JdbcUserEmailRepository implements UserEmailRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcUserEmailRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void save(UserEmail email) {
        jdbcTemplate.update(
                "INSERT INTO identity_user_email (id, user_id, email_normalized, email_display, is_primary, " +
                "verified_at, created_at, updated_at, version) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
                email.getId(), email.getUserId(), email.getEmailNormalized(), email.getEmailDisplay(),
                email.getIsPrimary(), email.getVerifiedAt(), email.getCreatedAt(), email.getUpdatedAt(), email.getVersion()
        );
    }

    @Override
    public Optional<UserEmail> findByNormalized(String emailNormalized) {
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(
                    "SELECT * FROM identity_user_email WHERE email_normalized = ?",
                    new UserEmailRowMapper(), emailNormalized));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<UserEmail> findByUserId(String userId) {
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(
                    "SELECT * FROM identity_user_email WHERE user_id = ? LIMIT 1",
                    new UserEmailRowMapper(), userId));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public void update(UserEmail email) {
        jdbcTemplate.update(
                "UPDATE identity_user_email SET email_normalized = ?, email_display = ?, is_primary = ?, " +
                "verified_at = ?, updated_at = ?, version = version + 1 WHERE id = ? AND version = ?",
                email.getEmailNormalized(), email.getEmailDisplay(), email.getIsPrimary(),
                email.getVerifiedAt(), email.getUpdatedAt(), email.getId(), email.getVersion()
        );
    }

    @Override
    public void markVerified(String id, long verifiedAt, long version) {
        jdbcTemplate.update(
                "UPDATE identity_user_email SET verified_at = ?, updated_at = ?, version = version + 1 " +
                "WHERE id = ? AND version = ?",
                verifiedAt, System.currentTimeMillis(), id, version
        );
    }

    static class UserEmailRowMapper implements RowMapper<UserEmail> {
        @Override
        public UserEmail mapRow(ResultSet rs, int rowNum) throws SQLException {
            UserEmail e = new UserEmail();
            e.setId(rs.getString("id"));
            e.setUserId(rs.getString("user_id"));
            e.setEmailNormalized(rs.getString("email_normalized"));
            e.setEmailDisplay(rs.getString("email_display"));
            e.setIsPrimary(rs.getInt("is_primary"));
            e.setVerifiedAt(JdbcUserRepository.getNullableLong(rs, "verified_at"));
            e.setCreatedAt(rs.getLong("created_at"));
            e.setUpdatedAt(rs.getLong("updated_at"));
            e.setVersion(rs.getLong("version"));
            return e;
        }
    }
}