package com.github.houbb.core.identity.admin.api.controller;

import com.github.houbb.core.identity.admin.application.service.BackendHealthAggregationService;
import com.github.houbb.core.identity.admin.application.service.ContractCompatibilityService;
import com.github.houbb.core.identity.admin.application.service.SystemOverviewService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Admin API: System overview, health, version, contracts.
 */
@RestController
@RequestMapping("/admin-api/v1/identity")
public class SystemController {

    private final SystemOverviewService overviewService;
    private final BackendHealthAggregationService healthService;
    private final ContractCompatibilityService contractService;

    public SystemController(SystemOverviewService overviewService,
                            BackendHealthAggregationService healthService,
                            ContractCompatibilityService contractService) {
        this.overviewService = overviewService;
        this.healthService = healthService;
        this.contractService = contractService;
    }

    @GetMapping("/bootstrap")
    public Map<String, Object> bootstrap() {
        return overviewService.getOverview();
    }

    @GetMapping("/system/overview")
    public Map<String, Object> overview() {
        return overviewService.getOverview();
    }

    @GetMapping("/system/health")
    public Map<String, Object> health() {
        return healthService.getDetailedHealth();
    }

    @GetMapping("/system/version")
    public Map<String, Object> version() {
        return Map.of(
                "service", "core-identity-admin-backend",
                "version", "0.1.0",
                "javaVersion", System.getProperty("java.version"),
                "springBootVersion", "3.3.0"
        );
    }

    @GetMapping("/system/contracts")
    public Map<String, Object> contracts() {
        return contractService.getContractVersions();
    }
}