package com.github.houbb.core.identity.infrastructure.persistence;

import com.github.houbb.core.identity.application.domain.PlatformOperator;
import com.github.houbb.core.identity.application.port.PlatformOperatorRepository;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

@Repository
public class JdbcPlatformOperatorRepository implements PlatformOperatorRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcPlatformOperatorRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void save(PlatformOperator operator) {
        jdbcTemplate.update(
                "INSERT INTO identity_platform_operator (id, user_id, operator_role, status, granted_by, " +
                "granted_at, disabled_at, created_at, updated_at, version) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                operator.getId(), operator.getUserId(), operator.getOperatorRole(), operator.getStatus(),
                operator.getGrantedBy(), operator.getGrantedAt(), operator.getDisabledAt(),
                operator.getCreatedAt(), operator.getUpdatedAt(), operator.getVersion()
        );
    }

    @Override
    public Optional<PlatformOperator> findByUserId(String userId) {
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(
                    "SELECT * FROM identity_platform_operator WHERE user_id = ?",
                    new PlatformOperatorRowMapper(), userId));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public void update(PlatformOperator operator) {
        jdbcTemplate.update(
                "UPDATE identity_platform_operator SET status = ?, disabled_at = ?, updated_at = ?, " +
                "version = version + 1 WHERE id = ? AND version = ?",
                operator.getStatus(), operator.getDisabledAt(), operator.getUpdatedAt(),
                operator.getId(), operator.getVersion()
        );
    }

    @Override
    public void disable(String id, long disabledAt, long version) {
        jdbcTemplate.update(
                "UPDATE identity_platform_operator SET status = 'DISABLED', disabled_at = ?, updated_at = ?, " +
                "version = version + 1 WHERE id = ? AND version = ?",
                disabledAt, System.currentTimeMillis(), id, version
        );
    }

    static class PlatformOperatorRowMapper implements RowMapper<PlatformOperator> {
        @Override
        public PlatformOperator mapRow(ResultSet rs, int rowNum) throws SQLException {
            PlatformOperator p = new PlatformOperator();
            p.setId(rs.getString("id"));
            p.setUserId(rs.getString("user_id"));
            p.setOperatorRole(rs.getString("operator_role"));
            p.setStatus(rs.getString("status"));
            p.setGrantedBy(rs.getString("granted_by"));
            p.setGrantedAt(rs.getLong("granted_at"));
            p.setDisabledAt(JdbcUserRepository.getNullableLong(rs, "disabled_at"));
            p.setCreatedAt(rs.getLong("created_at"));
            p.setUpdatedAt(rs.getLong("updated_at"));
            p.setVersion(rs.getLong("version"));
            return p;
        }
    }
}