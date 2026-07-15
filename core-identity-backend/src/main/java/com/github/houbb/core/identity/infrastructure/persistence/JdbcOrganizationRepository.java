package com.github.houbb.core.identity.infrastructure.persistence;

import com.github.houbb.core.identity.application.domain.Organization;
import com.github.houbb.core.identity.application.port.OrganizationRepository;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

@Repository
public class JdbcOrganizationRepository implements OrganizationRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcOrganizationRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void save(Organization organization) {
        jdbcTemplate.update(
                "INSERT INTO identity_organization (id, organization_type, name, slug, personal_owner_user_id, " +
                "status, created_at, updated_at, version) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
                organization.getId(), organization.getOrganizationType(), organization.getName(),
                organization.getSlug(), organization.getPersonalOwnerUserId(), organization.getStatus(),
                organization.getCreatedAt(), organization.getUpdatedAt(), organization.getVersion()
        );
    }

    @Override
    public Optional<Organization> findById(String id) {
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(
                    "SELECT * FROM identity_organization WHERE id = ?", new OrganizationRowMapper(), id));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<Organization> findByPersonalOwner(String userId) {
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(
                    "SELECT * FROM identity_organization WHERE personal_owner_user_id = ?",
                    new OrganizationRowMapper(), userId));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<Organization> findBySlug(String slug) {
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(
                    "SELECT * FROM identity_organization WHERE slug = ?", new OrganizationRowMapper(), slug));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public void update(Organization organization) {
        jdbcTemplate.update(
                "UPDATE identity_organization SET name = ?, slug = ?, status = ?, updated_at = ?, " +
                "version = version + 1 WHERE id = ? AND version = ?",
                organization.getName(), organization.getSlug(), organization.getStatus(),
                organization.getUpdatedAt(), organization.getId(), organization.getVersion()
        );
    }

    static class OrganizationRowMapper implements RowMapper<Organization> {
        @Override
        public Organization mapRow(ResultSet rs, int rowNum) throws SQLException {
            Organization o = new Organization();
            o.setId(rs.getString("id"));
            o.setOrganizationType(rs.getString("organization_type"));
            o.setName(rs.getString("name"));
            o.setSlug(rs.getString("slug"));
            o.setPersonalOwnerUserId(rs.getString("personal_owner_user_id"));
            o.setStatus(rs.getString("status"));
            o.setCreatedAt(rs.getLong("created_at"));
            o.setUpdatedAt(rs.getLong("updated_at"));
            o.setVersion(rs.getLong("version"));
            return o;
        }
    }
}