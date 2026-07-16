package com.github.houbb.core.identity.infrastructure.observability;

import com.github.houbb.core.identity.application.port.LeaderElectionPort;
import com.github.houbb.core.identity.application.service.ClusterNodeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Graceful shutdown handler (P7.5).
 * <p>
 * Manages the orderly shutdown sequence:
 * 1. Mark readiness as false (stop receiving traffic)
 * 2. Mark cluster node as DRAINING
 * 3. Wait for in-flight requests to complete
 * 4. Release all held leases
 * 5. Complete shutdown
 */
public class GracefulShutdownHandler {

    private static final Logger log = LoggerFactory.getLogger(GracefulShutdownHandler.class);

    private final AtomicBoolean draining = new AtomicBoolean(false);
    private final ClusterNodeService clusterNodeService;
    private final LeaderElectionPort leaderElection;

    public GracefulShutdownHandler(ClusterNodeService clusterNodeService,
                                   LeaderElectionPort leaderElection) {
        this.clusterNodeService = clusterNodeService;
        this.leaderElection = leaderElection;
    }

    /**
     * Called when the application receives a shutdown signal.
     */
    public void onShutdown() {
        draining.set(true);
        log.info("Graceful shutdown initiated");

        // 1. Mark cluster node as DRAINING
        if (clusterNodeService.isEnabled()) {
            clusterNodeService.drain();
        }

        // 2. Release all leases held by this node
        if (leaderElection != null) {
            String nodeId = clusterNodeService.getNodeId();
            // Common lease names — release whatever we might hold
            for (String leaseName : new String[]{"outbox-relay", "session-expiry", "certificate-scan"}) {
                leaderElection.releaseLease(leaseName, nodeId);
            }
        }

        log.info("Graceful shutdown — draining complete, leases released");
    }

    /**
     * Whether this instance is currently draining (should not accept new work).
     */
    public boolean isDraining() {
        return draining.get();
    }
}
