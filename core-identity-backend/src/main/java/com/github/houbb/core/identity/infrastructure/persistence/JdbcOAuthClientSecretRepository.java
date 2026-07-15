package com.github.houbb.core.identity.infrastructure.persistence;

import com.github.houbb.core.identity.application.domain.OAuthClientSecret;
import com.github.houbb.core.identity.application.port.OAuthClientSecretRepository;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Repository
public class JdbcOAuthClientSecretRepository implements OAuthClientSecretRepository {
    private final JdbcTemplate t;
    public JdbcOAuthClientSecretRepository(JdbcTemplate t) { this.t = t; }

    @Override
    public void save(OAuthClientSecret s) {
        t.update("INSERT INTO identity_oauth_client_secret (id,client_id,secret_prefix,secret_hash,name,status,expires_at,last_used_at,created_at,revoked_at) VALUES (?,?,?,?,?,?,?,?,?,?)",
            s.getId(),s.getClientId(),s.getSecretPrefix(),s.getSecretHash(),s.getName(),s.getStatus(),s.getExpiresAt(),s.getLastUsedAt(),s.getCreatedAt(),s.getRevokedAt());
    }
    @Override
    public Optional<OAuthClientSecret> findById(String id) {
        try { return Optional.ofNullable(t.queryForObject("SELECT * FROM identity_oauth_client_secret WHERE id=?",new RowMapper<>(){
            @Override public OAuthClientSecret mapRow(ResultSet rs,int r) throws SQLException { return map(rs); }},id)); }
        catch (EmptyResultDataAccessException e) { return Optional.empty(); }
    }
    @Override
    public List<OAuthClientSecret> findByClientId(String clientId) {
        return t.query("SELECT * FROM identity_oauth_client_secret WHERE client_id=? ORDER BY created_at",(rs,r)->map(rs),clientId);
    }
    @Override
    public Optional<OAuthClientSecret> findActiveByClientId(String clientId) {
        try { return Optional.ofNullable(t.queryForObject("SELECT * FROM identity_oauth_client_secret WHERE client_id=? AND status='ACTIVE' ORDER BY created_at DESC LIMIT 1",(rs,r)->map(rs),clientId)); }
        catch (EmptyResultDataAccessException e) { return Optional.empty(); }
    }
    @Override
    public void update(OAuthClientSecret s) {
        t.update("UPDATE identity_oauth_client_secret SET status=?,last_used_at=?,revoked_at=?,expires_at=? WHERE id=?",s.getStatus(),s.getLastUsedAt(),s.getRevokedAt(),s.getExpiresAt(),s.getId());
    }
    @Override
    public void revokeByClientId(String clientId, long now) {
        t.update("UPDATE identity_oauth_client_secret SET status='REVOKED',revoked_at=? WHERE client_id=? AND status='ACTIVE'",now,clientId);
    }
    @Override
    public void updateLastUsedAt(String id, long lastUsedAt) {
        t.update("UPDATE identity_oauth_client_secret SET last_used_at=? WHERE id=?",lastUsedAt,id);
    }
    private OAuthClientSecret map(ResultSet rs) throws SQLException {
        OAuthClientSecret s=new OAuthClientSecret();
        s.setId(rs.getString("id"));s.setClientId(rs.getString("client_id"));s.setSecretPrefix(rs.getString("secret_prefix"));s.setSecretHash(rs.getString("secret_hash"));s.setName(rs.getString("name"));s.setStatus(rs.getString("status"));s.setExpiresAt((Long)rs.getObject("expires_at"));s.setLastUsedAt((Long)rs.getObject("last_used_at"));s.setCreatedAt(rs.getLong("created_at"));s.setRevokedAt((Long)rs.getObject("revoked_at"));
        return s;
    }
}