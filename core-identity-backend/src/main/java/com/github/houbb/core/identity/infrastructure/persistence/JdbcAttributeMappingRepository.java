package com.github.houbb.core.identity.infrastructure.persistence;

import com.github.houbb.core.identity.application.domain.AttributeMapping;
import com.github.houbb.core.identity.application.port.AttributeMappingRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
public class JdbcAttributeMappingRepository implements AttributeMappingRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcAttributeMappingRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void save(AttributeMapping mapping) {
        jdbcTemplate.update(
                "INSERT INTO identity_attribute_mapping (id, connection_id, target_attribute, " +
                "source_attribute, source_type, ownership, required, default_value, " +
                "transformation_type, status, created_at, updated_at, version) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                mapping.getId(), mapping.getConnectionId(), mapping.getTargetAttribute(),
                mapping.getSourceAttribute(), mapping.getSourceType(), mapping.getOwnership(),
                mapping.getRequired(), mapping.getDefaultValue(), mapping.getTransformationType(),
                mapping.getStatus(), mapping.getCreatedAt(), mapping.getUpdatedAt(), mapping.getVersion()
        );
    }

    @Override
    public List<AttributeMapping> findByConnectionId(String connectionId) {
        return jdbcTemplate.query(
                "SELECT * FROM identity_attribute_mapping WHERE connection_id = ? ORDER BY created_at ASC",
                new AttributeMappingRowMapper(), connectionId);
    }

    @Override
    public void deleteById(String id) {
        jdbcTemplate.update(
                "DELETE FROM identity_attribute_mapping WHERE id = ?",
                id);
    }

    @Override
    public void update(AttributeMapping mapping) {
        jdbcTemplate.update(
                "UPDATE identity_attribute_mapping SET connection_id = ?, target_attribute = ?, " +
                "source_attribute = ?, source_type = ?, ownership = ?, required = ?, " +
                "default_value = ?, transformation_type = ?, status = ?, " +
                "updated_at = ?, version = version + 1 WHERE id = ? AND version = ?",
                mapping.getConnectionId(), mapping.getTargetAttribute(),
                mapping.getSourceAttribute(), mapping.getSourceType(), mapping.getOwnership(),
                mapping.getRequired(), mapping.getDefaultValue(), mapping.getTransformationType(),
                mapping.getStatus(), mapping.getUpdatedAt(), mapping.getId(), mapping.getVersion()
        );
    }

    static class AttributeMappingRowMapper implements RowMapper<AttributeMapping> {
        @Override
        public AttributeMapping mapRow(ResultSet rs, int rowNum) throws SQLException {
            AttributeMapping m = new AttributeMapping();
            m.setId(rs.getString("id"));
            m.setConnectionId(rs.getString("connection_id"));
            m.setTargetAttribute(rs.getString("target_attribute"));
            m.setSourceAttribute(rs.getString("source_attribute"));
            m.setSourceType(rs.getString("source_type"));
            m.setOwnership(rs.getString("ownership"));
            m.setRequired(rs.getInt("required"));
            m.setDefaultValue(rs.getString("default_value"));
            m.setTransformationType(rs.getString("transformation_type"));
            m.setStatus(rs.getString("status"));
            m.setCreatedAt(rs.getLong("created_at"));
            m.setUpdatedAt(rs.getLong("updated_at"));
            m.setVersion(rs.getLong("version"));
            return m;
        }
    }
}