package com.github.houbb.core.identity.infrastructure.persistence;

import com.github.houbb.core.identity.application.domain.OAuthClient;
import com.github.houbb.core.identity.application.port.OAuthClientRepository;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Repository
public class JdbcOAuthClientRepository implements OAuthClientRepository {
    private final JdbcTemplate t;
    public JdbcOAuthClientRepository(JdbcTemplate t) { this.t = t; }

    @Override
    public void save(OAuthClient c) {
        t.update("INSERT INTO identity_oauth_client (id,client_id,owner_type,owner_id,client_type,name,description,homepage_url,logo_object_id,privacy_policy_url,terms_url,status,review_status,consent_required,created_by,created_at,updated_at,version) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
            c.getId(),c.getClientId(),c.getOwnerType(),c.getOwnerId(),c.getClientType(),c.getName(),c.getDescription(),c.getHomepageUrl(),c.getLogoObjectId(),c.getPrivacyPolicyUrl(),c.getTermsUrl(),c.getStatus(),c.getReviewStatus(),c.getConsentRequired(),c.getCreatedBy(),c.getCreatedAt(),c.getUpdatedAt(),c.getVersion());
    }
    @Override
    public Optional<OAuthClient> findById(String id) {
        try { return Optional.ofNullable(t.queryForObject("SELECT * FROM identity_oauth_client WHERE id=?",new OAuthClientRowMapper(),id)); }
        catch (EmptyResultDataAccessException e) { return Optional.empty(); }
    }
    @Override
    public Optional<OAuthClient> findByClientId(String clientId) {
        try { return Optional.ofNullable(t.queryForObject("SELECT * FROM identity_oauth_client WHERE client_id=?",new OAuthClientRowMapper(),clientId)); }
        catch (EmptyResultDataAccessException e) { return Optional.empty(); }
    }
    @Override
    public List<OAuthClient> findByOwner(String ownerType, String ownerId) {
        return t.query("SELECT * FROM identity_oauth_client WHERE owner_type=? AND owner_id=? ORDER BY created_at",new OAuthClientRowMapper(),ownerType,ownerId);
    }
    @Override
    public List<OAuthClient> findAll() {
        return t.query("SELECT * FROM identity_oauth_client ORDER BY created_at",new OAuthClientRowMapper());
    }
    @Override
    public List<OAuthClient> findByStatus(String status) {
        return t.query("SELECT * FROM identity_oauth_client WHERE status=? ORDER BY created_at",new OAuthClientRowMapper(),status);
    }
    @Override
    public void update(OAuthClient c) {
        t.update("UPDATE identity_oauth_client SET name=?,description=?,homepage_url=?,logo_object_id=?,privacy_policy_url=?,terms_url=?,status=?,review_status=?,consent_required=?,updated_at=?,version=version+1 WHERE id=? AND version=?",
            c.getName(),c.getDescription(),c.getHomepageUrl(),c.getLogoObjectId(),c.getPrivacyPolicyUrl(),c.getTermsUrl(),c.getStatus(),c.getReviewStatus(),c.getConsentRequired(),c.getUpdatedAt(),c.getId(),c.getVersion());
    }
    @Override
    public void updateStatus(String id, String status, String reviewStatus, long now, long version) {
        t.update("UPDATE identity_oauth_client SET status=?,review_status=?,updated_at=?,version=version+1 WHERE id=? AND version=?",status,reviewStatus,now,id,version);
    }
    static class OAuthClientRowMapper implements RowMapper<OAuthClient> {
        @Override
        public OAuthClient mapRow(ResultSet rs, int rowNum) throws SQLException {
            OAuthClient c=new OAuthClient();
            c.setId(rs.getString("id"));c.setClientId(rs.getString("client_id"));c.setOwnerType(rs.getString("owner_type"));c.setOwnerId(rs.getString("owner_id"));c.setClientType(rs.getString("client_type"));c.setName(rs.getString("name"));c.setDescription(rs.getString("description"));c.setHomepageUrl(rs.getString("homepage_url"));c.setLogoObjectId(rs.getString("logo_object_id"));c.setPrivacyPolicyUrl(rs.getString("privacy_policy_url"));c.setTermsUrl(rs.getString("terms_url"));c.setStatus(rs.getString("status"));c.setReviewStatus(rs.getString("review_status"));c.setConsentRequired(rs.getInt("consent_required"));c.setCreatedBy(rs.getString("created_by"));c.setCreatedAt(rs.getLong("created_at"));c.setUpdatedAt(rs.getLong("updated_at"));c.setVersion(rs.getLong("version"));
            return c;
        }
    }
}