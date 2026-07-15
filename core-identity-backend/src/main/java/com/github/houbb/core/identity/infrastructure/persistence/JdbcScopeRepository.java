package com.github.houbb.core.identity.infrastructure.persistence;

import com.github.houbb.core.identity.application.domain.Scope;
import com.github.houbb.core.identity.application.port.ScopeRepository;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class JdbcScopeRepository implements ScopeRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcScopeRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void save(Scope scope) {
        jdbcTemplate.update(
                "INSERT INTO identity_scope (id, scope_code, source_service, audience_code, name, " +
                "description, risk_level, consent_display, assignable, status, " +
                "created_at, updated_at, version) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                scope.getId(), scope.getScopeCode(), scope.getSourceService(),
                scope.getAudienceCode(), scope.getName(), scope.getDescription(),
                scope.getRiskLevel(), scope.getConsentDisplay(), scope.getAssignable(),
                scope.getStatus(), scope.getCreatedAt(), scope.getUpdatedAt(), scope.getVersion()
        );
    }

    @Override
    public Optional<Scope> findById(String id) {
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(
                    "SELECT * FROM identity_scope WHERE id = ?", new ScopeRowMapper(), id));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<Scope> findByCode(String scopeCode) {
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(
                    "SELECT * FROM identity_scope WHERE scope_code = ?",
                    new ScopeRowMapper(), scopeCode));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<Scope> findByService(String sourceService) {
        return jdbcTemplate.query(
                "SELECT * FROM identity_scope WHERE source_service = ? ORDER BY scope_code",
                new ScopeRowMapper(), sourceService);
    }

    @Override
    public List<Scope> findByIds(List<String> ids) {
        if (ids == null || ids.isEmpty()) {
            return new ArrayList<>();
        }
        StringBuilder sql = new StringBuilder("SELECT * FROM identity_scope WHERE id IN (");
        List<Object> params = new ArrayList<>();
        for (int i = 0; i < ids.size(); i++) {
            if (i > 0) sql.append(", ");
            sql.append("?");
            params.add(ids.get(i));
        }
        sql.append(") ORDER BY scope_code");
        return jdbcTemplate.query(sql.toString(), new ScopeRowMapper(), params.toArray());
    }

    @Override
    public List<Scope> findAllAssignable() {
        return jdbcTemplate.query(
                "SELECT * FROM identity_scope WHERE assignable = 1 AND status = 'ACTIVE' ORDER BY scope_code",
                new ScopeRowMapper());
    }

    @Override
    public List<Scope> findAll() {
        return jdbcTemplate.query(
                "SELECT * FROM identity_scope ORDER BY source_service, scope_code",
                new ScopeRowMapper());
    }

    @Override
    public void update(Scope scope) {
        jdbcTemplate.update(
                "UPDATE identity_scope SET name = ?, description = ?, risk_level = ?, " +
                "consent_display = ?, assignable = ?, status = ?, " +
                "audience_code = ?, updated_at = ?, version = version + 1 " +
                "WHERE id = ? AND version = ?",
                scope.getName(), scope.getDescription(), scope.getRiskLevel(),
                scope.getConsentDisplay(), scope.getAssignable(), scope.getStatus(),
                scope.getAudienceCode(), scope.getUpdatedAt(),
                scope.getId(), scope.getVersion()
        );
    }

    @Override
    public void updateStatus(String id, String status, long now, long version) {
        jdbcTemplate.update(
                "UPDATE identity_scope SET status = ?, updated_at = ?, version = version + 1 " +
                "WHERE id = ? AND version = ?",
                status, now, id, version);
    }

    static class ScopeRowMapper implements RowMapper<Scope> {
        @Override
        public Scope mapRow(ResultSet rs, int rowNum) throws SQLException {
            Scope s = new Scope();
            s.setId(rs.getString("id"));
            s.setScopeCode(rs.getString("scope_code"));
            s.setSourceService(rs.getString("source_service"));
            s.setAudienceCode(rs.getString("audience_code"));
            s.setName(rs.getString("name"));
            s.setDescription(rs.getString("description"));
            s.setRiskLevel(rs.getString("risk_level"));
            s.setConsentDisplay(rs.getString("consent_display"));
            s.setAssignable(rs.getInt("assignable"));
            s.setStatus(rs.getString("status"));
            s.setCreatedAt(rs.getLong("created_at"));
            s.setUpdatedAt(rs.getLong("updated_at"));
            s.setVersion(rs.getLong("version"));
            return s;
        }
    }
}