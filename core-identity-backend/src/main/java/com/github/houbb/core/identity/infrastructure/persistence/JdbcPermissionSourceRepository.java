package com.github.houbb.core.identity.infrastructure.persistence;

import com.github.houbb.core.identity.application.domain.PermissionSource;
import com.github.houbb.core.identity.application.port.PermissionSourceRepository;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

@Repository
public class JdbcPermissionSourceRepository implements PermissionSourceRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcPermissionSourceRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void save(PermissionSource source) {
        jdbcTemplate.update(
                "INSERT INTO identity_permission_source (id, service_name, manifest_version, checksum, " +
                "last_synced_at, last_synced_by, status, created_at, updated_at, version) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                source.getId(), source.getServiceName(), source.getManifestVersion(),
                source.getChecksum(), source.getLastSyncedAt(), source.getLastSyncedBy(),
                source.getStatus(), source.getCreatedAt(), source.getUpdatedAt(), source.getVersion()
        );
    }

    @Override
    public Optional<PermissionSource> findByServiceName(String serviceName) {
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(
                    "SELECT * FROM identity_permission_source WHERE service_name = ?",
                    new PermissionSourceRowMapper(), serviceName));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public void update(PermissionSource source) {
        jdbcTemplate.update(
                "UPDATE identity_permission_source SET manifest_version = ?, checksum = ?, " +
                "last_synced_at = ?, last_synced_by = ?, status = ?, " +
                "updated_at = ?, version = version + 1 WHERE id = ? AND version = ?",
                source.getManifestVersion(), source.getChecksum(),
                source.getLastSyncedAt(), source.getLastSyncedBy(), source.getStatus(),
                source.getUpdatedAt(), source.getId(), source.getVersion()
        );
    }

    @Override
    public void updateSyncInfo(String id, String manifestVersion, String checksum,
                               long syncedAt, String syncedBy, long version) {
        jdbcTemplate.update(
                "UPDATE identity_permission_source SET manifest_version = ?, checksum = ?, " +
                "last_synced_at = ?, last_synced_by = ?, updated_at = ?, version = version + 1 " +
                "WHERE id = ? AND version = ?",
                manifestVersion, checksum, syncedAt, syncedBy, syncedAt, id, version);
    }

    static class PermissionSourceRowMapper implements RowMapper<PermissionSource> {
        @Override
        public PermissionSource mapRow(ResultSet rs, int rowNum) throws SQLException {
            PermissionSource s = new PermissionSource();
            s.setId(rs.getString("id"));
            s.setServiceName(rs.getString("service_name"));
            s.setManifestVersion(rs.getString("manifest_version"));
            s.setChecksum(rs.getString("checksum"));
            s.setLastSyncedAt(rs.getLong("last_synced_at"));
            s.setLastSyncedBy(rs.getString("last_synced_by"));
            s.setStatus(rs.getString("status"));
            s.setCreatedAt(rs.getLong("created_at"));
            s.setUpdatedAt(rs.getLong("updated_at"));
            s.setVersion(rs.getLong("version"));
            return s;
        }
    }
}