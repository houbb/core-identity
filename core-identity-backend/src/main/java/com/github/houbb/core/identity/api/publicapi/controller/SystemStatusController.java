package com.github.houbb.core.identity.api.publicapi.controller;

import com.github.houbb.core.identity.application.service.ClusterNodeService;
import com.github.houbb.core.identity.application.service.DegradationManager;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * System status and health aggregation endpoint (P7.6).
 * <p>
 * GET /api/v1/system/status
 */
@RestController
public class SystemStatusController {

    private final DegradationManager degradationManager;
    private final ClusterNodeService clusterNodeService;

    public SystemStatusController(DegradationManager degradationManager,
                                  ClusterNodeService clusterNodeService) {
        this.degradationManager = degradationManager;
        this.clusterNodeService = clusterNodeService;
    }

    @GetMapping("/api/v1/system/status")
    public ResponseEntity<Map<String, Object>> status() {
        Map<String, Object> result = new LinkedHashMap<>();

        // Overall status
        Map<String, Object> deps = degradationManager.getSystemStatus();
        result.put("overall", deps.get("overall"));

        // Cluster info
        Map<String, Object> cluster = new LinkedHashMap<>();
        cluster.put("enabled", clusterNodeService.isEnabled());
        cluster.put("nodeId", clusterNodeService.getNodeId());
        if (clusterNodeService.isEnabled()) {
            cluster.put("healthyNodes", clusterNodeService.getHealthyNodes().size());
            cluster.put("totalNodes", clusterNodeService.getAllNodes().size());
        }
        result.put("cluster", cluster);

        // Dependencies
        result.put("dependencies", deps);

        return ResponseEntity.ok(result);
    }
}
