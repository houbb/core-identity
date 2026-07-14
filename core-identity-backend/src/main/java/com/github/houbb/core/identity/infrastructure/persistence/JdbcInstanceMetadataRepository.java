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

/**
 * JDBC implementation of InstanceMetadataRepository.
 */
@Repository
public class JdbcInstanceMetadataRepository implements InstanceMetadataRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcInstanceMetadataRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void save(InstanceMetadata metadata) {
        jdbcTemplate.update(
                "INSERT INTO identity_instance_metadata (instance_id, instance_name, installation_id, edition, " +
                "current_version, schema_version, installed_at, last_started_at, created_at, updated_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                metadata.getInstanceId(), metadata.getInstanceName(), metadata.getInstallationId(),
                metadata.getEdition(), metadata.getCurrentVersion(), metadata.getSchemaVersion(),
                metadata.getInstalledAt(), metadata.getLastStartedAt(),
                metadata.getCreatedAt(), metadata.getUpdatedAt()
        );
    }

    @Override
    public Optional<InstanceMetadata> findById(String instanceId) {
        try {
            InstanceMetadata metadata = jdbcTemplate.queryForObject(
                    "SELECT * FROM identity_instance_metadata WHERE instance_id = ?",
                    new InstanceMetadataRowMapper(), instanceId
            );
            return Optional.ofNullable(metadata);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public void update(InstanceMetadata metadata) {
        jdbcTemplate.update(
                "UPDATE identity_instance_metadata SET last_started_at = ?, updated_at = ? WHERE instance_id = ?",
                metadata.getLastStartedAt(), metadata.getUpdatedAt(), metadata.getInstanceId()
        );
    }

    static class InstanceMetadataRowMapper implements RowMapper<InstanceMetadata> {
        @Override
        public InstanceMetadata mapRow(ResultSet rs, int rowNum) throws SQLException {
            InstanceMetadata m = new InstanceMetadata();
            m.setInstanceId(rs.getString("instance_id"));
            m.setInstanceName(rs.getString("instance_name"));
            m.setInstallationId(rs.getString("installation_id"));
            m.setEdition(rs.getString("edition"));
            m.setCurrentVersion(rs.getString("current_version"));
            m.setSchemaVersion(rs.getString("schema_version"));
            m.setInstalledAt(rs.getLong("installed_at"));
            m.setLastStartedAt(rs.getLong("last_started_at"));
            m.setCreatedAt(rs.getLong("created_at"));
            m.setUpdatedAt(rs.getLong("updated_at"));
            return m;
        }
    }
}