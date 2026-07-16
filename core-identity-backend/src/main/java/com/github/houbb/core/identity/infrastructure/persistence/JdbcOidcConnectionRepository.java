package com.github.houbb.core.identity.infrastructure.persistence;

import com.github.houbb.core.identity.application.domain.OidcConnection;
import com.github.houbb.core.identity.application.port.OidcConnectionRepository;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

@Repository
public class JdbcOidcConnectionRepository implements OidcConnectionRepository {
    private final JdbcTemplate t;
    public JdbcOidcConnectionRepository(JdbcTemplate t) { this.t = t; }

    @Override
    public void save(OidcConnection c) {
        t.update("INSERT INTO identity_oidc_connection (connection_id,issuer,discovery_uri,client_id,encrypted_client_secret,secret_key_version,scopes_json,subject_claim,email_claim,name_claim,groups_claim,require_email_verified,userinfo_enabled,logout_endpoint,configuration_cache_json,configuration_fetched_at,created_at,updated_at,version) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
            c.getConnectionId(),c.getIssuer(),c.getDiscoveryUri(),c.getClientId(),c.getEncryptedClientSecret(),c.getSecretKeyVersion(),c.getScopesJson(),c.getSubjectClaim(),c.getEmailClaim(),c.getNameClaim(),c.getGroupsClaim(),c.getRequireEmailVerified(),c.getUserinfoEnabled(),c.getLogoutEndpoint(),c.getConfigurationCacheJson(),c.getConfigurationFetchedAt(),c.getCreatedAt(),c.getUpdatedAt(),c.getVersion());
    }

    @Override
    public Optional<OidcConnection> findById(String connectionId) {
        try { return Optional.ofNullable(t.queryForObject("SELECT * FROM identity_oidc_connection WHERE connection_id=?",new OidcConnectionRowMapper(),connectionId)); }
        catch (EmptyResultDataAccessException e) { return Optional.empty(); }
    }

    @Override
    public void update(OidcConnection c) {
        t.update("UPDATE identity_oidc_connection SET issuer=?,discovery_uri=?,client_id=?,encrypted_client_secret=?,secret_key_version=?,scopes_json=?,subject_claim=?,email_claim=?,name_claim=?,groups_claim=?,require_email_verified=?,userinfo_enabled=?,logout_endpoint=?,configuration_cache_json=?,configuration_fetched_at=?,updated_at=?,version=version+1 WHERE connection_id=? AND version=?",
            c.getIssuer(),c.getDiscoveryUri(),c.getClientId(),c.getEncryptedClientSecret(),c.getSecretKeyVersion(),c.getScopesJson(),c.getSubjectClaim(),c.getEmailClaim(),c.getNameClaim(),c.getGroupsClaim(),c.getRequireEmailVerified(),c.getUserinfoEnabled(),c.getLogoutEndpoint(),c.getConfigurationCacheJson(),c.getConfigurationFetchedAt(),c.getUpdatedAt(),c.getConnectionId(),c.getVersion());
    }

    @Override
    public void updateDiscoveryCache(String connectionId, String configurationCacheJson, long fetchedAt, long now, long version) {
        t.update("UPDATE identity_oidc_connection SET configuration_cache_json=?,configuration_fetched_at=?,updated_at=?,version=version+1 WHERE connection_id=? AND version=?",configurationCacheJson,fetchedAt,now,connectionId,version);
    }

    static class OidcConnectionRowMapper implements RowMapper<OidcConnection> {
        @Override
        public OidcConnection mapRow(ResultSet rs, int rowNum) throws SQLException {
            OidcConnection c=new OidcConnection();
            c.setConnectionId(rs.getString("connection_id"));c.setIssuer(rs.getString("issuer"));c.setDiscoveryUri(rs.getString("discovery_uri"));c.setClientId(rs.getString("client_id"));c.setEncryptedClientSecret(rs.getString("encrypted_client_secret"));c.setSecretKeyVersion(rs.getString("secret_key_version"));c.setScopesJson(rs.getString("scopes_json"));c.setSubjectClaim(rs.getString("subject_claim"));c.setEmailClaim(rs.getString("email_claim"));c.setNameClaim(rs.getString("name_claim"));c.setGroupsClaim(rs.getString("groups_claim"));c.setRequireEmailVerified(rs.getInt("require_email_verified"));c.setUserinfoEnabled(rs.getInt("userinfo_enabled"));c.setLogoutEndpoint(rs.getString("logout_endpoint"));c.setConfigurationCacheJson(rs.getString("configuration_cache_json"));
            Object fetchedAt = rs.getObject("configuration_fetched_at");
            if (fetchedAt != null) { c.setConfigurationFetchedAt(((Number) fetchedAt).longValue()); }
            else { c.setConfigurationFetchedAt(null); }
            c.setCreatedAt(rs.getLong("created_at"));c.setUpdatedAt(rs.getLong("updated_at"));c.setVersion(rs.getLong("version"));
            return c;
        }
    }
}
