package com.github.houbb.core.identity.infrastructure.persistence;

import com.github.houbb.core.identity.application.domain.ClusterNode;
import com.github.houbb.core.identity.application.port.ClusterNodeRepository;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * JDBC implementation of {@link ClusterNodeRepository}.
 */
@Repository
public class JdbcClusterNodeRepository implements ClusterNodeRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcClusterNodeRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void save(ClusterNode node) {
        jdbcTemplate.update(
                "INSERT INTO identity_cluster_node (id, instance_id, service_type, version, api_version, " +
                "region, availability_zone, hostname, status, started_at, last_heartbeat_at, " +
                "draining_at, metadata_json, created_at, updated_at, version) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                node.getId(), node.getInstanceId(), node.getServiceType(), node.getVersion(),
                node.getApiVersion(), node.getRegion(), node.getAvailabilityZone(), node.getHostname(),
                node.getStatus(), node.getStartedAt(), node.getLastHeartbeatAt(),
                node.getDrainingAt(), node.getMetadataJson(),
                node.getCreatedAt(), node.getUpdatedAt(), node.getVersion()
        );
    }

    @Override
    public Optional<ClusterNode> findById(String id) {
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(
                    "SELECT * FROM identity_cluster_node WHERE id = ?",
                    new ClusterNodeRowMapper(), id));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<ClusterNode> findByInstanceId(String instanceId) {
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(
                    "SELECT * FROM identity_cluster_node WHERE instance_id = ?",
                    new ClusterNodeRowMapper(), instanceId));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<ClusterNode> findByStatus(String status) {
        return jdbcTemplate.query(
                "SELECT * FROM identity_cluster_node WHERE status = ? ORDER BY started_at DESC",
                new ClusterNodeRowMapper(), status);
    }

    @Override
    public List<ClusterNode> findAllHealthy() {
        return jdbcTemplate.query(
                "SELECT * FROM identity_cluster_node WHERE status IN ('HEALTHY', 'DEGRADED') ORDER BY started_at DESC",
                new ClusterNodeRowMapper());
    }

    @Override
    public void updateHeartbeat(String id, long lastHeartbeatAt, long updatedAt, long version) {
        jdbcTemplate.update(
                "UPDATE identity_cluster_node SET last_heartbeat_at = ?, updated_at = ?, version = version + 1 " +
                "WHERE id = ? AND version = ?",
                lastHeartbeatAt, updatedAt, id, version);
    }

    @Override
    public void updateStatus(String id, String status, long updatedAt, long version) {
        jdbcTemplate.update(
                "UPDATE identity_cluster_node SET status = ?, updated_at = ?, version = version + 1 " +
                "WHERE id = ? AND version = ?",
                status, updatedAt, id, version);
    }

    @Override
    public void markExpired(long heartbeatTimeout, long now) {
        jdbcTemplate.update(
                "UPDATE identity_cluster_node SET status = 'UNAVAILABLE', updated_at = ? " +
                "WHERE status IN ('HEALTHY', 'DEGRADED') AND last_heartbeat_at < ?",
                now, heartbeatTimeout);
    }

    @Override
    public List<ClusterNode> findAll() {
        return jdbcTemplate.query(
                "SELECT * FROM identity_cluster_node ORDER BY started_at DESC",
                new ClusterNodeRowMapper());
    }

    static class ClusterNodeRowMapper implements RowMapper<ClusterNode> {
        @Override
        public ClusterNode mapRow(ResultSet rs, int rowNum) throws SQLException {
            ClusterNode node = new ClusterNode();
            node.setId(rs.getString("id"));
            node.setInstanceId(rs.getString("instance_id"));
            node.setServiceType(rs.getString("service_type"));
            node.setVersion(rs.getString("version"));
            node.setApiVersion(rs.getString("api_version"));
            node.setRegion(rs.getString("region"));
            node.setAvailabilityZone(rs.getString("availability_zone"));
            node.setHostname(rs.getString("hostname"));
            node.setStatus(rs.getString("status"));
            node.setStartedAt(rs.getLong("started_at"));
            node.setLastHeartbeatAt(rs.getLong("last_heartbeat_at"));
            node.setDrainingAt(JdbcUserRepository.getNullableLong(rs, "draining_at"));
            node.setMetadataJson(rs.getString("metadata_json"));
            node.setCreatedAt(rs.getLong("created_at"));
            node.setUpdatedAt(rs.getLong("updated_at"));
            node.setDbVersion(rs.getLong("version"));
            return node;
        }
    }
}
