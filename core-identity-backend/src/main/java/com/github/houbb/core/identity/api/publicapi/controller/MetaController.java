package com.github.houbb.core.identity.api.publicapi.controller;

import com.github.houbb.core.identity.api.response.MetaResponse;
import com.github.houbb.core.identity.application.service.SystemInfoService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Public API: Identity meta and capabilities.
 */
@RestController
@RequestMapping("/api/v1/identity")
public class MetaController {

    private final SystemInfoService systemInfoService;

    public MetaController(SystemInfoService systemInfoService) {
        this.systemInfoService = systemInfoService;
    }

    @GetMapping("/meta")
    public MetaResponse getMeta() {
        MetaResponse response = new MetaResponse();
        response.setService("core-identity");
        response.setVersion(systemInfoService.getVersion());
        response.setApiVersion(systemInfoService.getApiVersion());
        response.setStatus(systemInfoService.getStatus());
        response.setInstanceName("Core Identity");
        response.setEdition("COMMUNITY");
        response.setCapabilities(systemInfoService.getCapabilities());
        return response;
    }

    @GetMapping("/capabilities")
    public String[] getCapabilities() {
        return systemInfoService.getCapabilities();
    }
}