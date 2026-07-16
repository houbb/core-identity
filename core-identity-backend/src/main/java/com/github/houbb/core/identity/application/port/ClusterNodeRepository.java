package com.github.houbb.core.identity.application.port;

import com.github.houbb.core.identity.application.domain.ClusterNode;

import java.util.List;
import java.util.Optional;

/**
 * Repository for identity_cluster_node (P7.1).
 */
public interface ClusterNodeRepository {

    void save(ClusterNode node);

    Optional<ClusterNode> findById(String id);

    Optional<ClusterNode> findByInstanceId(String instanceId);

    List<ClusterNode> findByStatus(String status);

    List<ClusterNode> findAllHealthy();

    void updateHeartbeat(String id, long lastHeartbeatAt, long updatedAt, long version);

    void updateStatus(String id, String status, long updatedAt, long version);

    void markExpired(long heartbeatTimeout, long now);

    List<ClusterNode> findAll();
}
