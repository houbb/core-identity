package com.github.houbb.core.identity.infrastructure.persistence;

import com.github.houbb.core.identity.application.domain.*;
import com.github.houbb.core.identity.application.port.*;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

@Repository
class JdbcAuthorizationCodeRepository implements AuthorizationCodeRepository {
    private final JdbcTemplate t;
    JdbcAuthorizationCodeRepository(JdbcTemplate t) { this.t = t; }
    @Override public void save(AuthorizationCode c) {
        t.update("INSERT INTO identity_authorization_code (id,code_hash,client_id,user_id,organization_id,redirect_uri,audience,scopes_json,code_challenge,code_challenge_method,nonce,status,expires_at,created_at,version) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
            c.getId(),c.getCodeHash(),c.getClientId(),c.getUserId(),c.getOrganizationId(),c.getRedirectUri(),c.getAudience(),c.getScopesJson(),c.getCodeChallenge(),c.getCodeChallengeMethod(),c.getNonce(),c.getStatus(),c.getExpiresAt(),c.getCreatedAt(),c.getVersion());
    }
    @Override public Optional<AuthorizationCode> findByCodeHash(String hash) {
        try { return Optional.ofNullable(t.queryForObject("SELECT * FROM identity_authorization_code WHERE code_hash=?",new AuthCodeMapper(),hash)); }
        catch (EmptyResultDataAccessException e) { return Optional.empty(); }
    }
    @Override public void markUsed(String id, long now) { t.update("UPDATE identity_authorization_code SET status='USED',used_at=?,version=version+1 WHERE id=?",now,id); }
    @Override public void deleteExpired(long before) { t.update("DELETE FROM identity_authorization_code WHERE expires_at<?",before); }
    static class AuthCodeMapper implements RowMapper<AuthorizationCode> {
        @Override public AuthorizationCode mapRow(ResultSet rs,int r) throws SQLException {
            AuthorizationCode c=new AuthorizationCode();
            c.setId(rs.getString("id"));c.setCodeHash(rs.getString("code_hash"));c.setClientId(rs.getString("client_id"));c.setUserId(rs.getString("user_id"));c.setOrganizationId(rs.getString("organization_id"));c.setRedirectUri(rs.getString("redirect_uri"));c.setAudience(rs.getString("audience"));c.setScopesJson(rs.getString("scopes_json"));c.setCodeChallenge(rs.getString("code_challenge"));c.setCodeChallengeMethod(rs.getString("code_challenge_method"));c.setNonce(rs.getString("nonce"));c.setStatus(rs.getString("status"));c.setExpiresAt(rs.getLong("expires_at"));c.setUsedAt((Long)rs.getObject("used_at"));c.setCreatedAt(rs.getLong("created_at"));c.setVersion(rs.getLong("version"));
            return c;
        }
    }
}

@Repository
class JdbcRefreshTokenRepository implements RefreshTokenRepository {
    private final JdbcTemplate t;
    JdbcRefreshTokenRepository(JdbcTemplate t) { this.t = t; }
    @Override public void save(RefreshToken rt) {
        t.update("INSERT INTO identity_refresh_token (id,family_id,token_hash,status,expires_at,created_at,version) VALUES (?,?,?,?,?,?,?)",
            rt.getId(),rt.getFamilyId(),rt.getTokenHash(),rt.getStatus(),rt.getExpiresAt(),rt.getCreatedAt(),rt.getVersion());
    }
    @Override public Optional<RefreshToken> findByTokenHash(String hash) {
        try { return Optional.ofNullable(t.queryForObject("SELECT * FROM identity_refresh_token WHERE token_hash=?",new RTMapper(),hash)); }
        catch (EmptyResultDataAccessException e) { return Optional.empty(); }
    }
    @Override public Optional<RefreshToken> findById(String id) {
        try { return Optional.ofNullable(t.queryForObject("SELECT * FROM identity_refresh_token WHERE id=?",new RTMapper(),id)); }
        catch (EmptyResultDataAccessException e) { return Optional.empty(); }
    }
    @Override public void update(RefreshToken rt) {
        t.update("UPDATE identity_refresh_token SET status=?,used_at=?,replaced_by_id=?,version=version+1 WHERE id=? AND version=?",
            rt.getStatus(),rt.getUsedAt(),rt.getReplacedById(),rt.getId(),rt.getVersion());
    }
    @Override public List<RefreshToken> findByFamilyId(String familyId) { return t.query("SELECT * FROM identity_refresh_token WHERE family_id=? ORDER BY created_at",new RTMapper(),familyId); }
    @Override public void deleteExpired(long before) { t.update("DELETE FROM identity_refresh_token WHERE expires_at<?",before); }
    static class RTMapper implements RowMapper<RefreshToken> {
        @Override public RefreshToken mapRow(ResultSet rs,int r) throws SQLException {
            RefreshToken rt=new RefreshToken();
            rt.setId(rs.getString("id"));rt.setFamilyId(rs.getString("family_id"));rt.setTokenHash(rs.getString("token_hash"));rt.setStatus(rs.getString("status"));rt.setExpiresAt(rs.getLong("expires_at"));rt.setUsedAt((Long)rs.getObject("used_at"));rt.setReplacedById(rs.getString("replaced_by_id"));rt.setCreatedAt(rs.getLong("created_at"));rt.setVersion(rs.getLong("version"));
            return rt;
        }
    }
}

@Repository
class JdbcRefreshTokenFamilyRepository implements RefreshTokenFamilyRepository {
    private final JdbcTemplate t;
    JdbcRefreshTokenFamilyRepository(JdbcTemplate t) { this.t = t; }
    @Override public void save(RefreshTokenFamily f) {
        t.update("INSERT INTO identity_refresh_token_family (id,grant_id,client_id,user_id,session_id,status,created_at) VALUES (?,?,?,?,?,?,?)",
            f.getId(),f.getGrantId(),f.getClientId(),f.getUserId(),f.getSessionId(),f.getStatus(),f.getCreatedAt());
    }
    @Override public Optional<RefreshTokenFamily> findById(String id) {
        try { return Optional.ofNullable(t.queryForObject("SELECT * FROM identity_refresh_token_family WHERE id=?",new RTFMapper(),id)); }
        catch (EmptyResultDataAccessException e) { return Optional.empty(); }
    }
    @Override public void update(RefreshTokenFamily f) {
        t.update("UPDATE identity_refresh_token_family SET status=?,revoked_reason=?,revoked_at=? WHERE id=?",f.getStatus(),f.getRevokedReason(),f.getRevokedAt(),f.getId());
    }
    @Override public List<RefreshTokenFamily> findByUserId(String userId) { return t.query("SELECT * FROM identity_refresh_token_family WHERE user_id=? ORDER BY created_at",new RTFMapper(),userId); }
    @Override public List<RefreshTokenFamily> findByGrantId(String grantId) { return t.query("SELECT * FROM identity_refresh_token_family WHERE grant_id=? ORDER BY created_at",new RTFMapper(),grantId); }
    static class RTFMapper implements RowMapper<RefreshTokenFamily> {
        @Override public RefreshTokenFamily mapRow(ResultSet rs,int r) throws SQLException {
            RefreshTokenFamily f=new RefreshTokenFamily();
            f.setId(rs.getString("id"));f.setGrantId(rs.getString("grant_id"));f.setClientId(rs.getString("client_id"));f.setUserId(rs.getString("user_id"));f.setSessionId(rs.getString("session_id"));f.setStatus(rs.getString("status"));f.setRevokedReason(rs.getString("revoked_reason"));f.setCreatedAt(rs.getLong("created_at"));f.setRevokedAt((Long)rs.getObject("revoked_at"));
            return f;
        }
    }
}

@Repository
class JdbcAuthorizationGrantRepository implements AuthorizationGrantRepository {
    private final JdbcTemplate t;
    JdbcAuthorizationGrantRepository(JdbcTemplate t) { this.t = t; }
    @Override public void save(AuthorizationGrant g) {
        t.update("INSERT INTO identity_authorization_grant (id,client_id,user_id,organization_id,status,first_granted_at,created_at,updated_at,version) VALUES (?,?,?,?,?,?,?,?,?)",
            g.getId(),g.getClientId(),g.getUserId(),g.getOrganizationId(),g.getStatus(),g.getFirstGrantedAt(),g.getCreatedAt(),g.getUpdatedAt(),g.getVersion());
    }
    @Override public Optional<AuthorizationGrant> findById(String id) {
        try { return Optional.ofNullable(t.queryForObject("SELECT * FROM identity_authorization_grant WHERE id=?",new GrantMapper(),id)); }
        catch (EmptyResultDataAccessException e) { return Optional.empty(); }
    }
    @Override public Optional<AuthorizationGrant> findByUserAndClient(String userId, String clientId) {
        try { return Optional.ofNullable(t.queryForObject("SELECT * FROM identity_authorization_grant WHERE user_id=? AND client_id=?",new GrantMapper(),userId,clientId)); }
        catch (EmptyResultDataAccessException e) { return Optional.empty(); }
    }
    @Override public List<AuthorizationGrant> findByUserId(String userId) { return t.query("SELECT * FROM identity_authorization_grant WHERE user_id=? ORDER BY created_at",new GrantMapper(),userId); }
    @Override public void update(AuthorizationGrant g) {
        t.update("UPDATE identity_authorization_grant SET status=?,last_used_at=?,revoked_at=?,updated_at=?,version=version+1 WHERE id=? AND version=?",
            g.getStatus(),g.getLastUsedAt(),g.getRevokedAt(),g.getUpdatedAt(),g.getId(),g.getVersion());
    }
    static class GrantMapper implements RowMapper<AuthorizationGrant> {
        @Override public AuthorizationGrant mapRow(ResultSet rs,int r) throws SQLException {
            AuthorizationGrant g=new AuthorizationGrant();
            g.setId(rs.getString("id"));g.setClientId(rs.getString("client_id"));g.setUserId(rs.getString("user_id"));g.setOrganizationId(rs.getString("organization_id"));g.setStatus(rs.getString("status"));g.setFirstGrantedAt(rs.getLong("first_granted_at"));g.setLastUsedAt((Long)rs.getObject("last_used_at"));g.setRevokedAt((Long)rs.getObject("revoked_at"));g.setCreatedAt(rs.getLong("created_at"));g.setUpdatedAt(rs.getLong("updated_at"));g.setVersion(rs.getLong("version"));
            return g;
        }
    }
}

@Repository
class JdbcApiKeyRepository implements ApiKeyRepository {
    private final JdbcTemplate t;
    JdbcApiKeyRepository(JdbcTemplate t) { this.t = t; }
    @Override public void save(ApiKey k) {
        t.update("INSERT INTO identity_api_key (id,key_prefix,key_hash,name,owner_type,owner_id,organization_id,status,expires_at,created_at,version) VALUES (?,?,?,?,?,?,?,?,?,?,?)",
            k.getId(),k.getKeyPrefix(),k.getKeyHash(),k.getName(),k.getOwnerType(),k.getOwnerId(),k.getOrganizationId(),k.getStatus(),k.getExpiresAt(),k.getCreatedAt(),k.getVersion());
    }
    @Override public Optional<ApiKey> findById(String id) {
        try { return Optional.ofNullable(t.queryForObject("SELECT * FROM identity_api_key WHERE id=?",new ApiKeyMapper(),id)); }
        catch (EmptyResultDataAccessException e) { return Optional.empty(); }
    }
    @Override public Optional<ApiKey> findByPrefix(String prefix) {
        try { return Optional.ofNullable(t.queryForObject("SELECT * FROM identity_api_key WHERE key_prefix=? AND status='ACTIVE' LIMIT 1",new ApiKeyMapper(),prefix)); }
        catch (EmptyResultDataAccessException e) { return Optional.empty(); }
    }
    @Override public Optional<ApiKey> findByHash(String hash) {
        try { return Optional.ofNullable(t.queryForObject("SELECT * FROM identity_api_key WHERE key_hash=?",new ApiKeyMapper(),hash)); }
        catch (EmptyResultDataAccessException e) { return Optional.empty(); }
    }
    @Override public List<ApiKey> findByOwner(String ownerType, String ownerId) { return t.query("SELECT * FROM identity_api_key WHERE owner_type=? AND owner_id=? ORDER BY created_at",new ApiKeyMapper(),ownerType,ownerId); }
    @Override public List<ApiKey> findByOrg(String orgId) { return t.query("SELECT * FROM identity_api_key WHERE organization_id=? ORDER BY created_at",new ApiKeyMapper(),orgId); }
    @Override public void update(ApiKey k) {
        t.update("UPDATE identity_api_key SET status=?,last_used_at=?,last_used_ip=?,revoked_at=?,version=version+1 WHERE id=? AND version=?",k.getStatus(),k.getLastUsedAt(),k.getLastUsedIp(),k.getRevokedAt(),k.getId(),k.getVersion());
    }
    @Override public void revoke(String id, long now) { t.update("UPDATE identity_api_key SET status='REVOKED',revoked_at=? WHERE id=?",now,id); }
    static class ApiKeyMapper implements RowMapper<ApiKey> {
        @Override public ApiKey mapRow(ResultSet rs,int r) throws SQLException {
            ApiKey k=new ApiKey();
            k.setId(rs.getString("id"));k.setKeyPrefix(rs.getString("key_prefix"));k.setKeyHash(rs.getString("key_hash"));k.setName(rs.getString("name"));k.setOwnerType(rs.getString("owner_type"));k.setOwnerId(rs.getString("owner_id"));k.setOrganizationId(rs.getString("organization_id"));k.setStatus(rs.getString("status"));k.setExpiresAt((Long)rs.getObject("expires_at"));k.setLastUsedAt((Long)rs.getObject("last_used_at"));k.setLastUsedIp(rs.getString("last_used_ip"));k.setCreatedAt(rs.getLong("created_at"));k.setRevokedAt((Long)rs.getObject("revoked_at"));k.setVersion(rs.getLong("version"));
            return k;
        }
    }
}

@Repository
class JdbcServiceAccountRepository implements ServiceAccountRepository {
    private final JdbcTemplate t;
    JdbcServiceAccountRepository(JdbcTemplate t) { this.t = t; }
    @Override public void save(ServiceAccount sa) {
        t.update("INSERT INTO identity_service_account (id,organization_id,account_type,name,description,status,last_used_at,created_by,created_at,updated_at,version) VALUES (?,?,?,?,?,?,?,?,?,?,?)",
            sa.getId(),sa.getOrganizationId(),sa.getAccountType(),sa.getName(),sa.getDescription(),sa.getStatus(),sa.getLastUsedAt(),sa.getCreatedBy(),sa.getCreatedAt(),sa.getUpdatedAt(),sa.getVersion());
    }
    @Override public Optional<ServiceAccount> findById(String id) {
        try { return Optional.ofNullable(t.queryForObject("SELECT * FROM identity_service_account WHERE id=?",new SAMapper(),id)); }
        catch (EmptyResultDataAccessException e) { return Optional.empty(); }
    }
    @Override public List<ServiceAccount> findByOrg(String orgId) { return t.query("SELECT * FROM identity_service_account WHERE organization_id=? ORDER BY created_at",new SAMapper(),orgId); }
    @Override public void update(ServiceAccount sa) {
        t.update("UPDATE identity_service_account SET name=?,description=?,status=?,last_used_at=?,updated_at=?,version=version+1 WHERE id=? AND version=?",
            sa.getName(),sa.getDescription(),sa.getStatus(),sa.getLastUsedAt(),sa.getUpdatedAt(),sa.getId(),sa.getVersion());
    }
    static class SAMapper implements RowMapper<ServiceAccount> {
        @Override public ServiceAccount mapRow(ResultSet rs,int r) throws SQLException {
            ServiceAccount sa=new ServiceAccount();
            sa.setId(rs.getString("id"));sa.setOrganizationId(rs.getString("organization_id"));sa.setAccountType(rs.getString("account_type"));sa.setName(rs.getString("name"));sa.setDescription(rs.getString("description"));sa.setStatus(rs.getString("status"));sa.setLastUsedAt((Long)rs.getObject("last_used_at"));sa.setCreatedBy(rs.getString("created_by"));sa.setCreatedAt(rs.getLong("created_at"));sa.setUpdatedAt(rs.getLong("updated_at"));sa.setVersion(rs.getLong("version"));
            return sa;
        }
    }
}

@Repository
class JdbcAuthorizationGrantScopeRepository implements AuthorizationGrantScopeRepository {
    private final JdbcTemplate t;
    JdbcAuthorizationGrantScopeRepository(JdbcTemplate t) { this.t = t; }
    @Override public void save(AuthorizationGrantScope gs) {
        t.update("INSERT INTO identity_authorization_grant_scope (id,grant_id,scope_id,created_at) VALUES (?,?,?,?)",
            gs.getId(), gs.getGrantId(), gs.getScopeId(), gs.getCreatedAt());
    }
    @Override public void saveAll(List<AuthorizationGrantScope> grantScopes) {
        for (AuthorizationGrantScope gs : grantScopes) save(gs);
    }
    @Override public List<AuthorizationGrantScope> findByGrantId(String grantId) {
        return t.query("SELECT * FROM identity_authorization_grant_scope WHERE grant_id=? ORDER BY created_at",
            new GrantScopeMapper(), grantId);
    }
    @Override public List<AuthorizationGrantScope> findByGrantIds(List<String> grantIds) {
        if (grantIds.isEmpty()) return List.of();
        String placeholders = String.join(",", Collections.nCopies(grantIds.size(), "?"));
        return t.query("SELECT * FROM identity_authorization_grant_scope WHERE grant_id IN (" + placeholders + ") ORDER BY created_at",
            new GrantScopeMapper(), grantIds.toArray());
    }
    @Override public void deleteByGrantId(String grantId) {
        t.update("DELETE FROM identity_authorization_grant_scope WHERE grant_id=?", grantId);
    }
    @Override public void deleteByGrantIdAndScopeId(String grantId, String scopeId) {
        t.update("DELETE FROM identity_authorization_grant_scope WHERE grant_id=? AND scope_id=?", grantId, scopeId);
    }
    static class GrantScopeMapper implements RowMapper<AuthorizationGrantScope> {
        @Override public AuthorizationGrantScope mapRow(ResultSet rs, int r) throws SQLException {
            AuthorizationGrantScope gs = new AuthorizationGrantScope();
            gs.setId(rs.getString("id")); gs.setGrantId(rs.getString("grant_id"));
            gs.setScopeId(rs.getString("scope_id")); gs.setCreatedAt(rs.getLong("created_at"));
            return gs;
        }
    }
}

@Repository
class JdbcServiceAccountRoleRepository implements ServiceAccountRoleRepository {
    private final JdbcTemplate t;
    JdbcServiceAccountRoleRepository(JdbcTemplate t) { this.t = t; }
    @Override public void save(ServiceAccountRole role) {
        t.update("INSERT INTO identity_service_account_role (id,service_account_id,role_id,assigned_by,created_at) VALUES (?,?,?,?,?)",
            role.getId(), role.getServiceAccountId(), role.getRoleId(), role.getAssignedBy(), role.getCreatedAt());
    }
    @Override public List<ServiceAccountRole> findByServiceAccountId(String saId) {
        return t.query("SELECT * FROM identity_service_account_role WHERE service_account_id=? ORDER BY created_at",
            new SARoleMapper(), saId);
    }
    @Override public Optional<ServiceAccountRole> findByServiceAccountAndRole(String saId, String roleId) {
        try {
            return Optional.ofNullable(t.queryForObject(
                "SELECT * FROM identity_service_account_role WHERE service_account_id=? AND role_id=?",
                new SARoleMapper(), saId, roleId));
        } catch (EmptyResultDataAccessException e) { return Optional.empty(); }
    }
    @Override public void deleteByServiceAccountId(String saId) {
        t.update("DELETE FROM identity_service_account_role WHERE service_account_id=?", saId);
    }
    @Override public void deleteByServiceAccountIdAndRoleId(String saId, String roleId) {
        t.update("DELETE FROM identity_service_account_role WHERE service_account_id=? AND role_id=?", saId, roleId);
    }
    static class SARoleMapper implements RowMapper<ServiceAccountRole> {
        @Override public ServiceAccountRole mapRow(ResultSet rs, int r) throws SQLException {
            ServiceAccountRole role = new ServiceAccountRole();
            role.setId(rs.getString("id")); role.setServiceAccountId(rs.getString("service_account_id"));
            role.setRoleId(rs.getString("role_id")); role.setAssignedBy(rs.getString("assigned_by"));
            role.setCreatedAt(rs.getLong("created_at"));
            return role;
        }
    }
}

@Repository
class JdbcServiceCredentialRepository implements ServiceCredentialRepository {
    private final JdbcTemplate t;
    JdbcServiceCredentialRepository(JdbcTemplate t) { this.t = t; }
    @Override public void save(ServiceCredential c) {
        t.update("INSERT INTO identity_service_credential (id,service_account_id,client_id,secret_prefix,secret_hash,name,status,expires_at,created_at) VALUES (?,?,?,?,?,?,?,?,?)",
            c.getId(), c.getServiceAccountId(), c.getClientId(), c.getSecretPrefix(), c.getSecretHash(),
            c.getName(), c.getStatus(), c.getExpiresAt(), c.getCreatedAt());
    }
    @Override public Optional<ServiceCredential> findById(String id) {
        try { return Optional.ofNullable(t.queryForObject("SELECT * FROM identity_service_credential WHERE id=?", new SCMapper(), id)); }
        catch (EmptyResultDataAccessException e) { return Optional.empty(); }
    }
    @Override public List<ServiceCredential> findByServiceAccountId(String saId) {
        return t.query("SELECT * FROM identity_service_credential WHERE service_account_id=? ORDER BY created_at", new SCMapper(), saId);
    }
    @Override public Optional<ServiceCredential> findActiveByServiceAccountId(String saId) {
        try {
            return Optional.ofNullable(t.queryForObject(
                "SELECT * FROM identity_service_credential WHERE service_account_id=? AND status='ACTIVE' ORDER BY created_at DESC LIMIT 1",
                new SCMapper(), saId));
        } catch (EmptyResultDataAccessException e) { return Optional.empty(); }
    }
    @Override public Optional<ServiceCredential> findByClientId(String clientId) {
        try { return Optional.ofNullable(t.queryForObject("SELECT * FROM identity_service_credential WHERE client_id=?", new SCMapper(), clientId)); }
        catch (EmptyResultDataAccessException e) { return Optional.empty(); }
    }
    @Override public void update(ServiceCredential c) {
        t.update("UPDATE identity_service_credential SET status=?,last_used_at=?,revoked_at=? WHERE id=?",
            c.getStatus(), c.getLastUsedAt(), c.getRevokedAt(), c.getId());
    }
    @Override public void updateLastUsedAt(String id, long now) {
        t.update("UPDATE identity_service_credential SET last_used_at=? WHERE id=?", now, id);
    }
    @Override public void revokeByServiceAccountId(String saId, long now) {
        t.update("UPDATE identity_service_credential SET status='REVOKED',revoked_at=? WHERE service_account_id=? AND status='ACTIVE'", now, saId);
    }
    static class SCMapper implements RowMapper<ServiceCredential> {
        @Override public ServiceCredential mapRow(ResultSet rs, int r) throws SQLException {
            ServiceCredential c = new ServiceCredential();
            c.setId(rs.getString("id")); c.setServiceAccountId(rs.getString("service_account_id"));
            c.setClientId(rs.getString("client_id")); c.setSecretPrefix(rs.getString("secret_prefix"));
            c.setSecretHash(rs.getString("secret_hash")); c.setName(rs.getString("name"));
            c.setStatus(rs.getString("status")); c.setExpiresAt((Long)rs.getObject("expires_at"));
            c.setLastUsedAt((Long)rs.getObject("last_used_at")); c.setCreatedAt(rs.getLong("created_at"));
            c.setRevokedAt((Long)rs.getObject("revoked_at"));
            return c;
        }
    }
}