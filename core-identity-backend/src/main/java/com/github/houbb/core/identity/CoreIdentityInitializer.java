package com.github.houbb.core.identity;

import com.github.houbb.core.identity.application.domain.InternalClient;
import com.github.houbb.core.identity.application.port.InternalClientRepository;
import com.github.houbb.core.identity.application.service.InternalTokenServiceImpl;
import com.github.houbb.core.identity.application.domain.InstanceMetadata;
import com.github.houbb.core.identity.application.port.InstanceMetadataRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Component that initializes instance metadata and default internal client on first boot.
 */
@Component
public class CoreIdentityInitializer {

    private static final Logger log = LoggerFactory.getLogger(CoreIdentityInitializer.class);

    private final InstanceMetadataRepository instanceRepo;
    private final InternalClientRepository clientRepo;

    @Value("${core.identity.instance-name:Core Identity}")
    private String instanceName;

    @Value("${core.identity.edition:COMMUNITY}")
    private String edition;

    @Value("${CORE_INTERNAL_CLIENT_ID:admin-backend}")
    private String defaultClientId;

    @Value("${CORE_INTERNAL_CLIENT_SECRET:dev-secret-change-in-production}")
    private String defaultClientSecret;

    public CoreIdentityInitializer(InstanceMetadataRepository instanceRepo,
                                    InternalClientRepository clientRepo) {
        this.instanceRepo = instanceRepo;
        this.clientRepo = clientRepo;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void initialize() {
        initializeInstanceMetadata();
        initializeInternalClient();
    }

    private void initializeInstanceMetadata() {
        String instanceId = "instance-" + UUID.randomUUID().toString().substring(0, 8);
        Optional<InstanceMetadata> existing = instanceRepo.findById(instanceId);

        if (existing.isEmpty()) {
            Instant now = Instant.now();
            InstanceMetadata metadata = new InstanceMetadata();
            metadata.setInstanceId(instanceId);
            metadata.setInstanceName(instanceName);
            metadata.setInstallationId("install-" + UUID.randomUUID().toString().substring(0, 8));
            metadata.setEdition(edition);
            metadata.setCurrentVersion("0.1.0");
            metadata.setSchemaVersion("0.1.0");
            metadata.setInstalledAt(now.toEpochMilli());
            metadata.setLastStartedAt(now.toEpochMilli());
            metadata.setCreatedAt(now.toEpochMilli());
            metadata.setUpdatedAt(now.toEpochMilli());

            instanceRepo.save(metadata);
            log.info("Initialized instance metadata: {}", instanceId);
        } else {
            // Update last started
            existing.get().setLastStartedAt(Instant.now().toEpochMilli());
            existing.get().setUpdatedAt(Instant.now().toEpochMilli());
            instanceRepo.update(existing.get());
            log.info("Updated instance last started: {}", instanceId);
        }
    }

    private void initializeInternalClient() {
        Optional<InternalClient> existing = clientRepo.findByClientId(defaultClientId);

        if (existing.isEmpty()) {
            Instant now = Instant.now();
            InternalClient client = new InternalClient();
            client.setId(UUID.randomUUID().toString());
            client.setClientId(defaultClientId);
            client.setClientSecretHash(InternalTokenServiceImpl.hashSecret(defaultClientSecret));
            client.setDisplayName("Admin Backend");
            client.setClientType("SERVICE");
            client.setScopes(List.of("identity.system.read", "identity.audit.write"));
            client.setStatus("ACTIVE");
            client.setExpiresAt(null);
            client.setLastUsedAt(null);
            client.setCreatedAt(now.toEpochMilli());
            client.setUpdatedAt(now.toEpochMilli());
            client.setVersion(1);

            clientRepo.save(client);
            log.info("Initialized default internal client: {}", defaultClientId);
        }
    }
}