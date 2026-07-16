package com.github.houbb.core.identity.application.service;

import com.github.houbb.core.identity.application.domain.ClusterNode;
import com.github.houbb.core.identity.application.port.ClusterNodeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.UUID;

/**
 * Manages cluster node lifecycle: registration, heartbeat, and draining (P7.1).
 */
public class ClusterNodeService {

    private static final Logger log = LoggerFactory.getLogger(ClusterNodeService.class);

    private static final long HEARTBEAT_INTERVAL_MS = 15_000L;   // 15 seconds
    private static final long HEARTBEAT_TIMEOUT_MS = 45_000L;    // 3 missed heartbeats -> UNAVAILABLE

    private final ClusterNodeRepository repository;
    private final String serviceType;
    private final String appVersion;
    private final String apiVersion;
    private final String region;
    private final String availabilityZone;
    private final boolean enabled;

    private final String nodeId;

    public ClusterNodeService(ClusterNodeRepository repository,
                              @Value("${core.cluster.enabled:false}") boolean enabled,
                              @Value("${core.cluster.region:default}") String region,
                              @Value("${core.cluster.availability-zone:default}") String availabilityZone) {
        this.repository = repository;
        this.enabled = enabled;
        this.serviceType = "IDENTITY";
        this.appVersion = resolveAppVersion();
        this.apiVersion = "v1";
        this.region = region;
        this.availabilityZone = availabilityZone;
        this.nodeId = UUID.randomUUID().toString();

        if (enabled) {
            log.info("Cluster node service enabled — nodeId={}, serviceType={}, version={}",
                    nodeId, serviceType, appVersion);
        } else {
            log.info("Cluster node service disabled (standalone mode)");
        }
    }

    /**
     * Called at application startup. Registers this node in the cluster.
     */
    public ClusterNode register() {
        if (!enabled) {
            log.debug("Cluster disabled — skipping registration");
            return null;
        }

        String hostname = resolveHostname();
        long now = System.currentTimeMillis();

        ClusterNode node = new ClusterNode();
        node.setId(nodeId);
        node.setInstanceId(nodeId);
        node.setServiceType(serviceType);
        node.setVersion(appVersion);
        node.setApiVersion(apiVersion);
        node.setRegion(region);
        node.setAvailabilityZone(availabilityZone);
        node.setHostname(hostname);
        node.setStatus("HEALTHY");
        node.setStartedAt(now);
        node.setLastHeartbeatAt(now);
        node.setCreatedAt(now);
        node.setUpdatedAt(now);
        node.setDbVersion(1L);

        // Check if we've registered before (on restart)
        repository.findByInstanceId(nodeId).ifPresentOrElse(
                existing -> {
                    // Re-register: update status and heartbeat
                    repository.updateHeartbeat(existing.getId(), now, now, existing.getDbVersion());
                    log.info("Cluster node re-registered: id={}, hostname={}", existing.getId(), hostname);
                },
                () -> {
                    repository.save(node);
                    log.info("Cluster node registered: id={}, hostname={}, version={}", nodeId, hostname, appVersion);
                }
        );

        return node;
    }

    /**
     * Periodic heartbeat. Called by @Scheduled.
     * Also marks expired nodes as UNAVAILABLE.
     */
    public void heartbeat() {
        if (!enabled) {
            return;
        }

        long now = System.currentTimeMillis();
        repository.findByInstanceId(nodeId).ifPresent(node -> {
            repository.updateHeartbeat(node.getId(), now, now, node.getDbVersion());
            log.debug("Heartbeat sent for node {}", node.getId());
        });

        // Mark expired nodes
        long timeout = now - HEARTBEAT_TIMEOUT_MS;
        repository.markExpired(timeout, now);
    }

    /**
     * Called during graceful shutdown. Marks node as DRAINING.
     */
    public void drain() {
        if (!enabled) {
            return;
        }

        long now = System.currentTimeMillis();
        repository.findByInstanceId(nodeId).ifPresent(node -> {
            repository.updateStatus(node.getId(), "DRAINING", now, node.getDbVersion());
            log.info("Cluster node draining: id={}", node.getId());
        });
    }

    /**
     * Returns all healthy nodes in the cluster.
     */
    public List<ClusterNode> getHealthyNodes() {
        return repository.findAllHealthy();
    }

    /**
     * Returns all known nodes.
     */
    public List<ClusterNode> getAllNodes() {
        return repository.findAll();
    }

    public String getNodeId() {
        return nodeId;
    }

    public boolean isEnabled() {
        return enabled;
    }

    private static String resolveAppVersion() {
        // Try to read from manifest; fall back to "0.8.0" (current CHANGELOG version)
        Package pkg = ClusterNodeService.class.getPackage();
        if (pkg != null && pkg.getImplementationVersion() != null) {
            return pkg.getImplementationVersion();
        }
        return "0.8.0";
    }

    private static String resolveHostname() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            return "unknown";
        }
    }
}
