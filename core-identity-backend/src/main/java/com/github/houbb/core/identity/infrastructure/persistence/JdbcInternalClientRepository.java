package com.github.houbb.core.identity.infrastructure.persistence;

import com.github.houbb.core.identity.application.domain.InternalClient;
import com.github.houbb.core.identity.application.port.InternalClientRepository;
import com.github.houbb.core.identity.infrastructure.json.JsonUtils;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * JDBC implementation of InternalClientRepository.
 */
@Repository
public class JdbcInternalClientRepository implements InternalClientRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcInternalClientRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void save(InternalClient client) {
        jdbcTemplate.update(
                "INSERT INTO identity_internal_client (id, client_id, client_secret_hash, display_name, " +
                "client_type, scopes_json, status, expires_at, last_used_at, created_at, updated_at, version) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                client.getId(), client.getClientId(), client.getClientSecretHash(),
                client.getDisplayName(), client.getClientType(),
                JsonUtils.toJson(client.getScopes()),
                client.getStatus(), client.getExpiresAt(), client.getLastUsedAt(),
                client.getCreatedAt(), client.getUpdatedAt(), client.getVersion()
        );
    }

    @Override
    public Optional<InternalClient> findByClientId(String clientId) {
        try {
            InternalClient client = jdbcTemplate.queryForObject(
                    "SELECT * FROM identity_internal_client WHERE client_id = ?",
                    new InternalClientRowMapper(), clientId
            );
            return Optional.ofNullable(client);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public void updateLastUsedAt(String clientId, long timestamp) {
        jdbcTemplate.update(
                "UPDATE identity_internal_client SET last_used_at = ?, updated_at = ? WHERE client_id = ?",
                timestamp, timestamp, clientId
        );
    }

    static class InternalClientRowMapper implements RowMapper<InternalClient> {
        @Override
        public InternalClient mapRow(ResultSet rs, int rowNum) throws SQLException {
            InternalClient c = new InternalClient();
            c.setId(rs.getString("id"));
            c.setClientId(rs.getString("client_id"));
            c.setClientSecretHash(rs.getString("client_secret_hash"));
            c.setDisplayName(rs.getString("display_name"));
            c.setClientType(rs.getString("client_type"));
            c.setScopes(JsonUtils.fromJsonList(rs.getString("scopes_json")));
            c.setStatus(rs.getString("status"));
            c.setExpiresAt(rs.getObject("expires_at", Long.class));
            c.setLastUsedAt(rs.getObject("last_used_at", Long.class));
            c.setCreatedAt(rs.getLong("created_at"));
            c.setUpdatedAt(rs.getLong("updated_at"));
            c.setVersion(rs.getLong("version"));
            return c;
        }
    }
}