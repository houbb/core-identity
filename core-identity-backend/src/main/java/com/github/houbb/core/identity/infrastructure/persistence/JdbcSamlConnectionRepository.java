package com.github.houbb.core.identity.infrastructure.persistence;

import com.github.houbb.core.identity.application.domain.SamlConnection;
import com.github.houbb.core.identity.application.port.SamlConnectionRepository;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

@Repository
public class JdbcSamlConnectionRepository implements SamlConnectionRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcSamlConnectionRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void save(SamlConnection c) {
        jdbcTemplate.update(
                "INSERT INTO identity_saml_connection (connection_id, sp_entity_id, acs_url, idp_entity_id, " +
                "idp_sso_url, idp_slo_url, name_id_format, subject_attribute, email_attribute, name_attribute, " +
                "groups_attribute, sign_authn_requests, require_signed_response, require_signed_assertion, " +
                "require_encrypted_assertion, clock_skew_seconds, metadata_xml_encrypted, metadata_fetched_at, " +
                "created_at, updated_at, version) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                c.getConnectionId(), c.getSpEntityId(), c.getAcsUrl(), c.getIdpEntityId(),
                c.getIdpSsoUrl(), c.getIdpSloUrl(), c.getNameIdFormat(), c.getSubjectAttribute(),
                c.getEmailAttribute(), c.getNameAttribute(), c.getGroupsAttribute(),
                c.getSignAuthnRequests(), c.getRequireSignedResponse(), c.getRequireSignedAssertion(),
                c.getRequireEncryptedAssertion(), c.getClockSkewSeconds(), c.getMetadataXmlEncrypted(),
                c.getMetadataFetchedAt(), c.getCreatedAt(), c.getUpdatedAt(), c.getVersion()
        );
    }

    @Override
    public Optional<SamlConnection> findById(String connectionId) {
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(
                    "SELECT * FROM identity_saml_connection WHERE connection_id = ?",
                    new SamlConnectionRowMapper(), connectionId));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public void update(SamlConnection c) {
        jdbcTemplate.update(
                "UPDATE identity_saml_connection SET sp_entity_id = ?, acs_url = ?, idp_entity_id = ?, " +
                "idp_sso_url = ?, idp_slo_url = ?, name_id_format = ?, subject_attribute = ?, " +
                "email_attribute = ?, name_attribute = ?, groups_attribute = ?, sign_authn_requests = ?, " +
                "require_signed_response = ?, require_signed_assertion = ?, require_encrypted_assertion = ?, " +
                "clock_skew_seconds = ?, metadata_xml_encrypted = ?, metadata_fetched_at = ?, " +
                "updated_at = ?, version = version + 1 WHERE connection_id = ? AND version = ?",
                c.getSpEntityId(), c.getAcsUrl(), c.getIdpEntityId(), c.getIdpSsoUrl(),
                c.getIdpSloUrl(), c.getNameIdFormat(), c.getSubjectAttribute(), c.getEmailAttribute(),
                c.getNameAttribute(), c.getGroupsAttribute(), c.getSignAuthnRequests(),
                c.getRequireSignedResponse(), c.getRequireSignedAssertion(), c.getRequireEncryptedAssertion(),
                c.getClockSkewSeconds(), c.getMetadataXmlEncrypted(), c.getMetadataFetchedAt(),
                c.getUpdatedAt(), c.getConnectionId(), c.getVersion()
        );
    }

    static class SamlConnectionRowMapper implements RowMapper<SamlConnection> {
        @Override
        public SamlConnection mapRow(ResultSet rs, int rowNum) throws SQLException {
            SamlConnection c = new SamlConnection();
            c.setConnectionId(rs.getString("connection_id"));
            c.setSpEntityId(rs.getString("sp_entity_id"));
            c.setAcsUrl(rs.getString("acs_url"));
            c.setIdpEntityId(rs.getString("idp_entity_id"));
            c.setIdpSsoUrl(rs.getString("idp_sso_url"));
            c.setIdpSloUrl(rs.getString("idp_slo_url"));
            c.setNameIdFormat(rs.getString("name_id_format"));
            c.setSubjectAttribute(rs.getString("subject_attribute"));
            c.setEmailAttribute(rs.getString("email_attribute"));
            c.setNameAttribute(rs.getString("name_attribute"));
            c.setGroupsAttribute(rs.getString("groups_attribute"));
            c.setSignAuthnRequests(rs.getInt("sign_authn_requests"));
            c.setRequireSignedResponse(rs.getInt("require_signed_response"));
            c.setRequireSignedAssertion(rs.getInt("require_signed_assertion"));
            c.setRequireEncryptedAssertion(rs.getInt("require_encrypted_assertion"));
            c.setClockSkewSeconds(rs.getInt("clock_skew_seconds"));
            c.setMetadataXmlEncrypted(rs.getString("metadata_xml_encrypted"));
            Object fetchedAt = rs.getObject("metadata_fetched_at");
            if (fetchedAt != null) {
                c.setMetadataFetchedAt(((Number) fetchedAt).longValue());
            } else {
                c.setMetadataFetchedAt(null);
            }
            c.setCreatedAt(rs.getLong("created_at"));
            c.setUpdatedAt(rs.getLong("updated_at"));
            c.setVersion(rs.getLong("version"));
            return c;
        }
    }
}