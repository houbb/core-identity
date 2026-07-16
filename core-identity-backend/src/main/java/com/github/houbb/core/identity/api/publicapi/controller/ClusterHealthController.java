package com.github.houbb.core.identity.api.publicapi.controller;

import com.github.houbb.core.identity.application.service.ClusterNodeService;
import com.github.houbb.core.identity.infrastructure.database.DatabaseAdapter;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Cluster node health and status endpoint (P7.1).
 */
@RestController
public class ClusterHealthController {

    private final ClusterNodeService clusterNodeService;
    private final DatabaseAdapter databaseAdapter;

    public ClusterHealthController(ClusterNodeService clusterNodeService, DatabaseAdapter databaseAdapter) {
        this.clusterNodeService = clusterNodeService;
        this.databaseAdapter = databaseAdapter;
    }

    /**
     * Exposes node health and cluster status.
     * <p>
     * GET /api/v1/health
     */
    @GetMapping("/api/v1/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", "UP");
        result.put("timestamp", Instant.now().toString());

        // Node info
        Map<String, Object> node = new LinkedHashMap<>();
        node.put("nodeId", clusterNodeService.getNodeId());
        node.put("clusterEnabled", clusterNodeService.isEnabled());
        node.put("healthyNodes", clusterNodeService.isEnabled() ?
                clusterNodeService.getHealthyNodes().size() : 1);
        result.put("node", node);

        // Database info
        Map<String, Object> database = new LinkedHashMap<>();
        database.put("type", databaseAdapter.getType().name());
        database.put("readReplicaAvailable", databaseAdapter.isReadReplicaAvailable());
        result.put("database", database);

        return ResponseEntity.ok(result);
    }
}
