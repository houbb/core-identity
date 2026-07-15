package com.github.houbb.core.identity.infrastructure.persistence;

import com.github.houbb.core.identity.application.domain.Audience;
import com.github.houbb.core.identity.application.port.AudienceRepository;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Repository
public class JdbcAudienceRepository implements AudienceRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcAudienceRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void save(Audience audience) {
        jdbcTemplate.update(
                "INSERT INTO identity_audience (id, audience_code, service_name, description, " +
                "issuer_allowed, token_ttl_seconds, status, created_at, updated_at, version) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                audience.getId(), audience.getAudienceCode(), audience.getServiceName(),
                audience.getDescription(), audience.getIssuerAllowed(),
                audience.getTokenTtlSeconds(), audience.getStatus(),
                audience.getCreatedAt(), audience.getUpdatedAt(), audience.getVersion()
        );
    }

    @Override
    public Optional<Audience> findById(String id) {
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(
                    "SELECT * FROM identity_audience WHERE id = ?", new AudienceRowMapper(), id));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<Audience> findByCode(String audienceCode) {
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(
                    "SELECT * FROM identity_audience WHERE audience_code = ?",
                    new AudienceRowMapper(), audienceCode));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<Audience> findAllActive() {
        return jdbcTemplate.query(
                "SELECT * FROM identity_audience WHERE status = 'ACTIVE' ORDER BY audience_code",
                new AudienceRowMapper());
    }

    @Override
    public List<Audience> findAll() {
        return jdbcTemplate.query(
                "SELECT * FROM identity_audience ORDER BY audience_code",
                new AudienceRowMapper());
    }

    @Override
    public void update(Audience audience) {
        jdbcTemplate.update(
                "UPDATE identity_audience SET service_name = ?, description = ?, " +
                "issuer_allowed = ?, token_ttl_seconds = ?, status = ?, " +
                "updated_at = ?, version = version + 1 WHERE id = ? AND version = ?",
                audience.getServiceName(), audience.getDescription(),
                audience.getIssuerAllowed(), audience.getTokenTtlSeconds(),
                audience.getStatus(), audience.getUpdatedAt(),
                audience.getId(), audience.getVersion()
        );
    }

    @Override
    public void updateStatus(String id, String status, long now, long version) {
        jdbcTemplate.update(
                "UPDATE identity_audience SET status = ?, updated_at = ?, version = version + 1 " +
                "WHERE id = ? AND version = ?",
                status, now, id, version);
    }

    static class AudienceRowMapper implements RowMapper<Audience> {
        @Override
        public Audience mapRow(ResultSet rs, int rowNum) throws SQLException {
            Audience a = new Audience();
            a.setId(rs.getString("id"));
            a.setAudienceCode(rs.getString("audience_code"));
            a.setServiceName(rs.getString("service_name"));
            a.setDescription(rs.getString("description"));
            a.setIssuerAllowed(rs.getInt("issuer_allowed"));
            a.setTokenTtlSeconds(rs.getInt("token_ttl_seconds"));
            a.setStatus(rs.getString("status"));
            a.setCreatedAt(rs.getLong("created_at"));
            a.setUpdatedAt(rs.getLong("updated_at"));
            a.setVersion(rs.getLong("version"));
            return a;
        }
    }
}