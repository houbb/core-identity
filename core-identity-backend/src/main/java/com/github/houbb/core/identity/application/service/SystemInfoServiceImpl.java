package com.github.houbb.core.identity.application.service;

import com.github.houbb.core.identity.application.domain.InstanceMetadata;
import com.github.houbb.core.identity.application.port.InstanceMetadataRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/**
 * Default implementation of SystemInfoService.
 */
public class SystemInfoServiceImpl implements SystemInfoService {

    private static final Logger log = LoggerFactory.getLogger(SystemInfoServiceImpl.class);

    private final String version;
    private final String apiVersion;
    private final String instanceName;
    private final String edition;
    private final String[] capabilities;

    public SystemInfoServiceImpl(String version, String apiVersion, String instanceName, String edition) {
        this.version = version;
        this.apiVersion = apiVersion;
        this.instanceName = instanceName;
        this.edition = edition;
        this.capabilities = new String[] {
                "SYSTEM_META",
                "INTERNAL_SERVICE_AUTH",
                "AUDIT_FOUNDATION",
                "OUTBOX_FOUNDATION"
        };
    }

    @Override
    public String getSystemInfo() {
        return "Core Identity Backend - " + instanceName + " (" + edition + ")";
    }

    @Override
    public String getVersion() {
        return version;
    }

    @Override
    public String getApiVersion() {
        return apiVersion;
    }

    @Override
    public String getStatus() {
        return "RUNNING";
    }

    @Override
    public String[] getCapabilities() {
        return capabilities;
    }
}