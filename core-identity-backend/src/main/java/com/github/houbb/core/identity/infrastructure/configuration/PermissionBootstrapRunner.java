package com.github.houbb.core.identity.infrastructure.configuration;

import com.github.houbb.core.identity.application.service.PermissionCatalogService;
import com.github.houbb.core.identity.application.service.PermissionCatalogService.PermissionManifestEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.Yaml;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Bootstrap runner that registers Identity's own permission manifest on startup.
 */
@Component
public class PermissionBootstrapRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(PermissionBootstrapRunner.class);

    private final PermissionCatalogService catalogService;

    public PermissionBootstrapRunner(PermissionCatalogService catalogService) {
        this.catalogService = catalogService;
    }

    @Override
    public void run(ApplicationArguments args) {
        try {
            ClassPathResource resource = new ClassPathResource("permissions/identity-permissions.yml");
            Yaml yaml = new Yaml();
            @SuppressWarnings("unchecked")
            Map<String, Object> manifest = yaml.load(resource.getInputStream());

            String service = (String) manifest.get("service");
            String version = manifest.get("version").toString();

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> rawPermissions = (List<Map<String, Object>>) manifest.get("permissions");

            List<PermissionManifestEntry> entries = new ArrayList<>();
            if (rawPermissions != null) {
                for (Map<String, Object> p : rawPermissions) {
                    entries.add(new PermissionManifestEntry(
                            (String) p.get("code"),
                            (String) p.get("name"),
                            (String) p.get("resource"),
                            (String) p.get("action"),
                            (String) p.getOrDefault("riskLevel", "LOW"),
                            (String) p.get("description")
                    ));
                }
            }

            catalogService.syncPermissions(service, version, entries, "bootstrap");
            log.info("Identity permission manifest bootstrapped: {} permissions", entries.size());
        } catch (Exception e) {
            log.error("Failed to bootstrap identity permissions: {}", e.getMessage(), e);
        }
    }
}