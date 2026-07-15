package com.github.houbb.core.identity.infrastructure.persistence;

import com.github.houbb.core.identity.application.domain.User;
import com.github.houbb.core.identity.application.port.UserRepository;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

@Repository
public class JdbcUserRepository implements UserRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcUserRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void save(User user) {
        jdbcTemplate.update(
                "INSERT INTO identity_user (id, display_name, status, locale, timezone, avatar_object_id, " +
                "last_login_at, locked_until, disabled_at, disabled_reason, created_at, updated_at, version) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                user.getId(), user.getDisplayName(), user.getStatus(), user.getLocale(), user.getTimezone(),
                user.getAvatarObjectId(), user.getLastLoginAt(), user.getLockedUntil(), user.getDisabledAt(),
                user.getDisabledReason(), user.getCreatedAt(), user.getUpdatedAt(), user.getVersion()
        );
    }

    @Override
    public Optional<User> findById(String id) {
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(
                    "SELECT * FROM identity_user WHERE id = ?", new UserRowMapper(), id));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public void update(User user) {
        jdbcTemplate.update(
                "UPDATE identity_user SET display_name = ?, status = ?, locale = ?, timezone = ?, " +
                "avatar_object_id = ?, last_login_at = ?, locked_until = ?, disabled_at = ?, " +
                "disabled_reason = ?, updated_at = ?, version = version + 1 WHERE id = ? AND version = ?",
                user.getDisplayName(), user.getStatus(), user.getLocale(), user.getTimezone(),
                user.getAvatarObjectId(), user.getLastLoginAt(), user.getLockedUntil(), user.getDisabledAt(),
                user.getDisabledReason(), user.getUpdatedAt(), user.getId(), user.getVersion()
        );
    }

    @Override
    public void updateStatus(String id, String status, long version) {
        jdbcTemplate.update(
                "UPDATE identity_user SET status = ?, updated_at = ?, version = version + 1 WHERE id = ? AND version = ?",
                status, System.currentTimeMillis(), id, version
        );
    }

    static class UserRowMapper implements RowMapper<User> {
        @Override
        public User mapRow(ResultSet rs, int rowNum) throws SQLException {
            User u = new User();
            u.setId(rs.getString("id"));
            u.setDisplayName(rs.getString("display_name"));
            u.setStatus(rs.getString("status"));
            u.setLocale(rs.getString("locale"));
            u.setTimezone(rs.getString("timezone"));
            u.setAvatarObjectId(rs.getString("avatar_object_id"));
            u.setLastLoginAt(getNullableLong(rs, "last_login_at"));
            u.setLockedUntil(getNullableLong(rs, "locked_until"));
            u.setDisabledAt(getNullableLong(rs, "disabled_at"));
            u.setDisabledReason(rs.getString("disabled_reason"));
            u.setCreatedAt(rs.getLong("created_at"));
            u.setUpdatedAt(rs.getLong("updated_at"));
            u.setVersion(rs.getLong("version"));
            return u;
        }
    }

    static Long getNullableLong(ResultSet rs, String column) throws SQLException {
        long val = rs.getLong(column);
        return rs.wasNull() ? null : val;
    }
}